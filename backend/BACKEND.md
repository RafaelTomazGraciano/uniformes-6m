# Backend

### Registrar o usuário

**api/usuario/registrar**

```json
{
    "nome": "teste",
    "email": "teste@teste.com",
    "senha": "123456"
}
```


### Login

**api/auth/login**

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
docker run -d --name uniform -e POSTGRES_DB=uniform -e POSTGRES_USER=uniform -e POSTGRES_PASSWORD=postgresUniform -p 5432:5432 postgres:18-alpine
```