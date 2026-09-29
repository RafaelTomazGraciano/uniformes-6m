export type Usuario = {
  idUsuario: string
  nome: string
  email: string
}

export type AtualizarUsuarioPayload = {
  nome: string
  email: string
}

export type TrocarSenhaPayload = {
  email: string
  novaSenha: string
}
