import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Lock, Mail, UserRound, UserRoundPlus } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { registerUser } from '@/lib/auth'
import { registerSchema, type RegisterFormValues } from '@/lib/schemas/auth'

export function RegisterForm() {
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      nome: '',
      email: '',
      senha: '',
      confirmarSenha: '',
    },
  })

  const mutation = useMutation({
    mutationFn: async (values: RegisterFormValues) => {
      await registerUser({
        nome: values.nome,
        email: values.email,
        senha: values.senha,
      })
    },
    onSuccess: () => {
      toast.success('Conta criada com sucesso. Agora faça login.')
      navigate('/login', { replace: true })
    },
    onError: (error: Error) => {
      toast.error(error.message)
    },
  })

  return (
    <form className="space-y-5" onSubmit={handleSubmit((values) => mutation.mutate(values))} noValidate>
      <div className="space-y-2.5">
        <Label htmlFor="nome" className="text-[15px] font-semibold text-foreground">
          Nome
        </Label>
        <div className="relative">
          <UserRound className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            id="nome"
            type="text"
            placeholder="Digite seu nome"
            autoComplete="name"
            className="h-11 rounded-md border-border bg-card pl-9 text-[13px] text-foreground placeholder:text-muted-foreground focus-visible:ring-ring"
            {...register('nome')}
            aria-invalid={Boolean(errors.nome)}
          />
        </div>
        {errors.nome && <p className="text-sm text-destructive">{errors.nome.message}</p>}
      </div>

      <div className="space-y-2.5">
        <Label htmlFor="email" className="text-[15px] font-semibold text-foreground">
          E-mail
        </Label>
        <div className="relative">
          <Mail className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            id="email"
            type="email"
            placeholder="Digite seu e-mail"
            autoComplete="email"
            className="h-11 rounded-md border-border bg-card pl-9 text-[13px] text-foreground placeholder:text-muted-foreground focus-visible:ring-ring"
            {...register('email')}
            aria-invalid={Boolean(errors.email)}
          />
        </div>
        {errors.email && <p className="text-sm text-destructive">{errors.email.message}</p>}
      </div>

      <div className="space-y-2.5">
        <Label htmlFor="senha" className="text-[15px] font-semibold text-foreground">
          Senha
        </Label>
        <div className="relative">
          <Lock className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            id="senha"
            type="password"
            placeholder="Digite sua senha"
            autoComplete="new-password"
            className="h-11 rounded-md border-border bg-card pl-9 text-[13px] text-foreground placeholder:text-muted-foreground focus-visible:ring-ring"
            {...register('senha')}
            aria-invalid={Boolean(errors.senha)}
          />
        </div>
        {errors.senha && <p className="text-sm text-destructive">{errors.senha.message}</p>}
      </div>

      <div className="space-y-2.5">
        <Label htmlFor="confirmarSenha" className="text-[15px] font-semibold text-foreground">
          Confirmar senha
        </Label>
        <div className="relative">
          <Lock className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            id="confirmarSenha"
            type="password"
            placeholder="Confirme sua senha"
            autoComplete="new-password"
            className="h-11 rounded-md border-border bg-card pl-9 text-[13px] text-foreground placeholder:text-muted-foreground focus-visible:ring-ring"
            {...register('confirmarSenha')}
            aria-invalid={Boolean(errors.confirmarSenha)}
          />
        </div>
        {errors.confirmarSenha && <p className="text-sm text-destructive">{errors.confirmarSenha.message}</p>}
      </div>

      <Button
        type="submit"
        className="h-11 w-full rounded-md bg-primary text-[15px] font-medium text-primary-foreground hover:bg-primary/90"
        disabled={mutation.isPending}
      >
        <UserRoundPlus className="size-4" />
        {mutation.isPending ? 'Cadastrando...' : 'Criar conta'}
      </Button>

      <p className="text-center text-sm text-muted-foreground">
        Ja possui conta?{' '}
        <Link to="/login" className="font-medium text-primary hover:underline">
          Entrar
        </Link>
      </p>
    </form>
  )
}
