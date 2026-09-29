import { useEffect, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

import { AppLayout } from '@/components/layout/app-layout'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useAuth } from '@/hooks/use-auth'
import {
  atualizarUsuarioSchema,
  trocarSenhaSchema,
  type AtualizarUsuarioFormValues,
  type TrocarSenhaFormValues,
} from '@/lib/schemas/usuario'
import { atualizarUsuario, deletarUsuario, trocarSenha } from '@/services/usuario-service'

export function ContaPage() {
  const { session, logout, atualizarUsuario: sincronizarSessao } = useAuth()
  const navigate = useNavigate()

  const [salvandoDados, setSalvandoDados] = useState(false)
  const [salvandoSenha, setSalvandoSenha] = useState(false)
  const [excluindo, setExcluindo] = useState(false)
  const [confirmandoExclusao, setConfirmandoExclusao] = useState(false)

  const formDados = useForm<AtualizarUsuarioFormValues>({
    resolver: zodResolver(atualizarUsuarioSchema),
    defaultValues: { nome: '', email: '' },
  })

  const formSenha = useForm<TrocarSenhaFormValues>({
    resolver: zodResolver(trocarSenhaSchema),
    defaultValues: { email: '', novaSenha: '', confirmarSenha: '' },
  })

  /* Não existe GET /usuario/me: o login só devolve o token, e o nome guardado na
     sessão é derivado do e-mail. Depois da primeira gravação passa a ser o real. */
  useEffect(() => {
    if (!session) return
    formDados.reset({ nome: session.user.name, email: session.user.email })
    formSenha.setValue('email', session.user.email)
  }, [session, formDados, formSenha])

  async function salvarDados(valores: AtualizarUsuarioFormValues) {
    setSalvandoDados(true)

    try {
      const usuario = await atualizarUsuario(valores)
      sincronizarSessao({ id: usuario.idUsuario, name: usuario.nome, email: usuario.email })
      toast.success('Dados atualizados.')
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Não foi possível salvar.')
    } finally {
      setSalvandoDados(false)
    }
  }

  async function salvarSenha(valores: TrocarSenhaFormValues) {
    setSalvandoSenha(true)

    try {
      await trocarSenha({ email: valores.email, novaSenha: valores.novaSenha })
      formSenha.reset({ email: valores.email, novaSenha: '', confirmarSenha: '' })
      toast.success('Senha alterada. Use a nova no próximo login.')
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Não foi possível trocar a senha.')
    } finally {
      setSalvandoSenha(false)
    }
  }

  async function excluirConta() {
    setExcluindo(true)

    try {
      await deletarUsuario()
      toast.success('Conta excluída.')
      logout()
      navigate('/login', { replace: true })
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Não foi possível excluir a conta.')
      setExcluindo(false)
    }
  }

  return (
    <AppLayout title="Minha conta" description="Seus dados de acesso ao sistema.">
      <div className="grid max-w-3xl gap-4">
        <Card>
          <CardHeader>
            <CardTitle>Dados</CardTitle>
            <CardDescription>Nome e e-mail usados para entrar e para assinar as entregas.</CardDescription>
          </CardHeader>

          <CardContent className="pb-6">
            <form className="space-y-4" onSubmit={formDados.handleSubmit(salvarDados)} noValidate>
              <div className="flex flex-col gap-2">
                <Label htmlFor="nome">Nome</Label>
                <Input
                  id="nome"
                  {...formDados.register('nome')}
                  aria-invalid={Boolean(formDados.formState.errors.nome)}
                />
                {formDados.formState.errors.nome && (
                  <p className="text-sm text-destructive">{formDados.formState.errors.nome.message}</p>
                )}
              </div>

              <div className="flex flex-col gap-2">
                <Label htmlFor="email">E-mail</Label>
                <Input
                  id="email"
                  type="email"
                  autoComplete="email"
                  {...formDados.register('email')}
                  aria-invalid={Boolean(formDados.formState.errors.email)}
                />
                {formDados.formState.errors.email && (
                  <p className="text-sm text-destructive">{formDados.formState.errors.email.message}</p>
                )}
              </div>

              <Button type="submit" disabled={salvandoDados}>
                {salvandoDados ? 'Salvando...' : 'Salvar dados'}
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Senha</CardTitle>
            <CardDescription>Pelo menos 6 caracteres. A nova senha vale a partir do próximo login.</CardDescription>
          </CardHeader>

          <CardContent className="pb-6">
            <form className="space-y-4" onSubmit={formSenha.handleSubmit(salvarSenha)} noValidate>
              <div className="flex flex-col gap-2">
                <Label htmlFor="novaSenha">Nova senha</Label>
                <Input
                  id="novaSenha"
                  type="password"
                  autoComplete="new-password"
                  {...formSenha.register('novaSenha')}
                  aria-invalid={Boolean(formSenha.formState.errors.novaSenha)}
                />
                {formSenha.formState.errors.novaSenha && (
                  <p className="text-sm text-destructive">{formSenha.formState.errors.novaSenha.message}</p>
                )}
              </div>

              <div className="flex flex-col gap-2">
                <Label htmlFor="confirmarSenha">Confirmar nova senha</Label>
                <Input
                  id="confirmarSenha"
                  type="password"
                  autoComplete="new-password"
                  {...formSenha.register('confirmarSenha')}
                  aria-invalid={Boolean(formSenha.formState.errors.confirmarSenha)}
                />
                {formSenha.formState.errors.confirmarSenha && (
                  <p className="text-sm text-destructive">{formSenha.formState.errors.confirmarSenha.message}</p>
                )}
              </div>

              <Button type="submit" disabled={salvandoSenha}>
                {salvandoSenha ? 'Salvando...' : 'Trocar senha'}
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Excluir conta</CardTitle>
            <CardDescription>
              Você perde o acesso ao sistema. As entregas já registradas por você continuam no histórico.
            </CardDescription>
          </CardHeader>

          <CardContent className="pb-6">
            <Button variant="outline" onClick={() => setConfirmandoExclusao(true)}>
              Excluir minha conta
            </Button>
          </CardContent>
        </Card>
      </div>

      <AlertDialog open={confirmandoExclusao} onOpenChange={setConfirmandoExclusao}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Excluir sua conta</AlertDialogTitle>
            <AlertDialogDescription>
              Essa ação encerra sua sessão e não pode ser desfeita. Tem certeza?
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction disabled={excluindo} onClick={() => void excluirConta()}>
              Excluir
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </AppLayout>
  )
}
