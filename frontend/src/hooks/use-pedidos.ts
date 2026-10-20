import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { CriarPedidoPayload } from '@/lib/types/pedido'
import type { Pageable } from '@/lib/types/page'
import { createPedido, getPedido, listPedidos } from '@/services/pedido-service'

export function usePedidosQuery(pageable: Pageable = {}) {
  return useQuery({
    queryKey: ['pedidos', 'list', pageable],
    queryFn: () => listPedidos({ sort: 'dataEfetivada,desc', ...pageable }),
  })
}

/** Histórico de um aluno, do mais recente ao mais antigo. */
export function usePedidosDoAlunoQuery(alunoId: string | undefined) {
  return useQuery({
    queryKey: ['pedidos', 'aluno', alunoId],
    queryFn: () => listPedidos({ sort: 'dataEfetivada,desc', size: 50 }, { alunoId }),
    enabled: Boolean(alunoId),
  })
}

export function usePedidoQuery(id: string | undefined) {
  return useQuery({
    queryKey: ['pedidos', 'detail', id],
    queryFn: () => getPedido(id as string),
    enabled: Boolean(id),
  })
}

export function useCreatePedidoMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: CriarPedidoPayload) => createPedido(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pedidos'] })
      queryClient.invalidateQueries({ queryKey: ['uniformes'] })
    },
  })
}
