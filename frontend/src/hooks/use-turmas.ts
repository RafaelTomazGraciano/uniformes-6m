import { useQuery } from '@tanstack/react-query'

import { listAllTurmas } from '@/services/turma-service'

export function useTurmasQuery() {
  return useQuery({
    queryKey: ['turmas', 'all'],
    queryFn: listAllTurmas,
    staleTime: 1000 * 60 * 5,
  })
}
