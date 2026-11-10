import { afterEach, describe, expect, it, vi } from 'vitest'

import {
  SESSION_CLEARED_EVENT,
  expireSession,
  getStoredSession,
  getTokenExpiration,
  isTokenExpired,
} from '@/lib/auth'
import { agoraEmSegundos, fakeJwt, guardarSessao, sessaoCom } from '@/test/jwt'

const STORAGE_KEY = 'uniformes-auth-session'

describe('getTokenExpiration', () => {
  it('lê o exp de um payload base64url (com - e _)', () => {
    // "~~~" e "???" geram "+" e "/" em base64 comum, viram "-" e "_" em base64url.
    const token = fakeJwt({ sub: '~~~???', exp: 1_900_000_000 })
    expect(token.split('.')[1]).toMatch(/[-_]/)
    expect(getTokenExpiration(token)).toBe(1_900_000_000)
  })

  it('devolve null para token malformado ou sem exp', () => {
    expect(getTokenExpiration('nao-e-jwt')).toBeNull()
    expect(getTokenExpiration('a.%%%.c')).toBeNull()
    expect(getTokenExpiration(fakeJwt({ sub: 'x' }))).toBeNull()
  })

  it('isTokenExpired compara o exp com o relógio', () => {
    expect(isTokenExpired(fakeJwt({ exp: agoraEmSegundos() - 10 }))).toBe(true)
    expect(isTokenExpired(fakeJwt({ exp: agoraEmSegundos() + 600 }))).toBe(false)
  })
})

describe('getStoredSession', () => {
  afterEach(() => localStorage.clear())

  it('devolve a sessão quando o token ainda vale', () => {
    const sessao = sessaoCom(agoraEmSegundos() + 3600)
    guardarSessao(sessao)

    expect(getStoredSession()).toEqual(sessao)
  })

  it('descarta e remove do storage a sessão com token vencido', () => {
    guardarSessao(sessaoCom(agoraEmSegundos() - 60))

    expect(getStoredSession()).toBeNull()
    expect(localStorage.getItem(STORAGE_KEY)).toBeNull()
  })

  it('descarta JSON corrompido', () => {
    localStorage.setItem(STORAGE_KEY, '{quebrado')

    expect(getStoredSession()).toBeNull()
    expect(localStorage.getItem(STORAGE_KEY)).toBeNull()
  })
})

describe('expireSession', () => {
  afterEach(() => localStorage.clear())

  it('limpa o storage e avisa a aba atual pelo evento auth:session-cleared', () => {
    guardarSessao(sessaoCom(agoraEmSegundos() + 3600))
    const ouvinte = vi.fn()
    window.addEventListener(SESSION_CLEARED_EVENT, ouvinte)

    expireSession()

    expect(localStorage.getItem(STORAGE_KEY)).toBeNull()
    expect(ouvinte).toHaveBeenCalledTimes(1)
    window.removeEventListener(SESSION_CLEARED_EVENT, ouvinte)
  })
})
