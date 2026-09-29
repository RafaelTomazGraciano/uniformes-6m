import { useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { trocarSenhaSchema, type TrocarSenhaFormValues } from '@/lib/schemas/usuario'
import { trocarSenha } from '@/services/usuario-service'

export function TrocarSenhaPage() {
  const navigate = useNavigate()
  const [salvando, setSalvando] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<TrocarSenhaFormValues>({
    resolver: zodResolver(trocarSenhaSchema),
    defaultValues: { email: '', novaSenha: '', confirmarSenha: '' },
  })

  async function onSubmit(valores: TrocarSenhaFormValues) {
    setSalvando(true)

    try {
      await trocarSenha({ email: valores.email, novaSenha: valores.novaSenha })
      toast.success('Senha alterada. Entre com a nova senha.')
      navigate('/login', { replace: true })
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Não foi possível trocar a senha.')
    } finally {
      setSalvando(false)
    }
  }

  return (
    <div className="min-h-screen bg-background p-4 md:p-8">
      <div className="mx-auto flex min-h-[calc(100vh-2rem)] w-full max-w-3xl items-center justify-center md:min-h-[calc(100vh-4rem)]">
        <div className="relative w-full overflow-hidden rounded-3xl bg-background shadow-[0_30px_60px_rgba(0,0,0,0.2)]">
          <div className="m-4 rounded-xl bg-card md:m-8">
            <section className="flex items-center p-6 md:p-8">
              <div className="w-full px-1 md:px-2">
                <p className="mb-2 text-sm text-muted-foreground">
                  Informe o e-mail da conta e escolha uma nova senha.
                </p>
                <h1 className="mb-10 text-4xl font-semibold text-foreground md:text-[40px]">Trocar senha</h1>

                <form className="space-y-6" onSubmit={handleSubmit(onSubmit)} noValidate>
                  <div className="space-y-2.5">
                    <Label htmlFor="email" className="text-[15px] font-semibold">
                      E-mail
                    </Label>
                    <Input
                      id="email"
                      type="email"
                      autoComplete="email"
                      placeholder="seu@email.com"
                      className="h-11"
                      {...register('email')}
                      aria-invalid={Boolean(errors.email)}
                    />
                    {errors.email && <p className="text-sm text-destructive">{errors.email.message}</p>}
                  </div>

                  <div className="space-y-2.5">
                    <Label htmlFor="novaSenha" className="text-[15px] font-semibold">
                      Nova senha
                    </Label>
                    <Input
                      id="novaSenha"
                      type="password"
                      autoComplete="new-password"
                      className="h-11"
                      {...register('novaSenha')}
                      aria-invalid={Boolean(errors.novaSenha)}
                    />
                    {errors.novaSenha && <p className="text-sm text-destructive">{errors.novaSenha.message}</p>}
                  </div>

                  <div className="space-y-2.5">
                    <Label htmlFor="confirmarSenha" className="text-[15px] font-semibold">
                      Confirmar nova senha
                    </Label>
                    <Input
                      id="confirmarSenha"
                      type="password"
                      autoComplete="new-password"
                      className="h-11"
                      {...register('confirmarSenha')}
                      aria-invalid={Boolean(errors.confirmarSenha)}
                    />
                    {errors.confirmarSenha && (
                      <p className="text-sm text-destructive">{errors.confirmarSenha.message}</p>
                    )}
                  </div>

                  <Button type="submit" className="h-11 w-full" disabled={salvando}>
                    {salvando ? 'Salvando...' : 'Trocar senha'}
                  </Button>

                  <p className="text-center text-sm text-muted-foreground">
                    <Link to="/login" className="font-medium text-primary hover:underline">
                      Voltar para o login
                    </Link>
                  </p>
                </form>
              </div>
            </section>
          </div>
        </div>
      </div>
    </div>
  )
}
