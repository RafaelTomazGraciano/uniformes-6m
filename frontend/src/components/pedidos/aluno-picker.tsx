import { Label } from '@/components/ui/label'
import {
  Combobox,
  ComboboxActions,
  ComboboxClear,
  ComboboxCollection,
  ComboboxContent,
  ComboboxEmpty,
  ComboboxField,
  ComboboxInput,
  ComboboxItem,
  ComboboxList,
  ComboboxTrigger,
} from '@/components/ui/combobox'
import { filtrarAluno } from '@/lib/alunos'
import type { Aluno } from '@/lib/types/aluno'

const MAX_SUGESTOES = 8

type AlunoPickerProps = {
  alunos: Aluno[] | undefined
  isLoading: boolean
  isError: boolean
  value: string
  onChange: (alunoId: string) => void
  errorMessage?: string
}

export function AlunoPicker({ alunos, isLoading, isError, value, onChange, errorMessage }: AlunoPickerProps) {
  const selecionado = alunos?.find((aluno) => aluno.id === value) ?? null
  const indisponivel = isLoading || isError

  return (
    <div className="flex flex-col gap-2">
      <Label htmlFor="aluno-busca">Aluno</Label>

      <Combobox
        items={alunos ?? []}
        value={selecionado}
        onValueChange={(aluno: Aluno | null) => onChange(aluno?.id ?? '')}
        itemToStringLabel={(aluno: Aluno) => aluno.nome}
        isItemEqualToValue={(a: Aluno, b: Aluno) => a.id === b.id}
        filter={filtrarAluno}
        limit={MAX_SUGESTOES}
        disabled={indisponivel}
      >
        <ComboboxField>
          <ComboboxInput
            id="aluno-busca"
            aria-invalid={Boolean(errorMessage)}
            placeholder={
              isError ? 'Não foi possível carregar os alunos' : isLoading ? 'Carregando alunos…' : 'Busque pelo nome'
            }
          />
          <ComboboxActions>
            <ComboboxClear aria-label="Limpar aluno" />
            <ComboboxTrigger aria-label="Mostrar alunos" />
          </ComboboxActions>
        </ComboboxField>

        <ComboboxContent>
          <ComboboxEmpty>Nenhum aluno com esse nome. Verifique a grafia ou cadastre o aluno primeiro.</ComboboxEmpty>
          <ComboboxList>
            <ComboboxCollection>
              {(aluno: Aluno) => (
                <ComboboxItem key={aluno.id} value={aluno}>
                  <span className="truncate">{aluno.nome}</span>
                  <span className="ml-auto shrink-0 text-xs text-muted-foreground">{aluno.turmaNome}</span>
                </ComboboxItem>
              )}
            </ComboboxCollection>
          </ComboboxList>
        </ComboboxContent>
      </Combobox>

      {errorMessage && <p className="text-sm text-destructive">{errorMessage}</p>}
    </div>
  )
}
