# Itaú Challenge — API de Transações

API REST em **Java 21 + Spring Boot** que registra transações financeiras e calcula estatísticas das transações dos últimos 60 segundos, com autenticação por **JWT** e controle de acesso por perfis (`ADMIN` e `COMMON`).

O projeto parte do [desafio de programação do Itaú](https://github.com/feltex/desafio-itau-backend) e vai além dele: persiste os dados em PostgreSQL, tem autenticação e gestão de usuários, e é organizado em camadas (veja [Arquitetura](#arquitetura)).

## Sumário

- [Funcionalidades](#funcionalidades)
- [Diferenças em relação ao enunciado](#diferenças-em-relação-ao-enunciado)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Como executar](#como-executar)
- [Primeiros passos com a API](#primeiros-passos-com-a-api)
- [Autenticação e perfis](#autenticação-e-perfis)
- [Endpoints](#endpoints)
- [Regras de negócio](#regras-de-negócio)
- [Formato de erros](#formato-de-erros)
- [Banco de dados](#banco-de-dados)
- [Testes](#testes)
- [Integração contínua](#integração-contínua)
- [Limitações conhecidas e próximos passos](#limitações-conhecidas-e-próximos-passos)

## Funcionalidades

- **Transações:** cadastro, remoção e estatísticas (`count`, `sum`, `avg`, `min`, `max`) dos últimos 60 segundos.
- **Autenticação:** cadastro, login e renovação de token (access token + refresh token, ambos JWT).
- **Usuários:** consulta do próprio perfil e, para administradores, listagem paginada, consulta, desativação e remoção.
- **Documentação interativa** com Swagger UI e **health check** via Actuator.

## Diferenças em relação ao enunciado

O enunciado original é um ponto de partida; estas são as decisões que este projeto tomou de forma diferente:

| Enunciado | Este projeto |
|---|---|
| Transações guardadas **em memória**, sem banco de dados | **PostgreSQL** com migrations Flyway |
| Rotas em português: `/transacao` e `/estatistica` | `/api/transactions/v1` e `/api/transactions/v1/stats` |
| `DELETE /transacao` apaga **todas** as transações | `DELETE /api/transactions/v1/{id}` apaga **uma** transação |
| Dados inválidos respondem `422 Unprocessable Entity` | Dados inválidos de transação respondem `409 Conflict` (veja [Formato de erros](#formato-de-erros)) |
| Sem autenticação | Autenticação JWT e perfis de acesso em todas as rotas de negócio |

## Tecnologias

| Área | Tecnologia |
|---|---|
| Linguagem / build | Java 21, Maven (wrapper incluído) |
| Framework | Spring Boot 4.1.0 (Web MVC, Validation, Actuator) |
| Segurança | Spring Security, OAuth2 Resource Server (JWT HS256), BCrypt |
| Persistência | Spring Data JPA / Hibernate, PostgreSQL 16, Flyway |
| Documentação | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5, Spring Test / MockMvc, Spring Security Test, Testcontainers |
| CI | GitHub Actions |

## Arquitetura

O código segue uma arquitetura em camadas com **dependências apontando para dentro**:

```
interfaces ──► application ──► domain
                    ▲
infrastructure ─────┘   (implementa as portas definidas em application e domain)
```

| Camada | Responsabilidade |
|---|---|
| `domain` | Regras de negócio puras (entidades, validações, interfaces de repositório). Não depende de nenhum framework. |
| `application` | Casos de uso e portas (`IPasswordHasher`, `ITokenService`, `IAuthenticator`). Usa apenas anotações pontuais do Spring (`@Transactional`, `@Configuration`/`@Bean`). |
| `infrastructure` | Adaptadores: JPA e mappers, JWT, BCrypt, Spring Security, `UserDetailsService`. |
| `interfaces` | Camada HTTP: controllers, DTOs de entrada e saída e tratamento global de erros. |

```
src/main/java/io/github/williamandradesantana/itau_challenge
├── domain
│   ├── auth            # User, Permission, PasswordPolicy, repositórios
│   ├── transaction     # Transaction, TransactionStatistics, repositório
│   └── shared          # DomainException, paginação (PageQuery, PageResult)
├── application
│   ├── auth            # casos de uso de autenticação e de usuários
│   ├── transaction     # criar, remover e calcular estatísticas
│   └── shared          # exceções de aplicação
├── infrastructure
│   ├── persistence     # entidades JPA, mappers e repositórios (auth e transaction)
│   ├── security        # SecurityConfig, JWT, BCrypt, UserDetailsService
│   └── ...
└── interfaces
    ├── auth            # AuthController, UserController e DTOs
    ├── transaction     # TransactionController e DTOs
    └── shared          # GlobalExceptionHandler, ExceptionResponse, PageResponse
```

## Como executar

### Pré-requisitos

- **JDK 21**
- **Docker** (para o PostgreSQL e para os testes de integração)

### 1. Configurar as variáveis de ambiente

```bash
cp .env.example .env
```

Preencha o `.env` (ele está no `.gitignore` e **não deve ser commitado**):

| Variável | Descrição | Exemplo |
|---|---|---|
| `POSTGRES_DB` | Nome do banco | `itau_challenge` |
| `POSTGRES_HOST` | Host do banco | `localhost` |
| `POSTGRES_PORT` | Porta do banco | `5432` |
| `POSTGRES_USER` | Usuário do banco | `itau` |
| `POSTGRES_PASSWORD` | Senha do banco | *(defina a sua)* |
| `JWT_SECRET` | Chave de assinatura dos tokens: **Base64 de pelo menos 32 bytes** | gere com `openssl rand -base64 32` |
| `JWT_EXPIRATION_SECONDS` | Validade do access token | `3600` (1 hora) |
| `JWT_REFRESH_EXPIRATION_SECONDS` | Validade do refresh token | `604800` (7 dias) |
| `CORS_ALLOWED_ORIGINS` | Origens liberadas no CORS, separadas por vírgula | `http://localhost:3000` |

> O `.env.example` também lista variáveis terminadas em `_TEST`. Os testes de integração sobem o próprio PostgreSQL com Testcontainers, então rodar `mvn test` só exige o Docker em execução.

### 2. Subir o banco de dados

```bash
docker compose up -d
```

O `docker-compose.yaml` sobe um PostgreSQL 16 e lê as credenciais do `.env`.

### 3. Rodar a aplicação

O Spring Boot não lê o arquivo `.env` sozinho: carregue as variáveis no terminal antes de iniciar.

**Linux, macOS ou Git Bash:**

```bash
set -a && source .env && set +a
./mvnw spring-boot:run
```

**PowerShell:**

```powershell
Get-Content .env | Where-Object { $_ -match '^\s*[^#\s][^=]*=' } | ForEach-Object {
    $name, $value = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim())
}
./mvnw spring-boot:run
```

**IntelliJ IDEA:** informe as variáveis no campo *Environment variables* da configuração de execução (ou use um plugin de arquivo `.env`).

As migrations do Flyway rodam automaticamente na inicialização. A API sobe em `http://localhost:8080`.

| Recurso | URL |
|---|---|
| Health check | `http://localhost:8080/actuator/health` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI (JSON) | `http://localhost:8080/v3/api-docs` |

## Primeiros passos com a API

Exemplo completo com `curl` (o `jq` é usado só para extrair o token):

```bash
# 1. Cadastrar um usuário (recebe a permissão COMMON)
curl -i -X POST http://localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"userName":"maria","fullName":"Maria Silva","password":"Senha@123"}'

# 2. Fazer login e guardar o access token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"userName":"maria","password":"Senha@123"}' | jq -r .accessToken)

# 3. Registrar uma transação (data e hora atuais)
curl -i -X POST http://localhost:8080/api/transactions/v1 \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"amount\":123.45,\"dateTime\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}"

# 4. Consultar as estatísticas dos últimos 60 segundos
curl -s http://localhost:8080/api/transactions/v1/stats \
  -H "Authorization: Bearer $TOKEN"
```

## Autenticação e perfis

1. `POST /auth/login` devolve um **access token** e um **refresh token**.
2. As rotas protegidas exigem o header `Authorization: Bearer <accessToken>`.
3. Quando o access token expira, `POST /auth/refresh` troca o refresh token por um novo par de tokens.

| Detalhe | Comportamento |
|---|---|
| Tokens | JWT assinado com HS256; cada token leva o claim `token_use` (`access` ou `refresh`) |
| Refresh token como Bearer | **Recusado** (401): só o access token abre rotas protegidas |
| Access token em `/auth/refresh` | Recusado (401) |
| Permissões no token | O access token leva as permissões no claim `roles`; o refresh token não leva roles e, ao renovar, as permissões são lidas de novo do banco |
| Usuário desativado ou apagado | Não consegue fazer login nem renovar o token |
| Sessão | Sem sessão e sem cookies (stateless); CSRF desativado |

**Perfis:**

| Perfil | Pode |
|---|---|
| `COMMON` | Usar as rotas de transações e consultar o próprio perfil (`/me`) |
| `ADMIN` | Tudo o que o `COMMON` faz, mais gerir usuários |

O cadastro público sempre cria usuários `COMMON`: permissões enviadas no corpo da requisição são ignoradas.

## Endpoints

Rotas marcadas como **Público** não exigem token.

### Autenticação — `/auth`

| Método | Rota | Acesso | Resposta |
|---|---|---|---|
| `POST` | `/auth/register` | Público | `201` sem corpo |
| `POST` | `/auth/login` | Público | `200` com os tokens |
| `POST` | `/auth/refresh` | Público | `200` com os tokens |

**Cadastro** — `POST /auth/register`

```json
{ "userName": "maria", "fullName": "Maria Silva", "password": "Senha@123" }
```

**Login** — `POST /auth/login`

```json
{ "userName": "maria", "password": "Senha@123" }
```

Resposta (login e refresh têm o mesmo formato):

```json
{
  "authenticated": true,
  "tokenType": "Bearer",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresAt": "2025-03-17T20:40:00Z",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

`expiresAt` é o horário de expiração do **access token**.

**Renovação** — `POST /auth/refresh`

```json
{ "refreshToken": "eyJhbGciOiJIUzI1NiJ9..." }
```

### Usuários — `/api/users/v1`

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/me` | Qualquer usuário autenticado | Dados do próprio usuário |
| `GET` | `?page=0&size=20` | `ADMIN` | Lista paginada (`size` de 1 a 100) |
| `GET` | `/{id}` | `ADMIN` | Consulta um usuário |
| `PATCH` | `/{id}` | `ADMIN` | Desativa o usuário (`200`; idempotente) |
| `DELETE` | `/{id}` | `ADMIN` | Remove o usuário (`204`) |

Resposta de um usuário (a senha nunca é devolvida):

```json
{
  "id": "5b6f3f0e-8d57-4b4f-9d1e-2f0c9a1e7c11",
  "userName": "maria",
  "fullName": "Maria Silva",
  "enabled": true,
  "roles": ["COMMON"]
}
```

Resposta da listagem:

```json
{
  "content": [ { "id": "...", "userName": "maria", "fullName": "Maria Silva", "enabled": true, "roles": ["COMMON"] } ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "hasNext": false
}
```

A listagem vem ordenada por `userName` e inclui usuários desativados (`enabled: false`).

### Transações — `/api/transactions/v1`

Acesso: `ADMIN` ou `COMMON`.

| Método | Rota | Descrição | Resposta |
|---|---|---|---|
| `POST` | `/` | Registra uma transação | `201` sem corpo |
| `DELETE` | `/{id}` | Remove uma transação | `204` |
| `GET` | `/stats` | Estatísticas dos últimos 60 segundos | `200` |

**Registro** — `POST /api/transactions/v1`

```json
{ "amount": 123.45, "dateTime": "2025-03-17T17:40:00-03:00" }
```

**Estatísticas** — `GET /api/transactions/v1/stats`

```json
{
  "count": 10,
  "sum": 1234.56,
  "avg": 123.456,
  "min": 12.34,
  "max": 123.56
}
```

## Regras de negócio

**Usuário**

| Campo | Regra |
|---|---|
| `userName` | Obrigatório; de 3 a 30 caracteres (espaços nas pontas são removidos); único |
| `fullName` | Obrigatório; de 3 a 150 caracteres |
| `password` | Obrigatória; de 8 caracteres até 72 bytes (limite do BCrypt) |

**Transação**

| Campo | Regra |
|---|---|
| `amount` | Obrigatório; **maior ou igual a zero** (armazenado como `decimal(10,2)`) |
| `dateTime` | Obrigatório; **não pode estar no futuro** |

**Estatísticas**

- Consideram as transações cuja `dateTime` está dentro dos últimos **60 segundos**, medidos pelo relógio do servidor (UTC).
- Sem nenhuma transação na janela, todos os campos voltam `0`.

## Formato de erros

Os erros da aplicação seguem o formato:

```json
{
  "timestamp": "2025-03-17T20:40:00.123Z",
  "message": "UserName already exists: maria",
  "details": "uri=/auth/register"
}
```

| Status | Quando |
|---|---|
| `400` | JSON malformado ou ausente, campo obrigatório em branco (a mensagem lista os campos), regra de domínio violada, paginação inválida, parâmetro com tipo errado |
| `401` | Credenciais inválidas, refresh token inválido ou expirado, token ausente ou inválido |
| `403` | Usuário autenticado sem permissão para a rota |
| `404` | Recurso ou rota inexistente |
| `405` / `415` | Método HTTP ou `Content-Type` não suportado |
| `409` | Usuário já existente; transação com `amount` negativo ou `dateTime` inválido |
| `500` | Erro inesperado: a resposta traz uma mensagem genérica, e o detalhe vai só para o log |

Observações:

- A mensagem de falha de login é a mesma para usuário inexistente, senha errada e conta desativada, para não revelar quais usuários existem.
- As respostas `401` de rotas protegidas (token ausente ou inválido) são geradas pelo Spring Security e trazem o header `WWW-Authenticate: Bearer`.

## Banco de dados

O esquema é versionado com **Flyway** (`src/main/resources/db/migration`):

| Migration | Conteúdo |
|---|---|
| `V1` | Tabela de usuários |
| `V2` | Tabela de permissões (apenas `COMMON` e `ADMIN`) |
| `V3` | Vínculo entre usuários e permissões |
| `V4` | Cria as permissões `COMMON` e `ADMIN` |
| `V5` | Cria os usuários iniciais `admin` (perfil `ADMIN`) e `user` (perfil `COMMON`) |
| `V6` | Tabela de transações |

> Os usuários da `V5` existem em **qualquer ambiente** em que a migration rodar. Antes de publicar a aplicação, troque os hashes das senhas ou remova essa migration.

## Testes

```bash
./mvnw test                                # todos os testes (os de integração exigem o Docker em execução)
./mvnw test -DexcludedGroups=integration   # só os testes unitários, sem Docker
./mvnw test -Dtest='*ControllerIntegrationTest'
```

| Tipo | O que cobre |
|---|---|
| Unitários | Regras de domínio, casos de uso (com repositórios em memória em `src/test/.../support`), DTOs, JWT, BCrypt, `UserDetailsService`, authenticator, mappers e tratador global de erros |
| Integração | Repositórios JPA, migrations, montagem do contexto e a API inteira via HTTP (rotas, regras de segurança, tokens inválidos e CORS), todos contra um **PostgreSQL real** subido pelo Testcontainers |

- Os testes de integração são marcados com `@Tag("integration")` e usam o perfil `test` (`src/test/resources/application-test.yaml`).
- Os testes de autenticação e de usuários cobrem as quatro camadas. Os testes do módulo de transações ainda serão adicionados.

## Integração contínua

O workflow [`.github/workflows/ci.yaml`](.github/workflows/ci.yaml) roda a cada push ou pull request para `main` e `develop`: instala o JDK 21 (Temurin), executa `mvn -B clean verify` e publica os relatórios do Surefire como artefato.

## Limitações conhecidas e próximos passos

- **Testes do módulo de transações** ainda não foram escritos.
- **Janela das estatísticas fixa em 60 segundos.** Torná-la configurável é um dos extras sugeridos pelo enunciado.
- **O cadastro de transação não devolve o `id` nem o header `Location`**, e não há rota para listar transações. Hoje, para usar `DELETE /api/transactions/v1/{id}`, é preciso obter o `id` por fora (por exemplo, no banco).
- **Refresh token sem revogação.** É um JWT stateless: vale até expirar. Desativar um usuário bloqueia as renovações, mas o access token já emitido continua válido até expirar.
- **Swagger UI sem esquema Bearer configurado:** a página serve para consultar a documentação, mas o botão *Authorize* não existe. Para chamar rotas protegidas, use `curl`, Postman ou similar.
- **Dados inválidos de transação respondem `409`**, enquanto o enunciado original usa `422`.