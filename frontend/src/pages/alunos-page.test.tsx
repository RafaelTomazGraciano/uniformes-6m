import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/aluno-service')
vi.mock('@/services/turma-service')

import { AlunosPage } from '@/pages/alunos-page'
import * as alunoService from '@/services/aluno-service'
import * as turmaService from '@/services/turma-service'
import { renderWithProviders } from '@/test/render'

const maria = { id: 'a1', nome: 'Maria Silva', turmaId: 't1', turmaNome: '9º Ano A' }
const joao = { id: 'a2', nome: 'João Conceição', turmaId: 't2', turmaNome: '6º Ano B' }

const turmaA = { id: 't1', nome: '9º Ano A', turno: 'DIURNO' as const, ensino: 'FUNDAMENTAL' as const }
const turmaB = { id: 't2', nome: '6º Ano B', turno: 'VESPERTINO' as const, ensino: 'FUNDAMENTAL' as const }

/** O formulário e os filtros têm selects próprios; busca pelo rótulo evita ambiguidade. */
function selectDeTurmaDoFiltro() {
  return screen.getByLabelText('Turma', { selector: '[id="filtro-turma"]' })
}

describe('AlunosPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(alunoService.listAllAlunos).mockResolvedValue([maria, joao])
    vi.mocked(turmaService.listAllTurmas).mockResolvedValue([turmaA, turmaB])
    vi.mocked(alunoService.createAluno).mockResolvedValue(maria)
  })

  it('lista os alunos vindos do backend', async () => {
    renderWithProviders(<AlunosPage />)

    expect(await screen.findByText('Maria Silva')).toBeInTheDocument()
    expect(screen.getByText('João Conceição')).toBeInTheDocument()
  })

  it('cadastra um aluno pelo diálogo', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunosPage />)

    await screen.findByText('Maria Silva')

    await user.click(screen.getByRole('button', { name: /novo aluno/i }))

    const dialogo = await screen.findByRole('dialog')
    await user.type(within(dialogo).getByLabelText('Nome'), 'Pedro Souza')
    await user.click(within(dialogo).getByLabelText('Turma'))
    await user.click(await screen.findByRole('option', { name: /9º Ano A/i }))
    await user.click(within(dialogo).getByRole('button', { name: /^salvar$/i }))

    expect(alunoService.createAluno).toHaveBeenCalledWith({ nome: 'Pedro Souza', turmaId: 't1' })
  })

  it('filtra por nome ignorando acentos', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunosPage />)

    await screen.findByText('Maria Silva')

    await user.type(screen.getByLabelText('Buscar'), 'conceicao')

    expect(screen.getByText('João Conceição')).toBeInTheDocument()
    expect(screen.queryByText('Maria Silva')).not.toBeInTheDocument()
  })

  it('filtra pela turma escolhida', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunosPage />)

    await screen.findByText('Maria Silva')

    await user.click(selectDeTurmaDoFiltro())
    await user.click(await screen.findByRole('option', { name: /6º Ano B/i }))

    expect(screen.getByText('João Conceição')).toBeInTheDocument()
    expect(screen.queryByText('Maria Silva')).not.toBeInTheDocument()
  })

  it('explica quando a busca não encontra ninguém', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunosPage />)

    await screen.findByText('Maria Silva')

    await user.type(screen.getByLabelText('Buscar'), 'zzzz')

    expect(screen.getByText(/nenhum aluno encontrado para esta busca/i)).toBeInTheDocument()
  })

  it('limpa os filtros e volta a listar todo mundo', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunosPage />)

    await screen.findByText('Maria Silva')

    await user.type(screen.getByLabelText('Buscar'), 'maria')
    expect(screen.queryByText('João Conceição')).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /limpar/i }))

    expect(screen.getByText('João Conceição')).toBeInTheDocument()
    expect(screen.getByText('Maria Silva')).toBeInTheDocument()
  })
})
