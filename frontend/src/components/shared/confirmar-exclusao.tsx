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

type ConfirmarExclusaoProps = {
  aberto: boolean
  titulo: string
  descricao: string
  carregando?: boolean
  onConfirmar: () => void
  onCancelar: () => void
}

/** Diálogo só para confirmar; cadastros e edições vivem em páginas próprias. */
export function ConfirmarExclusao({ aberto, titulo, descricao, carregando, onConfirmar, onCancelar }: ConfirmarExclusaoProps) {
  return (
    <AlertDialog open={aberto} onOpenChange={(open) => !open && onCancelar()}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{titulo}</AlertDialogTitle>
          <AlertDialogDescription>{descricao}</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Cancelar</AlertDialogCancel>
          <AlertDialogAction variant="destructive" disabled={carregando} onClick={onConfirmar}>
            {carregando ? 'Excluindo…' : 'Excluir'}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
