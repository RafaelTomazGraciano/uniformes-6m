import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { AtualizarAlunoPayload, CriarAlunoPayload } from '@/lib/types/aluno'
import type { Pageable } from '@/lib/types/page'
import { createAluno, deleteAluno, listAllAlunos, listAlunos, updateAluno } from '@/services/aluno-service'

const alunosKeys = {
  list: (pageable: Pageable) => ['alunos', 'list', pageable] as const,
}

export function useAlunosQuery(pageable: Pageable = {}) {
  return useQuery({
    queryKey: alunosKeys.list(pageable),
    queryFn: () => listAlunos(pageable),
  })
}

export function useAllAlunosQuery() {
  return useQuery({
    queryKey: ['alunos', 'all'],
    queryFn: listAllAlunos,
  })
}

export function useCreateAlunoMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: CriarAlunoPayload) => createAluno(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alunos'] })
    },
  })
}

export function useUpdateAlunoMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: AtualizarAlunoPayload }) => updateAluno(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alunos'] })
    },
  })
}

export function useDeleteAlunoMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => deleteAluno(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alunos'] })
    },
  })
}
