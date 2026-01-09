# Banking API

API RESTful para lancamentos de debito/credito em contas e consulta de saldo.

## Requisitos atendidos
- Lancamentos de debito e credito em conta especifica, inclusive em lote
- Consulta de saldo por conta
- Concorrencia segura com lock pessimista 
- Documentacao via Swagger/OpenAPI
- Testes unitarios, integracao e concorrencia

## Stack
- Java 21
- Spring Boot 4
- Spring Web, Validation, Data JPA
- Hibernate
- H2 (runtime)
- Springdoc OpenAPI (Swagger UI)

## Como rodar
```bash
./mvnw spring-boot:run
```

## Swagger
- UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI: http://localhost:8080/v3/api-docs

## H2 Console
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:bankingApi
- User: sa
- Password: (vazio)

## Endpoints
### Criar conta
`POST /accounts`

Request:
```json
{
  "initialBalance": 100.00
}
```

Response (201):
```json
{
  "accountId": "8d6c5f8c-8f5e-4a31-8f4a-0b01b2a1c9a1",
  "balance": 100.00
}
```

### Aplicar lancamentos (lote)
`POST /accounts/{accountId}/entries`

Request:
```json
{
  "entries": [
    { "type": "DEBIT", "amount": 40.00, "description": "debit" },
    { "type": "CREDIT", "amount": 10.00, "description": "credit" }
  ]
}
```

Response (201):
```json
{
  "accountId": "8d6c5f8c-8f5e-4a31-8f4a-0b01b2a1c9a1",
  "balance": 70.00,
  "entries": [
    {
      "id": "e8f3d0c0-5b9a-4e39-9b0a-7b9c9fbd12a1",
      "type": "DEBIT",
      "amount": 40.00,
      "description": "debit",
      "occurredAt": "2026-01-09T19:00:00Z"
    },
    {
      "id": "a1c2b3d4-e5f6-7890-1234-56789abcdef0",
      "type": "CREDIT",
      "amount": 10.00,
      "description": "credit",
      "occurredAt": "2026-01-09T19:00:00Z"
    }
  ]
}
```

### Obter saldo
`GET /accounts/{accountId}/balance`

Response (200):
```json
{
  "accountId": "8d6c5f8c-8f5e-4a31-8f4a-0b01b2a1c9a1",
  "balance": 70.00
}
```

## Testes
```bash
./mvnw test
```

## Observacoes de concorrencia
- O saldo e atualizado dentro de transacao
- A consulta da conta usa lock pessimista (`findByIdForUpdate`)
- Ha teste de concorrencia em `AccountConcurrencyTest`
# bankingApi2
