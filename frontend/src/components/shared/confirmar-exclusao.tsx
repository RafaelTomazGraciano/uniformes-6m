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
  /** Rótulos do botão; o padrão é "Excluir" / "Excluindo…". */
  rotuloConfirmar?: string
  rotuloCarregando?: string
  /** `false` para confirmações que não apagam nada (ex.: registrar saída). */
  destrutiva?: boolean
  onConfirmar: () => void
  onCancelar: () => void
}

/** Diálogo só para confirmar; cadastros e edições vivem em páginas próprias. */
export function ConfirmarExclusao({
  aberto,
  titulo,
  descricao,
  carregando,
  rotuloConfirmar = 'Excluir',
  rotuloCarregando = 'Excluindo…',
  destrutiva = true,
  onConfirmar,
  onCancelar,
}: ConfirmarExclusaoProps) {
  return (
    <AlertDialog open={aberto} onOpenChange={(open) => !open && onCancelar()}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{titulo}</AlertDialogTitle>
          <AlertDialogDescription>{descricao}</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Cancelar</AlertDialogCancel>
          <AlertDialogAction variant={destrutiva ? 'destructive' : 'default'} disabled={carregando} onClick={onConfirmar}>
            {carregando ? rotuloCarregando : rotuloConfirmar}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
