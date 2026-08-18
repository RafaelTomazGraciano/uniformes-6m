import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { EyeOff, Lock, LogIn, User } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useAuth } from '@/hooks/use-auth'
import { loginSchema, type LoginFormValues } from '@/lib/schemas/auth'

export function LoginForm() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [rememberMe, setRememberMe] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      senha: '',
    },
  })

  const mutation = useMutation({
    mutationFn: async (values: LoginFormValues) => {
      await login(values.email, values.senha)
    },
    onSuccess: () => {
      toast.success('Login realizado com sucesso.')
      navigate('/dashboard', { replace: true })
    },
    onError: (error: Error) => {
      toast.error(error.message)
    },
  })

  return (
    <form className="space-y-7" onSubmit={handleSubmit((values) => mutation.mutate(values))} noValidate>
      <div className="space-y-2.5">
        <Label htmlFor="email" className="text-[15px] font-semibold text-foreground">
          Usuario
        </Label>
        <div className="relative">
          <User className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            id="email"
            type="email"
            placeholder="Digite seu usuario"
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
            autoComplete="current-password"
            className="h-11 rounded-md border-border bg-card pl-9 pr-10 text-[13px] text-foreground placeholder:text-muted-foreground focus-visible:ring-ring"
            {...register('senha')}
            aria-invalid={Boolean(errors.senha)}
          />
          <EyeOff className="pointer-events-none absolute right-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
        </div>
        {errors.senha && <p className="text-sm text-destructive">{errors.senha.message}</p>}
      </div>

      <div className="flex items-center justify-between gap-3">
        <Label htmlFor="remember" className="cursor-pointer gap-2 text-[11px] font-medium text-muted-foreground">
          <Checkbox
            id="remember"
            checked={rememberMe}
            onCheckedChange={(checked) => setRememberMe(checked === true)}
          />
          Lembrar-me
        </Label>

        <Button
          type="button"
          variant="link"
          className="h-auto p-0 text-[11px] font-medium text-primary"
          onClick={() => toast.warning('Recuperacao de senha em breve.')}
        >
          Esqueci minha senha
        </Button>
      </div>

      <Button
        type="submit"
        className="h-11 w-full rounded-md bg-primary text-[15px] font-medium text-primary-foreground hover:bg-primary/90"
        disabled={mutation.isPending}
      >
        <LogIn className="size-4" />
        {mutation.isPending ? 'Entrando...' : 'Entrar'}
      </Button>

      <p className="text-center text-sm text-muted-foreground">
        Ainda nao possui conta?{' '}
        <Link to="/register" className="font-medium text-primary hover:underline">
          Cadastre-se
        </Link>
      </p>
    </form>
  )
}
