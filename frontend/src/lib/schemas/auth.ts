import { z } from 'zod'

export const loginSchema = z.object({
  email: z.email('Informe um e-mail válido.'),
  senha: z
    .string()
    .min(6, 'A senha deve ter pelo menos 6 caracteres.'),
})

export type LoginFormValues = z.infer<typeof loginSchema>

export const registerSchema = z
  .object({
    nome: z.string().min(3, 'Informe seu nome completo.'),
    email: z.email('Informe um e-mail valido.'),
    senha: z.string().min(6, 'A senha deve ter pelo menos 6 caracteres.'),
    confirmarSenha: z.string().min(6, 'Confirme sua senha.'),
  })
  .refine((values) => values.senha === values.confirmarSenha, {
    message: 'As senhas precisam ser iguais.',
    path: ['confirmarSenha'],
  })

export type RegisterFormValues = z.infer<typeof registerSchema>
