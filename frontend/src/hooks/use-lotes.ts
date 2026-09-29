import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { AtualizarLotePayload, CriarLotePayload } from '@/lib/types/lote'
import type { Pageable } from '@/lib/types/page'
import { createLote, listLotes, updateLote } from '@/services/lote-service'

/** Entrada de lote é o que faz o saldo subir: tudo que lê estoque precisa recarregar. */
function invalidarEstoque(queryClient: ReturnType<typeof useQueryClient>) {
  queryClient.invalidateQueries({ queryKey: ['lotes'] })
  queryClient.invalidateQueries({ queryKey: ['uniformes'] })
  queryClient.invalidateQueries({ queryKey: ['estoque'] })
}

export function useLotesQuery(pageable: Pageable = {}) {
  return useQuery({
    queryKey: ['lotes', 'list', pageable],
    queryFn: () => listLotes({ sort: 'dataEntrega,desc', ...pageable }),
  })
}

export function useCreateLoteMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: CriarLotePayload) => createLote(payload),
    onSuccess: () => invalidarEstoque(queryClient),
  })
}

export function useUpdateLoteMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: AtualizarLotePayload }) => updateLote(id, payload),
    onSuccess: () => invalidarEstoque(queryClient),
  })
}
