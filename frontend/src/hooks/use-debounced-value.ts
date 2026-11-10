import { useEffect, useState } from 'react'

/** O valor só acompanha `valor` depois de `atraso` ms sem mudanças (ex.: busca enquanto digita). */
export function useDebouncedValue<T>(valor: T, atraso = 250): T {
  const [atual, setAtual] = useState(valor)

  useEffect(() => {
    const timer = setTimeout(() => setAtual(valor), atraso)
    return () => clearTimeout(timer)
  }, [valor, atraso])

  return atual
}
