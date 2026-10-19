import type { VariantProps } from 'class-variance-authority'
import { Link, type LinkProps } from 'react-router-dom'

import { buttonVariants } from '@/components/ui/button'
import { cn } from '@/lib/utils'

/** Navegação com cara de botão: continua sendo um link para o leitor de tela (4.1.2). */
export function ButtonLink({ className, variant, size, ...props }: LinkProps & VariantProps<typeof buttonVariants>) {
  return <Link data-slot="button" className={cn(buttonVariants({ variant, size }), className)} {...props} />
}
