# Backend

Java 25

## Endpoints

Os endpoints da API estão disponíveis em **http://localhost:8080/api/**

### Escola

GET - buscar todas as escolas
**escola?page=0&size=10**

GET - buscar uma escola pelo id
**escola/{id}**

### Usuário

#### Registrar o usuário

POST - registrar o usuário
**usuario/registrar**

```json
{
    "nome": "teste",
    "email": "teste@teste.com",
    "senha": "123456"
}
```

#### Atualizar o usuário

PUT - atualizar o usuário
**usuario/atualizar**

```json
{
    "nome": "testeAtualizado",
    "email": "testeAtualizado@email.com"
}
```

O email será pego do token, então não precisa passar o email no body.

#### Deletar o usuário

DELETE - deletar o usuário
**usuario/deletar**

Ele passa o jwt no header, já que usuário estará autenticado, pois o usuário pode deletar seu próprio registro, então não precisa passar o id do usuário.

### Auth

#### Login

POST - login do usuário
**auth/login**

```json
{
    "email": "teste@teste.com",
    "senha": "123456"
}
```

# Implementação 

```java
@PostMapping
public ResponseEntity<?> criar(@RequestBody PedidoRequestDTO dto,
                                @AuthenticationPrincipal Usuario usuario) {
    // usuario vem do token, não do DTO
    pedidoService.criar(dto, usuario);
}
```


```json
"dataEfetivada": "2025-06-17T14:30:00"
```

Docker PostgreSQL:

```bash
docker run -d --name uniform -e POSTGRES_DB=uniform -e POSTGRES_USER=uniform -e POSTGRES_PASSWORD=postgresUniform -p 5432:5432 -v uniform_data:/var/lib/postgresql postgres:18-alpine
```