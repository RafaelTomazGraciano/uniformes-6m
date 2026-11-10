import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/aluno-service')
vi.mock('@/services/turma-service')
vi.mock('@/services/pedido-service')

import { normalizar } from '@/lib/alunos'
import type { Aluno, FiltroAlunos } from '@/lib/types/aluno'
import type { Page, Pageable } from '@/lib/types/page'
import { AlunosPage } from '@/pages/alunos-page'
import * as alunoService from '@/services/aluno-service'
import * as pedidoService from '@/services/pedido-service'
import * as turmaService from '@/services/turma-service'
import { joao, maria, pedro, turmaA, turmaB } from '@/test/fixtures'
import { renderWithProviders } from '@/test/render'

const todos = [joao, maria, pedro]

/* Simula o filtro do backend: nome ou turma sem acento, turma e situação, paginado. */
function servidor(pageable: Pageable = {}, filtro: FiltroAlunos = {}): Promise<Page<Aluno>> {
  const termo = normalizar(filtro.nome ?? '')
  const filtrados = todos.filter(
    (aluno) =>
      (!termo || normalizar(aluno.nome).includes(termo) || normalizar(aluno.turmaNome).includes(termo)) &&
      (!filtro.turmaId || aluno.turmaId === filtro.turmaId) &&
      (filtro.ativo === undefined || aluno.ativo === filtro.ativo),
  )
  const size = pageable.size ?? 20
  const page = pageable.page ?? 0
  const content = filtrados.slice(page * size, (page + 1) * size)
  const totalPages = Math.ceil(filtrados.length / size)
  return Promise.resolve({
    content,
    totalElements: filtrados.length,
    totalPages,
    number: page,
    size,
    first: page === 0,
    last: page >= totalPages - 1,
  })
}

describe('AlunosPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(alunoService.listAlunos).mockImplementation(servidor)
    vi.mocked(alunoService.getAluno).mockImplementation((id) => Promise.resolve(todos.find((aluno) => aluno.id === id) as Aluno))
    vi.mocked(alunoService.registrarSaidaAluno).mockResolvedValue({ ...maria, ativo: false, dataSaida: '2026-11-10T10:00:00' })
    vi.mocked(alunoService.reativarAluno).mockResolvedValue({ ...pedro, ativo: true, dataSaida: null })
    vi.mocked(turmaService.listAllTurmas).mockResolvedValue([turmaA, turmaB])
    vi.mocked(pedidoService.listPedidos).mockResolvedValue({
      content: [],
      totalElements: 0,
      totalPages: 0,
      number: 0,
      size: 50,
      first: true,
      last: true,
    })
  })

  it('lista só os alunos ativos por padrão, filtrando no servidor', async () => {
    renderWithProviders(<AlunosPage />)

    expect(await screen.findByText('Maria Silva')).toBeInTheDocument()
    expect(screen.getByText('João Conceição')).toBeInTheDocument()
    expect(screen.queryByText('Pedro Alves')).not.toBeInTheDocument()
    expect(alunoService.listAlunos).toHaveBeenCalledWith(
      expect.objectContaining({ page: 0, sort: 'nome,asc' }),
      expect.objectContaining({ ativo: true }),
    )
  })

  it('busca por nome no servidor ignorando acentos', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunosPage />)

    await screen.findByText('Maria Silva')
    await user.type(screen.getByLabelText('Buscar aluno'), 'conceicao')

    await waitFor(() => expect(screen.queryByText('Maria Silva')).not.toBeInTheDocument())
    expect(screen.getByText('João Conceição')).toBeInTheDocument()
    expect(alunoService.listAlunos).toHaveBeenCalledWith(expect.anything(), expect.objectContaining({ nome: 'conceicao' }))
  })

  it('mostra o nome da turma no filtro, nunca o id', async () => {
    renderWithProviders(<AlunosPage />, { rota: '/?turma=t2' })

    const filtro = await screen.findByRole('combobox', { name: /filtrar por turma/i })
    expect(await within(filtro).findByText(/6º Ano B/)).toBeInTheDocument()
    expect(filtro).not.toHaveTextContent('t2')
    expect(await screen.findByText('João Conceição')).toBeInTheDocument()
    expect(screen.queryByText('Maria Silva')).not.toBeInTheDocument()
    expect(alunoService.listAlunos).toHaveBeenCalledWith(expect.anything(), expect.objectContaining({ turmaId: 't2' }))
  })

  it('explica quando a busca não encontra ninguém e oferece limpar', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunosPage />)

    await screen.findByText('Maria Silva')
    await user.type(screen.getByLabelText('Buscar aluno'), 'zzzz')

    expect(await screen.findByText(/nenhum aluno com “zzzz”/i)).toBeInTheDocument()
    await user.click(screen.getAllByRole('button', { name: /limpar filtros/i })[0])
    expect(await screen.findByText('Maria Silva')).toBeInTheDocument()
    expect(screen.getByLabelText('Buscar aluno')).toHaveValue('')
  })

  it('mostra os desligados com a data de saída', async () => {
    renderWithProviders(<AlunosPage />, { rota: '/?situacao=desligados' })

    expect(await screen.findByText('Pedro Alves')).toBeInTheDocument()
    expect(screen.getByText('Saiu em 15/03/2026')).toBeInTheDocument()
    expect(screen.queryByText('Maria Silva')).not.toBeInTheDocument()
    expect(alunoService.listAlunos).toHaveBeenCalledWith(expect.anything(), expect.objectContaining({ ativo: false }))
  })

  it('abre a ficha do aluno com o histórico de entregas', async () => {
    renderWithProviders(<AlunosPage />, { rota: '/?ver=a1' })

    const ficha = await screen.findByRole('complementary', { name: 'Maria Silva' })
    expect(await within(ficha).findByText(/ainda não recebeu uniforme/i)).toBeInTheDocument()
    expect(pedidoService.listPedidos).toHaveBeenCalledWith(expect.anything(), { alunoId: 'a1' })
  })

  it('registra a saída depois de confirmar que o histórico fica guardado', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunosPage />)

    await screen.findByText('Maria Silva')
    await user.click(screen.getByRole('button', { name: 'Mais ações para Maria Silva' }))
    await user.click(await screen.findByRole('menuitem', { name: /registrar saída/i }))

    const dialogo = await screen.findByRole('alertdialog')
    expect(dialogo).toHaveTextContent(/histórico de uniformes entregues continua guardado/i)
    await user.click(within(dialogo).getByRole('button', { name: 'Registrar saída' }))

    await waitFor(() => expect(alunoService.registrarSaidaAluno).toHaveBeenCalledWith('a1'))
  })

  it('não registra a saída quando a pessoa cancela', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunosPage />)

    await screen.findByText('Maria Silva')
    await user.click(screen.getByRole('button', { name: 'Mais ações para Maria Silva' }))
    await user.click(await screen.findByRole('menuitem', { name: /registrar saída/i }))
    await user.click(within(await screen.findByRole('alertdialog')).getByRole('button', { name: 'Cancelar' }))

    expect(alunoService.registrarSaidaAluno).not.toHaveBeenCalled()
  })

  it('na ficha de quem saiu, mantém o histórico e troca a nova entrega por reativar', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunosPage />, { rota: '/?situacao=desligados&ver=a3' })

    const ficha = await screen.findByRole('complementary', { name: 'Pedro Alves' })
    expect(within(ficha).getByText('Saiu em 15/03/2026')).toBeInTheDocument()
    expect(within(ficha).queryByRole('link', { name: /nova entrega/i })).not.toBeInTheDocument()
    expect(pedidoService.listPedidos).toHaveBeenCalledWith(expect.anything(), { alunoId: 'a3' })

    await user.click(within(ficha).getByRole('button', { name: 'Reativar' }))
    await waitFor(() => expect(alunoService.reativarAluno).toHaveBeenCalledWith('a3'))
  })
})
