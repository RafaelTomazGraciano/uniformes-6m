import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { AtualizarAlunoPayload, CriarAlunoPayload, FiltroAlunos } from '@/lib/types/aluno'
import type { Pageable } from '@/lib/types/page'
import {
  createAluno,
  deleteAluno,
  getAluno,
  listAllAlunos,
  listAlunos,
  reativarAluno,
  registrarSaidaAluno,
  updateAluno,
} from '@/services/aluno-service'

const alunosKeys = {
  list: (pageable: Pageable, filtro: FiltroAlunos) => ['alunos', 'list', pageable, filtro] as const,
}

/* Mantém a página anterior enquanto a próxima busca chega: a lista não pisca a
   cada letra digitada. */
export function useAlunosQuery(pageable: Pageable = {}, filtro: FiltroAlunos = {}) {
  return useQuery({
    queryKey: alunosKeys.list(pageable, filtro),
    queryFn: () => listAlunos(pageable, filtro),
    placeholderData: keepPreviousData,
  })
}

export function useAllAlunosQuery(filtro: FiltroAlunos = {}) {
  return useQuery({
    queryKey: ['alunos', 'all', filtro],
    queryFn: () => listAllAlunos(filtro),
  })
}

export function useAlunoQuery(id: string | undefined | null) {
  return useQuery({
    queryKey: ['alunos', 'detail', id],
    queryFn: () => getAluno(id as string),
    enabled: Boolean(id),
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

/* As entregas carregam `alunoAtivo`, então a lista de entregas também é refeita. */
function useSituacaoAlunoMutation(acao: (id: string) => ReturnType<typeof registrarSaidaAluno>) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => acao(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alunos'] })
      queryClient.invalidateQueries({ queryKey: ['pedidos'] })
    },
  })
}

export function useRegistrarSaidaMutation() {
  return useSituacaoAlunoMutation(registrarSaidaAluno)
}

export function useReativarAlunoMutation() {
  return useSituacaoAlunoMutation(reativarAluno)
}
