# Foody — Rastreador de Pedidos

Aplicação full stack para acompanhar pedidos de delivery: cadastro e login de usuários, criação de pedidos, listagem paginada com busca e atualização de status com transições validadas.

O repositório é dividido em dois projetos independentes:

- **`order-tracking-server`** — API REST em Java + Spring Boot, autenticação JWT via cookie `HttpOnly` e persistência em SQLite.
- **`order-tracking-web`** — SPA em React + TypeScript + Vite, com Material UI, React Router, React Query e Axios.

---

## Sumário

- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Estrutura do repositório](#estrutura-do-repositório)
- [Pré-requisitos](#pré-requisitos)
- [Passo a passo](#passo-a-passo)
- [Usando a aplicação](#usando-a-aplicação)
- [API](#api)
- [Status dos pedidos](#status-dos-pedidos)
- [Configuração](#configuração)
- [Testes](#testes)
- [Solução de problemas](#solução-de-problemas)

---

## Funcionalidades

- Cadastro de usuário com nome, e-mail e senha (senha criptografada com BCrypt).
- Login com e-mail e senha, com sessão mantida por JWT em cookie `HttpOnly`.
- Rotas da API protegidas: apenas usuários autenticados acessam pedidos.
- Criação de pedido com cliente, endereço e um ou mais itens.
- Listagem paginada de pedidos, com busca por ID.
- Busca de pedido por ID.
- Atualização de status com validação das transições permitidas.
- Interface com tabela de pedidos, paginação, busca, criação em drawer e troca rápida de status.
- Testes automatizados no back-end e no front-end.

---

## Tecnologias

### Back-end

| Item | Versão / tecnologia |
| --- | --- |
| Java | 17 |
| Spring Boot | 3.5.16 |
| Segurança | Spring Security + JWT (`jjwt` 0.12.6) |
| Persistência | Spring Data JPA + Hibernate + SQLite (`sqlite-jdbc`) |
| Mapeamento | MapStruct 1.5.5 |
| Build | Maven (wrapper incluído, Maven 3.10.0) |
| Testes | JUnit + Spring Boot Test + Mockito |

### Front-end

| Item | Versão / tecnologia |
| --- | --- |
| React | 19 |
| TypeScript | 6 |
| Build/dev server | Vite 8 |
| Componentes | Material UI 9 |
| Roteamento | React Router 7 |
| Dados/HTTP | TanStack React Query 5 + Axios |
| Formulários | React Hook Form |
| Testes | Vitest + Testing Library |

---

## Estrutura do repositório

```text
foody-order-tracking/
├── order-tracking-server/        # API REST (Spring Boot)
│   ├── src/main/java/com/foody/order_tracking_server/
│   │   ├── config/               # Segurança e CORS
│   │   ├── controller/           # Endpoints REST
│   │   ├── dto/                  # Contratos de entrada/saída
│   │   ├── exception/            # Tratamento global de erros
│   │   ├── mappers/              # MapStruct
│   │   ├── model/                # Entidades JPA
│   │   ├── repository/           # Repositórios Spring Data
│   │   ├── security/             # Filtro JWT
│   │   └── service/              # Regras de negócio
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── application-dev.properties
│   ├── src/test/                 # Testes unitários e de integração
│   ├── mvnw / mvnw.cmd           # Maven Wrapper
│   └── pom.xml
├── order-tracking-web/           # SPA (React + Vite)
│   ├── src/
│   │   ├── api/                  # Chamadas HTTP
│   │   ├── components/           # Tabela, drawer, diálogo de status...
│   │   ├── context/              # Auth e snackbar
│   │   ├── layouts/              # Layout autenticado
│   │   ├── lib/                  # Axios e React Query
│   │   ├── models/               # Tipos e metadados de status
│   │   ├── pages/                # Login e Home
│   │   ├── test/                 # Testes com Vitest/RTL
│   │   └── routes.tsx
│   ├── package.json
│   └── vite.config.ts
└── README.md
```

---

## Pré-requisitos

- **Java 17** ou superior (JDK instalado e `java -version` funcionando).
- **Node.js 20.19+** ou **22.12+** (exigência do Vite 8) e **npm**.
- **Git**.
- Portas **8080** (API) e **5173** (front-end) livres.
- Não é necessário ter Maven instalado: o projeto inclui o Maven Wrapper (`mvnw` / `mvnw.cmd`). Na primeira execução ele baixa o Maven 3.10.0 automaticamente.

Para conferir as versões:

```bash
java -version
node -v
npm -v
```

---

## Passo a passo

### 1. Obter o projeto

```bash
git clone <URL_DO_REPOSITORIO>
cd foody-order-tracking
```

### 2. Subir o back-end

Abra um terminal e entre na pasta do servidor:

```bash
cd order-tracking-server
```

O servidor exige um segredo JWT com **pelo menos 32 caracteres**. Para desenvolvimento local, defina também `COOKIE_SECURE=false` para que o cookie de sessão funcione via HTTP.

**Windows (PowerShell):**

```powershell
$env:JWT_SECRET = "foody-dev-secret-com-pelo-menos-32-caracteres"
$env:COOKIE_SECURE = "false"
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**

```bash
export JWT_SECRET="foody-dev-secret-com-pelo-menos-32-caracteres"
export COOKIE_SECURE="false"
./mvnw spring-boot:run
```

Aguarde a mensagem `Tomcat started on port 8080` (ou `Started OrderTrackingServerApplication`).

O que acontece nesse passo:

- O banco SQLite é criado/atualizado automaticamente em `order-tracking-server/database.db` (`ddl-auto=update`).
- O CORS libera por padrão a origem `http://localhost:5173`.
- O cookie de autenticação é enviado com `HttpOnly`, `SameSite=Strict` e expiração padrão de 7 dias.

Para confirmar que a API está no ar, execute de outro terminal:

```bash
curl -i http://localhost:8080/api/order
```

A resposta esperada é **401 Unauthorized**, pois a rota exige autenticação. Isso é normal e indica que a API está respondendo.

### 3. Subir o front-end

Abra **outro terminal** e entre na pasta web:

```bash
cd order-tracking-web
npm install
npm run dev
```

Acesse: **http://localhost:5173**

O Vite está configurado para encaminhar chamadas para `/api` a `http://localhost:8080`, então front-end e API funcionam como se estivessem na mesma origem — sem necessidade de configurar URL da API.

### 4. Fazer login e usar o sistema

1. Na tela inicial, use a aba **Cadastrar** para criar um usuário (nome, e-mail e senha de 8 a 72 caracteres).
2. Após o cadastro, volte para a aba **Entrar** e faça login.
3. Na tela **Home**, clique em **Novo pedido** para criar um pedido com cliente, endereço e itens.
4. Atualize o status pelas ações da tabela ou pelo diálogo de status.
5. Use a busca por ID e a paginação para localizar pedidos.

O usuário de teste não é pré-criado — o primeiro cadastro deve ser feito pela própria interface.

---

## Usando a aplicação

- **Login/cadastro:** `/login`
- **Listagem de pedidos:** `/home` (protegida; redireciona para o login se não autenticado)
- **Criar pedido:** botão **Novo pedido** no topo da tabela.
- **Buscar:** o campo de busca procura por **ID do pedido**. Texto não numérico retorna lista vazia.
- **Alterar status:** ações rápidas na linha ou diálogo de status, respeitando as transições permitidas.
- **Sair:** botão de logout no cabeçalho; o cookie é removido e a sessão no front-end é encerrada.

---

## API

**Base URL:** `http://localhost:8080`

A autenticação é feita por cookie `access_token` retornado no login. Em ferramentas como Postman/Insomnia, habilite o armazenamento de cookies. Em `curl`, use `-c` para salvar e `-b` para enviar o cookie.

| Método | Rota | Descrição | Autenticação |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Cadastra um usuário | Pública |
| `POST` | `/api/auth/login` | Autentica e define o cookie de sessão | Pública |
| `POST` | `/api/auth/logout` | Remove o cookie de sessão | Pública |
| `GET` | `/api/order` | Lista pedidos com paginação e busca por ID | Obrigatória |
| `GET` | `/api/order/{id}` | Busca um pedido por ID | Obrigatória |
| `POST` | `/api/order` | Cria um pedido | Obrigatória |
| `PATCH` | `/api/order/{id}/status` | Atualiza o status do pedido | Obrigatória |

### Exemplos

#### Cadastrar usuário

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Ana Silva",
  "email": "ana@example.com",
  "password": "senha123"
}
```

Resposta: **201 Created**

```json
{ "message": "Success" }
```

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "ana@example.com",
  "password": "senha123"
}
```

Resposta: **200 OK** com o cabeçalho `Set-Cookie: access_token=...`.

Via `curl` (salva o cookie em `cookies.txt`):

```bash
curl -i -c cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@example.com","password":"senha123"}' \
  http://localhost:8080/api/auth/login
```

> No PowerShell, use `curl.exe` em vez de `curl` para evitar o alias do `Invoke-WebRequest`.

#### Criar pedido

```http
POST /api/order
Content-Type: application/json
Cookie: access_token=...

{
  "client": "Maria Souza",
  "address": "Rua das Flores, 123 - São Paulo/SP",
  "items": [
    {
      "name": "Pizza Margherita",
      "quantity": 1,
      "unitPrice": 49.90
    },
    {
      "name": "Refrigerante 2L",
      "quantity": 2,
      "unitPrice": 12.50
    }
  ]
}
```

Resposta: **201 Created**

```json
{
  "id": 1,
  "client": "Maria Souza",
  "address": "Rua das Flores, 123 - São Paulo/SP",
  "status": "RECEIVED",
  "createdAt": "2026-10-21T19:00:00",
  "items": [
    {
      "id": 1,
      "name": "Pizza Margherita",
      "unitPrice": 49.90,
      "quantity": 1,
      "orderId": 1
    },
    {
      "id": 2,
      "name": "Refrigerante 2L",
      "unitPrice": 12.50,
      "quantity": 2,
      "orderId": 1
    }
  ]
}
```

Via `curl`:

```bash
curl -b cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"client":"Maria Souza","address":"Rua das Flores, 123","items":[{"name":"Pizza Margherita","quantity":1,"unitPrice":49.90}]}' \
  http://localhost:8080/api/order
```

#### Listar pedidos

```http
GET /api/order?page=0&size=10&search=1
```

- `page`: página (começa em 0).
- `size`: itens por página.
- `search`: opcional; busca pelo ID do pedido.
- Ordenação padrão: `id` ascendente.

A resposta segue o formato de página do Spring Data:

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "number": 0,
  "size": 10
}
```

#### Buscar pedido por ID

```http
GET /api/order/1
```

#### Atualizar status

```http
PATCH /api/order/1/status
Content-Type: application/json

{
  "status": "IN_PREPARATION"
}
```

Resposta: **200 OK** com o pedido atualizado.

#### Logout

```http
POST /api/auth/logout
```

Resposta: **200 OK** e cookie expirado.

### Formato de erro

Erros seguem o contrato:

```json
{
  "code": "INVALID_STATUS_TRANSITION",
  "message": "Transição de status inválida: DELIVERED -> RECEIVED"
}
```

Principais códigos:

| HTTP | Código | Situação |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | Dados inválidos no corpo da requisição |
| 401 | `INVALID_CREDENTIALS` | E-mail ou senha incorretos |
| 401 | `UNAUTHORIZED` | Requisição sem autenticação válida |
| 404 | `ORDER_NOT_FOUND` | Pedido não encontrado |
| 409 | `EMAIL_ALREADY_EXISTS` | E-mail já cadastrado |
| 409 | `INVALID_STATUS_TRANSITION` | Transição de status não permitida |
| 409 | `DATA_CONFLICT` | Conflito de integridade no banco |

---

## Status dos pedidos

Os valores usados na API estão em inglês; a interface traduz os rótulos para português.

| API | Interface | Pode avançar para |
| --- | --- | --- |
| `RECEIVED` | Recebido | `IN_PREPARATION`, `CANCELED` |
| `IN_PREPARATION` | Em preparo | `OUT_FOR_DELIVERY`, `CANCELED` |
| `OUT_FOR_DELIVERY` | Saiu para entrega | `DELIVERED`, `CANCELED` |
| `DELIVERED` | Entregue | — (final) |
| `CANCELED` | Cancelado | — (final) |

Transições fora dessas regras retornam **409 Conflict**.

---

## Configuração

### Back-end

As configurações ficam em `order-tracking-server/src/main/resources/application.properties` e podem ser sobrescritas por variáveis de ambiente.

| Variável / propriedade | Padrão | Descrição |
| --- | --- | --- |
| `JWT_SECRET` / `jwt.secret` | *(vazio)* | Segredo HMAC para assinar o JWT. **Obrigatório**, mínimo de 32 caracteres. |
| `JWT_EXPIRATION_MS` / `jwt.expiration-ms` | `604800000` | Validade do token/cookie (7 dias). |
| `COOKIE_SECURE` / `cookie.secure` | `true` | Use `false` em HTTP local; mantenha `true` em produção com HTTPS. |
| `SPRING_DATASOURCE_URL` / `spring.datasource.url` | `jdbc:sqlite:database.db` | Caminho do banco SQLite, relativo ao diretório de trabalho do processo. |
| `APP_CORS_ALLOWED_ORIGINS` / `app.cors.allowed-origins` | `http://localhost:5173` | Origens permitidas, separadas por vírgula. Não aceita `*`. |

> **Atenção:** ao rodar sem o profile `dev`, o `cookie.secure` padrão é `true`. Em HTTP local, defina `COOKIE_SECURE=false`, como nos comandos acima.

### Profile `dev`

O arquivo `application-dev.properties` já existe e ajusta o ambiente local:

- `cookie.secure=false`
- logs de segurança em `DEBUG`
- banco em `${user.dir}/order-tracking-server/database.db`

Como o caminho do banco depende de `${user.dir}`, esse profile pressupõe que o processo seja iniciado com o diretório de trabalho na **raiz do repositório**. Ao rodar de dentro de `order-tracking-server`, o caminho resultaria em `order-tracking-server/order-tracking-server/database.db` (pasta inexistente) e a aplicação falharia com `SQLITE_CANTOPEN`.

Se quiser usar o profile `dev` a partir da pasta do servidor:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.workingDirectory=..
```

E garanta um `JWT_SECRET` válido (o valor no arquivo de profile pode não estar presente em um clone novo do repositório).

### Front-end

O Axios usa `VITE_API_URL` quando definida; se estiver vazia, as requisições usam caminhos relativos (`/api/...`) e o proxy do Vite encaminha para `http://localhost:8080`.

Para apontar para outra API, crie `order-tracking-web/.env.local`:

```bash
VITE_API_URL=http://localhost:8080
```

Nesse caso, inclua a origem do front-end em `APP_CORS_ALLOWED_ORIGINS` no back-end.

---

## Testes

### Back-end

```bash
cd order-tracking-server
```

Windows:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

Os testes cobrem controllers, services, segurança/JWT, mappers, repositórios e fluxos de integração. A suíte usa SQLite em memória (`src/test/resources/application-test.properties`), sem afetar o banco de desenvolvimento.

### Front-end

```bash
cd order-tracking-web
npm run test:run     # executa os testes uma vez
npm run test         # modo watch
npm run lint         # ESLint
npm run build        # type-check + build de produção
```

---

## Solução de problemas

### `401 Unauthorized` mesmo após o login

- Confirme que o login retornou `Set-Cookie: access_token`.
- Verifique se `COOKIE_SECURE` está como `false` ao rodar em HTTP local.
- Use sempre o mesmo host (`localhost` e `127.0.0.1` são origens diferentes para cookies).
- Em Postman/Insomnia, habilite o gerenciamento de cookies.
- Faça logout e login novamente para renovar o token.

### `APPLICATION FAILED TO START` / `Port 8080 was already in use`

Já existe outro processo usando a porta 8080. Encerre-o ou suba a API em outra porta:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

Nesse caso, ajuste o proxy em `order-tracking-web/vite.config.ts` ou defina `VITE_API_URL=http://localhost:8081`.

### `WeakKeyException` ou falha ao iniciar o `JwtService`

O `JWT_SECRET` está vazio ou tem menos de 32 caracteres. Defina um segredo com 32+ caracteres antes de iniciar a API.

### `SQLITE_CANTOPEN: unable to open database file`

- O diretório informado no caminho do banco não existe.
- Rodando com o profile `dev` de dentro de `order-tracking-server`, use `-Dspring-boot.run.workingDirectory=..` ou siga o passo 2 sem profile.
- Confira permissão de escrita na pasta do banco.

### `SQLITE_BUSY` / banco bloqueado

Outra instância da API está usando o mesmo arquivo SQLite. Encerre a outra instância antes de subir uma nova.

### Erro de CORS no navegador

Adicione a origem exata do front-end (protocolo + host + porta) em `APP_CORS_ALLOWED_ORIGINS`:

```bash
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

Não use `*`: a aplicação rejeita wildcard porque as requisições usam credenciais.

### Front-end sem resposta da API

- Confirme que o back-end está rodando em `http://localhost:8080`.
- Verifique o terminal do Vite para erros de proxy.
- Se `VITE_API_URL` estiver definida, confirme que a URL está correta e que o CORS permite a origem.

### Resetar o banco de desenvolvimento

Pare a API, apague `order-tracking-server/database.db` e inicie novamente. As tabelas serão recriadas automaticamente. **Isso apaga todos os usuários e pedidos locais.**

---

## Observações de implementação

- A API é stateless: o JWT vai em cookie `HttpOnly` e é validado por um filtro em cada requisição.
- Senhas são armazenadas com hash BCrypt; nunca em texto puro.
- O CSRF está desabilitado por se tratar de API stateless com cookie `SameSite=Strict`.
- O front-end mantém apenas um indicador de sessão em `localStorage`; a autorização real acontece na API.
- Não há papéis/permissões: qualquer usuário autenticado pode criar, listar e atualizar pedidos.
- Não há endpoints de exclusão ou edição completa de pedidos; apenas criação, consulta e mudança de status.
