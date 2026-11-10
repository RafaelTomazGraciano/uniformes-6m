import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { Pageable } from '@/lib/types/page'
import type { CriarRequisicaoPayload, StatusRequisicao } from '@/lib/types/requisicao'
import {
  atenderRequisicao,
  cancelarRequisicao,
  createRequisicao,
  listRequisicoes,
} from '@/services/requisicao-service'

export function useRequisicoesQuery(status: StatusRequisicao[], pageable: Pageable = {}) {
  return useQuery({
    queryKey: ['requisicoes', 'list', status, pageable],
    queryFn: () => listRequisicoes({ sort: 'dataCriacao,desc', ...pageable }, { status }),
  })
}

export function useCreateRequisicaoMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: CriarRequisicaoPayload) => createRequisicao(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['requisicoes'] })
    },
  })
}

export function useAtenderRequisicaoMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => atenderRequisicao(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['requisicoes'] })
    },
  })
}

export function useCancelarRequisicaoMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => cancelarRequisicao(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['requisicoes'] })
    },
  })
}
