import { Component, type ErrorInfo, type ReactNode } from 'react'

type LimiteErroProps = {
  /** O que aparece no lugar do trecho que falhou ao renderizar. */
  fallback: ReactNode
  children: ReactNode
}

/**
 * Isola um trecho da tela: se ele quebrar ao renderizar (ex.: uma linha com dado
 * inesperado), só ele vira o `fallback` e o resto da lista continua de pé.
 */
export class LimiteErro extends Component<LimiteErroProps, { falhou: boolean }> {
  state = { falhou: false }

  static getDerivedStateFromError() {
    return { falhou: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Falha ao exibir um item da lista', error, info.componentStack)
  }

  render() {
    return this.state.falhou ? this.props.fallback : this.props.children
  }
}
