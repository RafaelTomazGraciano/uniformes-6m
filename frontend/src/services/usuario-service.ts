import { apiFetch } from '@/lib/api-client'
import type { AtualizarUsuarioPayload, TrocarSenhaPayload, Usuario } from '@/lib/types/usuario'

export function atualizarUsuario(payload: AtualizarUsuarioPayload): Promise<Usuario> {
  return apiFetch('/usuario/atualizar', { method: 'PUT', body: payload })
}

/** Não exige autenticação: é também o caminho de "esqueci minha senha". */
export function trocarSenha(payload: TrocarSenhaPayload): Promise<{ message: string }> {
  return apiFetch('/usuario/trocar-senha', { method: 'PUT', body: payload })
}

export function deletarUsuario(): Promise<{ message: string }> {
  return apiFetch('/usuario/deletar', { method: 'DELETE' })
}
