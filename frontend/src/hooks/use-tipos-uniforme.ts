import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { AtualizarTipoUniformePayload, CriarTipoUniformePayload } from '@/lib/types/tipo-uniforme'
import {
  createTipoUniforme,
  deleteTipoUniforme,
  listAllTiposUniforme,
  updateTipoUniforme,
} from '@/services/tipo-uniforme-service'

export function useTiposUniformeQuery() {
  return useQuery({
    queryKey: ['tipos-uniforme', 'all'],
    queryFn: listAllTiposUniforme,
    staleTime: 1000 * 60 * 5,
  })
}

export function useCreateTipoUniformeMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: CriarTipoUniformePayload) => createTipoUniforme(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tipos-uniforme'] })
    },
  })
}

export function useUpdateTipoUniformeMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: AtualizarTipoUniformePayload }) =>
      updateTipoUniforme(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tipos-uniforme'] })
      // O nome do tipo aparece junto de cada uniforme em estoque.
      queryClient.invalidateQueries({ queryKey: ['uniformes'] })
    },
  })
}

export function useDeleteTipoUniformeMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => deleteTipoUniforme(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tipos-uniforme'] })
      queryClient.invalidateQueries({ queryKey: ['uniformes'] })
    },
  })
}
