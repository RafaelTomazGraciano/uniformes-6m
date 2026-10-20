#!/usr/bin/env node
/*
 * Popula a API com dados de demonstração: um usuário, tipos de uniforme (com
 * limite de estoque baixo), turmas, alunos, entradas de estoque e entregas.
 *
 *   node backend/scripts/seed.mjs            # contra http://localhost:8080/api
 *   SEED_API_URL=http://host:8080/api node backend/scripts/seed.mjs
 *
 * Usa só a API pública, então passa pelas mesmas validações do sistema.
 * Se já houver tipos de uniforme cadastrados, não faz nada (use --force para ignorar).
 */

const API = process.env.SEED_API_URL ?? 'http://localhost:8080/api'
const EMAIL = process.env.SEED_EMAIL ?? 'admin@uniformes6m.local'
const SENHA = process.env.SEED_SENHA ?? 'senha123'
const FORCAR = process.argv.includes('--force')

let token = ''

async function api(caminho, { method = 'GET', body } = {}) {
  const resposta = await fetch(`${API}${caminho}`, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  const texto = await resposta.text()
  const dados = texto ? JSON.parse(texto) : null
  if (!resposta.ok) {
    const erro = new Error(`${method} ${caminho} → ${resposta.status}: ${dados?.message ?? texto}`)
    erro.status = resposta.status
    throw erro
  }
  return dados
}

/* Gerador determinístico: rodar duas vezes num banco limpo dá o mesmo resultado. */
let semente = 42
function aleatorio() {
  semente = (semente * 1664525 + 1013904223) % 4294967296
  return semente / 4294967296
}
const escolher = (lista) => lista[Math.floor(aleatorio() * lista.length)]
const entre = (min, max) => min + Math.floor(aleatorio() * (max - min + 1))

function dataLocal(diasAtras, hora = 10) {
  const data = new Date()
  data.setDate(data.getDate() - diasAtras)
  data.setHours(hora, entre(0, 59), 0, 0)
  const dois = (valor) => String(valor).padStart(2, '0')
  return `${data.getFullYear()}-${dois(data.getMonth() + 1)}-${dois(data.getDate())}T${dois(data.getHours())}:${dois(data.getMinutes())}:00`
}

function chaveNfe() {
  return Array.from({ length: 44 }, () => entre(0, 9)).join('')
}

const TIPOS = [
  { tipo: 'Camiseta manga curta', estoqueBaixo: 5 },
  { tipo: 'Bermuda', estoqueBaixo: 4 },
  { tipo: 'Short-saia', estoqueBaixo: 3 },
  { tipo: 'Jaqueta de agasalho', estoqueBaixo: 2 },
  { tipo: 'Calça de agasalho', estoqueBaixo: 2 },
]

const TURMAS = [
  { nome: '6º ano A', turno: 'DIURNO', ensino: 'FUNDAMENTAL', alunos: 9 },
  { nome: '6º ano B', turno: 'VESPERTINO', ensino: 'FUNDAMENTAL', alunos: 8 },
  { nome: '7º ano A', turno: 'DIURNO', ensino: 'FUNDAMENTAL', alunos: 9 },
  { nome: '8º ano A', turno: 'DIURNO', ensino: 'FUNDAMENTAL', alunos: 7 },
  { nome: '9º ano A', turno: 'DIURNO', ensino: 'FUNDAMENTAL', alunos: 0 },
  { nome: '1º ano Médio', turno: 'VESPERTINO', ensino: 'MEDIO', alunos: 8 },
  { nome: 'Técnico em Administração', turno: 'NOTURNO', ensino: 'TECNICO', alunos: 5 },
]

const PRENOMES = ['Ana', 'Beatriz', 'Carla', 'Daniela', 'Eduarda', 'Fernanda', 'Gabriela', 'Helena', 'Isabela', 'Júlia', 'Larissa', 'Mariana', 'Natália', 'Olívia', 'Rafaela', 'Sofia', 'Arthur', 'Bruno', 'Caio', 'Davi', 'Enzo', 'Felipe', 'Gustavo', 'Heitor', 'Igor', 'João', 'Lucas', 'Miguel', 'Pedro', 'Rafael', 'Samuel', 'Thiago']
const SOBRENOMES = ['Almeida', 'Barbosa', 'Cardoso', 'Costa', 'Ferreira', 'Gomes', 'Lima', 'Martins', 'Mendes', 'Nunes', 'Oliveira', 'Pereira', 'Ribeiro', 'Rocha', 'Santos', 'Silva', 'Souza', 'Teixeira']

/* Grade de cada entrada: [tipo, sexo, [PP, P, M, G, GG]]. */
const TAMANHOS = ['PP', 'P', 'M', 'G', 'GG']
const LOTES = [
  {
    fornecedor: 'Confecções Aurora',
    diasAtras: 75,
    grade: [
      ['Camiseta manga curta', 'FEMININO', [6, 14, 20, 12, 4]],
      ['Camiseta manga curta', 'MASCULINO', [4, 12, 22, 16, 8]],
      ['Bermuda', 'MASCULINO', [3, 10, 14, 10, 5]],
      ['Short-saia', 'FEMININO', [4, 9, 10, 6, 2]],
    ],
    /* A nota trouxe a camiseta feminina M em duas linhas: o backend soma. */
    repetida: ['Camiseta manga curta', 'FEMININO', 'M', 6],
  },
  {
    fornecedor: 'Malharia Horizonte',
    diasAtras: 30,
    grade: [
      ['Jaqueta de agasalho', 'FEMININO', [2, 5, 6, 4, 2]],
      ['Jaqueta de agasalho', 'MASCULINO', [2, 4, 7, 5, 3]],
      ['Calça de agasalho', 'FEMININO', [1, 4, 5, 3, 1]],
      ['Calça de agasalho', 'MASCULINO', [1, 3, 5, 4, 2]],
      ['Bermuda', 'FEMININO', [2, 5, 6, 4, 1]],
    ],
  },
]

async function autenticar() {
  try {
    await api('/usuario/registrar', { method: 'POST', body: { nome: 'Marina Lopes', email: EMAIL, senha: SENHA } })
    console.log(`• usuário criado: ${EMAIL}`)
  } catch (erro) {
    if (erro.status !== 400 && erro.status !== 409) throw erro
    console.log(`• usuário já existia: ${EMAIL}`)
  }
  token = (await api('/auth/login', { method: 'POST', body: { email: EMAIL, senha: SENHA } })).token
}

async function main() {
  console.log(`Seed em ${API}`)
  await autenticar()

  const existentes = await api('/tipo-uniforme?size=1')
  if (existentes.totalElements > 0 && !FORCAR) {
    console.log('• já há tipos de uniforme cadastrados; nada a fazer (use --force para semear mesmo assim).')
    return
  }

  const tipos = {}
  for (const tipo of TIPOS) {
    tipos[tipo.tipo] = (await api('/tipo-uniforme', { method: 'POST', body: tipo })).id
  }
  console.log(`• ${TIPOS.length} tipos de uniforme`)

  const alunos = []
  const usados = new Set()
  for (const turma of TURMAS) {
    const criada = await api('/turma', { method: 'POST', body: { nome: turma.nome, turno: turma.turno, ensino: turma.ensino } })
    for (let indice = 0; indice < turma.alunos; indice++) {
      let nome
      do nome = `${escolher(PRENOMES)} ${escolher(SOBRENOMES)} ${escolher(SOBRENOMES)}`
      while (usados.has(nome))
      usados.add(nome)
      const comContato = aleatorio() < 0.6
      const aluno = await api('/aluno', {
        method: 'POST',
        body: {
          nome,
          turmaId: criada.id,
          ...(comContato
            ? { nomeResponsavel: `${escolher(PRENOMES)} ${nome.split(' ').at(-1)}`, telefoneResponsavel: `119${entre(10000000, 99999999)}` }
            : {}),
        },
      })
      alunos.push(aluno)
    }
  }
  console.log(`• ${TURMAS.length} turmas e ${alunos.length} alunos`)

  for (const lote of LOTES) {
    const itens = lote.grade.flatMap(([tipo, sexo, quantidades]) =>
      quantidades
        .map((quantidade, indice) => ({ tipoUniformeId: tipos[tipo], sexo, tamanho: TAMANHOS[indice], quantidade }))
        .filter((item) => item.quantidade > 0),
    )
    if (lote.repetida) {
      const [tipo, sexo, tamanho, quantidade] = lote.repetida
      itens.push({ tipoUniformeId: tipos[tipo], sexo, tamanho, quantidade })
    }
    await api('/lote', {
      method: 'POST',
      body: { chaveAcesso: chaveNfe(), fornecedor: lote.fornecedor, dataEntrega: dataLocal(lote.diasAtras, 9), itens },
    })
  }
  console.log(`• ${LOTES.length} entradas de estoque`)

  /* Entregas: cada aluno com 1–3 peças, às vezes a mesma peça em duas linhas. */
  const estoque = new Map((await api('/uniforme?size=500')).content.map((uniforme) => [uniforme.id, { ...uniforme }]))
  const disponiveis = () => [...estoque.values()].filter((uniforme) => uniforme.quantidade > 0)
  let entregas = 0
  for (const aluno of alunos.slice(0, 32)) {
    const itens = []
    for (let peca = 0; peca < entre(1, 3); peca++) {
      const opcoes = disponiveis()
      if (!opcoes.length) break
      const uniforme = escolher(opcoes)
      const quantidade = Math.min(uniforme.quantidade, entre(1, 2))
      uniforme.quantidade -= quantidade
      itens.push({ uniformeId: uniforme.id, quantidade })
      if (aleatorio() < 0.2 && uniforme.quantidade > 0) {
        uniforme.quantidade -= 1
        itens.push({ uniformeId: uniforme.id, quantidade: 1 })
      }
    }
    if (!itens.length) continue
    await api('/pedido', { method: 'POST', body: { alunoId: aluno.id, dataEfetivada: dataLocal(entre(0, 70), entre(8, 17)), itens } })
    entregas += 1
  }
  console.log(`• ${entregas} entregas`)
  console.log(`\nPronto. Entre com ${EMAIL} / ${SENHA}`)
}

main().catch((erro) => {
  console.error(`\nSeed falhou: ${erro.message}`)
  process.exit(1)
})
