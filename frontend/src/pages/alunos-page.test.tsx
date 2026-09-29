import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/aluno-service')
vi.mock('@/services/turma-service')

import { AlunosPage } from '@/pages/alunos-page'
import * as alunoService from '@/services/aluno-service'
import * as turmaService from '@/services/turma-service'
import { renderWithProviders } from '@/test/render'

const alunoMock = { id: 'a1', nome: 'Maria Silva', turmaId: 't1', turmaNome: '9º Ano A' }
const turmaMock = { id: 't1', nome: '9º Ano A', turno: 'DIURNO' as const, ensino: 'FUNDAMENTAL' as const }

function mockPage<T>(content: T[]) {
  return { content, totalElements: content.length, totalPages: 1, number: 0, size: 10, first: true, last: true }
}

describe('AlunosPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(alunoService.listAlunos).mockResolvedValue(mockPage([alunoMock]))
    vi.mocked(alunoService.listAllAlunos).mockResolvedValue([alunoMock])
    vi.mocked(turmaService.listAllTurmas).mockResolvedValue([turmaMock])
    vi.mocked(alunoService.createAluno).mockResolvedValue(alunoMock)
  })

  it('renders the list of alunos fetched from the backend', async () => {
    renderWithProviders(<AlunosPage />)

    expect(await screen.findByText('Maria Silva')).toBeInTheDocument()
    expect(screen.getByText('9º Ano A')).toBeInTheDocument()
  })

  it('creates a new aluno through the form dialog', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunosPage />)

    await screen.findByText('Maria Silva')

    await user.click(screen.getByRole('button', { name: /novo aluno/i }))
    await user.type(screen.getByLabelText('Nome'), 'João Souza')

    await user.click(screen.getByRole('combobox'))
    await user.click(await screen.findByRole('option', { name: /9º Ano A/i }))

    await user.click(screen.getByRole('button', { name: /^salvar$/i }))

    expect(alunoService.createAluno).toHaveBeenCalledWith({ nome: 'João Souza', turmaId: 't1' })
  })
})
