import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { AtualizarTurmaPayload, CriarTurmaPayload } from '@/lib/types/turma'
import { createTurma, deleteTurma, listAllTurmas, updateTurma } from '@/services/turma-service'

export function useTurmasQuery() {
  return useQuery({
    queryKey: ['turmas', 'all'],
    queryFn: listAllTurmas,
    staleTime: 1000 * 60 * 5,
  })
}

export function useCreateTurmaMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: CriarTurmaPayload) => createTurma(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['turmas'] })
    },
  })
}

export function useUpdateTurmaMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: AtualizarTurmaPayload }) => updateTurma(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['turmas'] })
      // Cada aluno carrega o nome da turma; renomear a turma muda a lista de alunos.
      queryClient.invalidateQueries({ queryKey: ['alunos'] })
    },
  })
}

export function useDeleteTurmaMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => deleteTurma(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['turmas'] })
      queryClient.invalidateQueries({ queryKey: ['alunos'] })
    },
  })
}
