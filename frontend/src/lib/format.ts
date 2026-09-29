const numero = new Intl.NumberFormat('pt-BR')
const mes = new Intl.DateTimeFormat('pt-BR', { month: 'long' })
const dataCurta = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short' })
const relativo = new Intl.RelativeTimeFormat('pt-BR', { numeric: 'auto' })

export function formatarNumero(valor: number): string {
  return numero.format(valor)
}

export function nomeDoMes(data: Date = new Date()): string {
  return mes.format(data)
}

/** "hoje", "ontem", "há 3 dias" e, passada uma semana, a data curta. */
export function formatarQuando(valor: string, agora: Date = new Date()): string {
  const data = new Date(valor)
  if (Number.isNaN(data.getTime())) return '—'

  const dias = Math.round((inicioDoDia(data).getTime() - inicioDoDia(agora).getTime()) / 86_400_000)

  if (Math.abs(dias) <= 6) return relativo.format(dias, 'day')
  return dataCurta.format(data)
}

function inicioDoDia(data: Date): Date {
  return new Date(data.getFullYear(), data.getMonth(), data.getDate())
}

export function formatarData(valor: string | null | undefined): string {
  if (!valor) return '—'
  const data = new Date(valor)
  return Number.isNaN(data.getTime()) ? '—' : dataCurta.format(data)
}

/* O backend espera LocalDateTime, que não carrega fuso. Mandar o ISO com "Z"
   faria o servidor guardar o horário UTC e, à noite, o dia errado. */
export function paraLocalDateTime(data: Date): string {
  const doisDigitos = (valor: number) => String(valor).padStart(2, '0')
  const dia = `${data.getFullYear()}-${doisDigitos(data.getMonth() + 1)}-${doisDigitos(data.getDate())}`
  const hora = `${doisDigitos(data.getHours())}:${doisDigitos(data.getMinutes())}:${doisDigitos(data.getSeconds())}`
  return `${dia}T${hora}`
}

/** `input[type="date"]` só entende "YYYY-MM-DD". */
export function dataParaInput(valor: string | null | undefined): string {
  return valor ? valor.slice(0, 10) : ''
}

export function inputParaLocalDateTime(valor: string): string {
  return `${valor}T00:00:00`
}

export function hojeParaInput(): string {
  return dataParaInput(paraLocalDateTime(new Date()))
}
