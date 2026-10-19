/* O Select do Base UI só mostra o rótulo no gatilho quando recebe `items`;
   sem isso ele exibe o `value` cru — era assim que o UUID vazava para a tela. */
export type SelectItemData = { value: string; label: string }

export function toSelectItems<T>(lista: readonly T[] | undefined, valor: (item: T) => string, rotulo: (item: T) => string) {
  return (lista ?? []).map<SelectItemData>((item) => ({ value: valor(item), label: rotulo(item) }))
}

export function labelsToSelectItems<K extends string>(rotulos: Record<K, string>) {
  return (Object.entries(rotulos) as [K, string][]).map(([value, label]) => ({ value, label }))
}
