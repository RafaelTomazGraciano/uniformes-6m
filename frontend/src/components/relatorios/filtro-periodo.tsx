import type { UseFormReturn } from 'react-hook-form'
import { Controller } from 'react-hook-form'

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import type { PeriodoFormValues } from '@/lib/schemas/relatorio'

const MESES = [
  'Janeiro',
  'Fevereiro',
  'Março',
  'Abril',
  'Maio',
  'Junho',
  'Julho',
  'Agosto',
  'Setembro',
  'Outubro',
  'Novembro',
  'Dezembro',
]

/** Um campo opcional vazio precisa virar `undefined`, não `NaN`. */
function paraNumero(valor: string): number | undefined {
  return valor === '' ? undefined : Number(valor)
}

export function FiltroPeriodo({ form }: { form: UseFormReturn<PeriodoFormValues> }) {
  const {
    control,
    register,
    watch,
    formState: { errors },
  } = form

  const porMes = watch('tipo') === 'MES'

  return (
    <Card>
      <CardHeader>
        <CardTitle>Período</CardTitle>
        <CardDescription>
          Vale para todos os relatórios menos o de estoque. Deixe o fim em branco para um mês ou ano único.
        </CardDescription>
      </CardHeader>

      <CardContent className="flex flex-wrap items-start gap-4 pb-6">
        <div className="flex w-40 flex-col gap-2">
          <Label htmlFor="tipo-periodo">Agrupar por</Label>
          <Controller
            control={control}
            name="tipo"
            render={({ field }) => (
              <Select value={field.value} onValueChange={(valor) => field.onChange(valor ?? 'MES')}>
                <SelectTrigger id="tipo-periodo" className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="MES">Mês</SelectItem>
                  <SelectItem value="ANO">Ano</SelectItem>
                </SelectContent>
              </Select>
            )}
          />
        </div>

        <div className="flex w-28 flex-col gap-2">
          <Label htmlFor="anoInicio">Ano inicial</Label>
          <Input
            id="anoInicio"
            type="number"
            min={2000}
            {...register('anoInicio', { setValueAs: paraNumero })}
            aria-invalid={Boolean(errors.anoInicio)}
          />
          {errors.anoInicio && <p className="text-sm text-destructive">{errors.anoInicio.message}</p>}
        </div>

        {porMes && (
          <div className="flex w-40 flex-col gap-2">
            <Label htmlFor="mesInicio">Mês inicial</Label>
            <Controller
              control={control}
              name="mesInicio"
              render={({ field }) => (
                <Select
                  value={field.value === undefined ? '' : String(field.value)}
                  onValueChange={(valor) => field.onChange(valor ? Number(valor) : undefined)}
                >
                  <SelectTrigger id="mesInicio" className="w-full">
                    <SelectValue placeholder="Selecione" />
                  </SelectTrigger>
                  <SelectContent>
                    {MESES.map((mes, indice) => (
                      <SelectItem key={mes} value={String(indice + 1)}>
                        {mes}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.mesInicio && <p className="text-sm text-destructive">{errors.mesInicio.message}</p>}
          </div>
        )}

        <div className="flex w-28 flex-col gap-2">
          <Label htmlFor="anoFim">Ano final</Label>
          <Input
            id="anoFim"
            type="number"
            min={2000}
            placeholder="Opcional"
            {...register('anoFim', { setValueAs: paraNumero })}
            aria-invalid={Boolean(errors.anoFim)}
          />
          {errors.anoFim && <p className="text-sm text-destructive">{errors.anoFim.message}</p>}
        </div>

        {porMes && (
          <div className="flex w-40 flex-col gap-2">
            <Label htmlFor="mesFim">Mês final</Label>
            <Controller
              control={control}
              name="mesFim"
              render={({ field }) => (
                <Select
                  value={field.value === undefined ? '' : String(field.value)}
                  onValueChange={(valor) => field.onChange(valor ? Number(valor) : undefined)}
                >
                  <SelectTrigger id="mesFim" className="w-full">
                    <SelectValue placeholder="Opcional" />
                  </SelectTrigger>
                  <SelectContent>
                    {MESES.map((mes, indice) => (
                      <SelectItem key={mes} value={String(indice + 1)}>
                        {mes}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.mesFim && <p className="text-sm text-destructive">{errors.mesFim.message}</p>}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
