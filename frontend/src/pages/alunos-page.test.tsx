import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/aluno-service')
vi.mock('@/services/turma-service')
vi.mock('@/services/pedido-service')

import { AlunosPage } from '@/pages/alunos-page'
import * as alunoService from '@/services/aluno-service'
import * as pedidoService from '@/services/pedido-service'
import * as turmaService from '@/services/turma-service'
import { joao, maria, turmaA, turmaB } from '@/test/fixtures'
import { renderWithProviders } from '@/test/render'

describe('AlunosPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(alunoService.listAllAlunos).mockResolvedValue([maria, joao])
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

  it('lista os alunos vindos do backend', async () => {
    renderWithProviders(<AlunosPage />)

    expect(await screen.findByText('Maria Silva')).toBeInTheDocument()
    expect(screen.getByText('João Conceição')).toBeInTheDocument()
  })

  it('filtra por nome ignorando acentos', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunosPage />)

    await screen.findByText('Maria Silva')
    await user.type(screen.getByLabelText('Buscar aluno'), 'conceicao')

    expect(screen.getByText('João Conceição')).toBeInTheDocument()
    expect(screen.queryByText('Maria Silva')).not.toBeInTheDocument()
  })

  it('mostra o nome da turma no filtro, nunca o id', async () => {
    renderWithProviders(<AlunosPage />, { rota: '/?turma=t2' })

    const filtro = await screen.findByRole('combobox', { name: /filtrar por turma/i })
    expect(await within(filtro).findByText(/6º Ano B/)).toBeInTheDocument()
    expect(filtro).not.toHaveTextContent('t2')
    expect(screen.queryByText('Maria Silva')).not.toBeInTheDocument()
  })

  it('explica quando a busca não encontra ninguém e oferece limpar', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunosPage />)

    await screen.findByText('Maria Silva')
    await user.type(screen.getByLabelText('Buscar aluno'), 'zzzz')

    expect(screen.getByText(/nenhum aluno com “zzzz”/i)).toBeInTheDocument()
    await user.click(screen.getAllByRole('button', { name: /limpar filtros/i })[0])
    expect(await screen.findByText('Maria Silva')).toBeInTheDocument()
  })

  it('abre a ficha do aluno com o histórico de entregas', async () => {
    renderWithProviders(<AlunosPage />, { rota: '/?ver=a1' })

    const ficha = await screen.findByRole('complementary', { name: 'Maria Silva' })
    expect(await within(ficha).findByText(/ainda não recebeu uniforme/i)).toBeInTheDocument()
    expect(pedidoService.listPedidos).toHaveBeenCalledWith(expect.anything(), { alunoId: 'a1' })
  })
})
