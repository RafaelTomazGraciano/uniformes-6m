import { useQuery } from '@tanstack/react-query'

import type { Pageable } from '@/lib/types/page'
import { listAllUniformes, listUniformes } from '@/services/uniforme-service'

export function useUniformesQuery(pageable: Pageable = {}) {
  return useQuery({
    queryKey: ['uniformes', 'list', pageable],
    queryFn: () => listUniformes(pageable),
  })
}

export function useAllUniformesQuery() {
  return useQuery({
    queryKey: ['uniformes', 'all'],
    queryFn: listAllUniformes,
  })
}
