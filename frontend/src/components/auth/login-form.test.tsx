import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'

import { LoginForm } from '@/components/auth/login-form'
import { renderWithProviders } from '@/test/render'

describe('LoginForm', () => {
  it('alterna a visibilidade da senha pelo botão do olho', async () => {
    const user = userEvent.setup()
    renderWithProviders(<LoginForm />)

    const senha = screen.getByLabelText('Senha')
    expect(senha).toHaveAttribute('type', 'password')

    await user.click(screen.getByRole('button', { name: 'Mostrar senha' }))
    expect(senha).toHaveAttribute('type', 'text')

    await user.click(screen.getByRole('button', { name: 'Ocultar senha' }))
    expect(senha).toHaveAttribute('type', 'password')
  })

  it('não envia o formulário ao revelar a senha', async () => {
    const user = userEvent.setup()
    renderWithProviders(<LoginForm />)

    await user.type(screen.getByLabelText('Usuario'), 'nao-e-email')
    await user.click(screen.getByRole('button', { name: 'Mostrar senha' }))

    // Se o botão fosse submit, a validação teria disparado aqui.
    expect(screen.queryByText('Informe um e-mail válido.')).not.toBeInTheDocument()
  })
})
