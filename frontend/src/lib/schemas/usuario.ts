import { z } from 'zod'

export const atualizarUsuarioSchema = z.object({
  nome: z.string().trim().min(1, 'Informe seu nome.'),
  email: z.email('Informe um e-mail válido.'),
})

export type AtualizarUsuarioFormValues = z.infer<typeof atualizarUsuarioSchema>

export const trocarSenhaSchema = z
  .object({
    email: z.email('Informe um e-mail válido.'),
    novaSenha: z.string().min(6, 'A senha deve ter pelo menos 6 caracteres.'),
    confirmarSenha: z.string().min(6, 'Confirme a nova senha.'),
  })
  .refine((valores) => valores.novaSenha === valores.confirmarSenha, {
    message: 'As senhas precisam ser iguais.',
    path: ['confirmarSenha'],
  })

export type TrocarSenhaFormValues = z.infer<typeof trocarSenhaSchema>
