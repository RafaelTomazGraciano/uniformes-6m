import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/aluno-service')
vi.mock('@/services/turma-service')

import { AlunoFormPage } from '@/pages/aluno-form-page'
import * as alunoService from '@/services/aluno-service'
import * as turmaService from '@/services/turma-service'
import { maria, turmaA, turmaB } from '@/test/fixtures'
import { renderWithProviders } from '@/test/render'

describe('AlunoFormPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(turmaService.listAllTurmas).mockResolvedValue([turmaA, turmaB])
    vi.mocked(alunoService.createAluno).mockResolvedValue(maria)
    vi.mocked(alunoService.getAluno).mockResolvedValue(maria)
  })

  it('cadastra um aluno em tela inteira', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunoFormPage />)

    await user.type(screen.getByLabelText('Nome completo'), 'Pedro Souza')
    await user.click(screen.getByLabelText('Turma'))
    await user.click(await screen.findByRole('option', { name: /9º Ano A/i }))
    await user.click(screen.getByRole('button', { name: /cadastrar aluno/i }))

    await waitFor(() => expect(alunoService.createAluno).toHaveBeenCalledWith({ nome: 'Pedro Souza', turmaId: 't1' }))
  })

  it('envia o contato do responsável só com dígitos', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunoFormPage />, { rota: '/?turma=t1' })

    await user.type(screen.getByLabelText('Nome completo'), 'Pedro Souza')
    await user.type(screen.getByLabelText('Responsável (opcional)'), 'Carla Souza')
    await user.type(screen.getByLabelText('Telefone do responsável (opcional)'), '(11) 98765-4321')
    await user.click(screen.getByRole('button', { name: /cadastrar aluno/i }))

    await waitFor(() =>
      expect(alunoService.createAluno).toHaveBeenCalledWith({
        nome: 'Pedro Souza',
        turmaId: 't1',
        nomeResponsavel: 'Carla Souza',
        telefoneResponsavel: '11987654321',
      }),
    )
  })

  it('recusa telefone sem DDD', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunoFormPage />, { rota: '/?turma=t1' })

    await user.type(screen.getByLabelText('Nome completo'), 'Pedro Souza')
    await user.type(screen.getByLabelText('Telefone do responsável (opcional)'), '98765')
    await user.click(screen.getByRole('button', { name: /cadastrar aluno/i }))

    expect(await screen.findByText(/10 ou 11 dígitos/i)).toBeInTheDocument()
    expect(alunoService.createAluno).not.toHaveBeenCalled()
  })

  it('preenche a turma vinda da URL e mostra o nome dela', async () => {
    renderWithProviders(<AlunoFormPage />, { rota: '/?turma=t2' })

    expect(await screen.findByText(/6º Ano B/)).toBeInTheDocument()
  })

  it('exige o nome antes de salvar', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AlunoFormPage />)

    await user.click(screen.getByRole('button', { name: /cadastrar aluno/i }))

    expect(await screen.findByText(/informe o nome do aluno/i)).toBeInTheDocument()
    expect(alunoService.createAluno).not.toHaveBeenCalled()
  })

  it('carrega o aluno para edição', async () => {
    renderWithProviders(<AlunoFormPage />, { rota: '/alunos/a1/editar', caminho: '/alunos/:id/editar' })

    expect(await screen.findByDisplayValue('Maria Silva')).toBeInTheDocument()
  })
})
