import { useEffect, useState, type ReactNode } from 'react'
import { Eye, EyeOff } from 'lucide-react'
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
import { Field, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput } from '@/components/ui/input-group'
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
  const [confirmacaoEmail, setConfirmacaoEmail] = useState('')

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

  const errosDados = formDados.formState.errors
  const errosSenha = formSenha.formState.errors
  const emailConfirmado = confirmacaoEmail.trim().toLowerCase() === (session?.user.email ?? '').toLowerCase()

  return (
    <AppLayout title="Minha conta" description="Seus dados de acesso ao sistema.">
      <div className="flex max-w-5xl flex-col">
        <Secao titulo="Dados" descricao="Nome e e-mail usados para entrar e para assinar as entregas.">
          <form onSubmit={formDados.handleSubmit(salvarDados)} noValidate>
            <FieldGroup>
              <Field data-invalid={Boolean(errosDados.nome) || undefined}>
                <FieldLabel htmlFor="nome">Nome</FieldLabel>
                <Input id="nome" autoComplete="name" aria-invalid={Boolean(errosDados.nome) || undefined} {...formDados.register('nome')} />
                <FieldError errors={[errosDados.nome]} />
              </Field>
              <Field data-invalid={Boolean(errosDados.email) || undefined}>
                <FieldLabel htmlFor="email">E-mail</FieldLabel>
                <Input
                  id="email"
                  type="email"
                  autoComplete="email"
                  aria-invalid={Boolean(errosDados.email) || undefined}
                  {...formDados.register('email')}
                />
                <FieldError errors={[errosDados.email]} />
              </Field>
              <Button type="submit" size="lg" className="self-start" disabled={salvandoDados}>
                {salvandoDados ? 'Salvando…' : 'Salvar dados'}
              </Button>
            </FieldGroup>
          </form>
        </Secao>

        <Secao titulo="Senha" descricao="Pelo menos 6 caracteres. A nova senha vale a partir do próximo login.">
          <form onSubmit={formSenha.handleSubmit(salvarSenha)} noValidate>
            <FieldGroup>
              <Field data-invalid={Boolean(errosSenha.novaSenha) || undefined}>
                <FieldLabel htmlFor="novaSenha">Nova senha</FieldLabel>
                <CampoSenha id="novaSenha" invalido={Boolean(errosSenha.novaSenha)} {...formSenha.register('novaSenha')} />
                <FieldError errors={[errosSenha.novaSenha]} />
              </Field>
              <Field data-invalid={Boolean(errosSenha.confirmarSenha) || undefined}>
                <FieldLabel htmlFor="confirmarSenha">Confirmar nova senha</FieldLabel>
                <CampoSenha id="confirmarSenha" invalido={Boolean(errosSenha.confirmarSenha)} {...formSenha.register('confirmarSenha')} />
                <FieldError errors={[errosSenha.confirmarSenha]} />
              </Field>
              <Button type="submit" size="lg" className="self-start" disabled={salvandoSenha}>
                {salvandoSenha ? 'Salvando…' : 'Trocar senha'}
              </Button>
            </FieldGroup>
          </form>
        </Secao>

        <Secao titulo="Excluir conta" descricao="Você perde o acesso ao sistema. As entregas registradas por você continuam no histórico." perigo>
          <div className="flex flex-col items-start gap-3 rounded-xl border border-destructive/40 bg-destructive/5 p-5">
            <p className="text-sm">Esta ação encerra sua sessão e não pode ser desfeita.</p>
            <Button variant="destructive" size="lg" onClick={() => setConfirmandoExclusao(true)}>
              Excluir minha conta
            </Button>
          </div>
        </Secao>
      </div>

      <AlertDialog
        open={confirmandoExclusao}
        onOpenChange={(aberto) => {
          setConfirmandoExclusao(aberto)
          if (!aberto) setConfirmacaoEmail('')
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Excluir sua conta</AlertDialogTitle>
            <AlertDialogDescription>
              Para confirmar, digite seu e-mail: <strong>{session?.user.email}</strong>
            </AlertDialogDescription>
          </AlertDialogHeader>
          <Field>
            <FieldLabel htmlFor="confirmacao-email" className="sr-only">
              Seu e-mail
            </FieldLabel>
            <Input
              id="confirmacao-email"
              type="email"
              autoComplete="off"
              value={confirmacaoEmail}
              onChange={(event) => setConfirmacaoEmail(event.target.value)}
            />
          </Field>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction variant="destructive" disabled={excluindo || !emailConfirmado} onClick={() => void excluirConta()}>
              {excluindo ? 'Excluindo…' : 'Excluir conta'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </AppLayout>
  )
}

/** Layout de configurações: título e explicação à esquerda, o formulário à direita. */
function Secao({ titulo, descricao, perigo, children }: { titulo: string; descricao: string; perigo?: boolean; children: ReactNode }) {
  const id = `secao-${titulo.toLowerCase().replace(/\s+/g, '-')}`
  return (
    <section aria-labelledby={id} className="grid gap-4 border-b border-border py-8 first:pt-0 last:border-0 md:grid-cols-[16rem_minmax(0,1fr)] md:gap-10">
      <div>
        <h2 id={id} className={perigo ? 'font-semibold text-destructive' : 'font-semibold'}>
          {titulo}
        </h2>
        <p className="mt-1 text-sm text-muted-foreground">{descricao}</p>
      </div>
      <div className="max-w-lg">{children}</div>
    </section>
  )
}

function CampoSenha({ invalido, ...props }: React.ComponentProps<'input'> & { invalido: boolean }) {
  const [visivel, setVisivel] = useState(false)
  return (
    <InputGroup className="h-9">
      <InputGroupInput type={visivel ? 'text' : 'password'} autoComplete="new-password" aria-invalid={invalido || undefined} {...props} />
      <InputGroupAddon align="inline-end">
        <InputGroupButton size="icon-sm" aria-label={visivel ? 'Ocultar senha' : 'Mostrar senha'} aria-pressed={visivel} onClick={() => setVisivel((atual) => !atual)}>
          {visivel ? <EyeOff aria-hidden /> : <Eye aria-hidden />}
        </InputGroupButton>
      </InputGroupAddon>
    </InputGroup>
  )
}
