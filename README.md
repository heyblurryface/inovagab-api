# InovaGAB API — Backend (Sprint 2)

Backend REST da plataforma de inovação **InovaGAB**, desenvolvido para o
**Challenge Grupo Águia Branca / FIAP (2026)**. Substitui o Firebase da Sprint 1 por uma API
própria com autenticação JWT, controle de acesso por perfil, MongoDB e integração com IA generativa.

| Camada | Tecnologia |
|---|---|
| Linguagem / framework | Java 17 · Spring Boot 3.3 |
| Segurança | Spring Security · JWT (jjwt 0.12) · BCrypt · `@PreAuthorize` por role |
| Banco | MongoDB (Spring Data MongoDB) |
| IA (Plus) | Google Gemini API (`generateContent`) — pontuação e priorização automática de ideias |
| Documentação | Swagger UI (springdoc-openapi) em `/swagger-ui.html` |
| Testes | JUnit 5 (regras de negócio: KPIs e avaliador heurístico) |
| Infra | Docker / docker-compose (Mongo + API) |

## 1. Como executar

### Pré-requisitos
- JDK 17+ e Maven 3.9+ (ou use o Docker abaixo, que dispensa os dois)
- MongoDB 6+ local **ou** um cluster gratuito no [MongoDB Atlas](https://www.mongodb.com/atlas)
- (Opcional) chave gratuita da Gemini API em <https://aistudio.google.com/apikey>

### Opção A — Docker (mais simples)
```bash
cp .env.example .env          # edite GEMINI_API_KEY se quiser IA real
docker compose up --build
```
API em `http://localhost:8080` · Swagger em `http://localhost:8080/swagger-ui.html`.

### Opção B — Maven local
```bash
# 1) suba um Mongo local (ou aponte MONGODB_URI para o Atlas)
docker run -d --name inovagab-mongo -p 27017:27017 mongo:7

# 2) variáveis (Windows PowerShell: $env:GEMINI_API_KEY="...")
export MONGODB_URI=mongodb://localhost:27017/inovagab
export GEMINI_API_KEY=SUA_CHAVE   # opcional

# 3) rode
mvn spring-boot:run
```

### Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/inovagab` | String de conexão (local ou Atlas) |
| `JWT_SECRET` | chave de desenvolvimento | Segredo HS256 — mínimo 32 caracteres, troque em produção |
| `JWT_EXPIRACAO_HORAS` | `12` | Validade do token |
| `GEMINI_API_KEY` | vazio | Chave da Gemini API. Sem ela, a IA usa o fallback heurístico |
| `GEMINI_MODEL` | `gemini-3.6-flash` | Modelo Gemini. Aceita **lista separada por vírgula** (ex.: `gemini-3.6-flash,gemini-3.6-flash-lite`): a cota gratuita é contada por modelo, então o seguinte assume quando o anterior esgota a cota do dia |
| `IA_FALLBACK_HEURISTICA` | `true` | Se `false`, a API retorna 503 quando a Gemini não estiver disponível |
| `SEED_ENABLED` | `true` | Popula usuários/dados de exemplo na primeira execução |
| `PORT` | `8080` | Porta HTTP |

### Usuários de teste (criados pelo seed — senha `123456`)

| E-mail | Perfil |
|---|---|
| operador@inovagab.com | operador |
| gestor@inovagab.com | gestor |
| lider@inovagab.com | lider |

O seed também cria 2 orientações estratégicas, 3 ideias, 3 projetos e o catálogo de recompensas,
para o dashboard já exibir dados na apresentação.

### Testes
```bash
mvn test
```

## 2. Arquitetura

```
┌────────────────────────────────────────────────────────────────────┐
│  App Android (Kotlin / Compose)  ──── Retrofit + JWT ────▶ REST    │
└────────────────────────────────────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼─────────────────────────────────┐
│  controller/   Endpoints REST · validação (Bean Validation)        │
│                autorização por role (@PreAuthorize)                │
├────────────────────────────────────────────────────────────────────┤
│  service/      Regras de negócio · gamificação (PontosService)     │
│                relatórios (Kpis) · orquestração da IA              │
├────────────────────────────────────────────────────────────────────┤
│  ia/           GeminiClient (RestClient) · IaService (prompt/JSON) │
│                AvaliadorHeuristico (fallback local)                │
├────────────────────────────────────────────────────────────────────┤
│  repository/   Spring Data MongoDB (MongoRepository + MongoTemplate)│
├────────────────────────────────────────────────────────────────────┤
│  domain/       Documentos: Usuario, Orientacao, Ideia, Projeto,    │
│                Recompensa, Resgate · enums · AvaliacaoIa           │
├────────────────────────────────────────────────────────────────────┤
│  security/     JwtService · JwtAuthFilter (stateless)              │
│  config/       SecurityConfig · OpenApiConfig · DataSeeder         │
│  exception/    GlobalExceptionHandler → JSON padronizado           │
└────────────────────────────────────────────────────────────────────┘
        │                                             │
   MongoDB (Atlas / local)                    Google Gemini API
```

**Fluxo de autenticação:** `POST /api/auth/login` valida e-mail/senha (BCrypt) e devolve um JWT
com `sub` (id), `nome`, `email` e `role`. Cada requisição passa pelo `JwtAuthFilter`, que valida a
assinatura, carrega o usuário do banco e popula o `SecurityContext` com a authority `ROLE_<PERFIL>`.
As regras de acesso ficam nos controllers via `@PreAuthorize`.

### Modelo de dados (MongoDB)

| Coleção | Campos principais |
|---|---|
| `usuarios` | nome, email (único), senhaHash, role, pontos, ativo, criadoEm |
| `orientacoes` | titulo, descricao, pilar, **categoria, campanha, dataInicio, dataFim, ativa**, criadoPor*, **historico[]** (id, data, acao, categoria, campanha, usuarioNome) |
| `ideias` | titulo, descricao, pilar, **orientacaoId/Titulo**, autorId/Nome, status, prioridade, **avaliacaoIa** (pontuacao, alinhamento, viabilidade, impacto, prioridadeSugerida, justificativa, modelo), criadoEm |
| `projetos` | titulo, descricao, pilar, **orientacaoId/Titulo**, ideiaOrigemId/Titulo, gestorId/Nome, etapa, status, investimento, retornoFinanceiro, prazoMeses, aumentoProdutividade, reducaoCustos, **resultados**, criadoEm, atualizadoEm — `roi` e `lucro` calculados |
| `recompensas` | titulo, descricao, custo, emoji, categoria, disponivel |
| `resgates` | recompensaId/Titulo/Emoji, custoPago, operadorId/Nome, resgatadoEm, status |

## 3. Perfis e permissões

| Recurso | Operador | Gestor | Líder |
|---|:---:|:---:|:---:|
| Consultar orientações vigentes e histórico | ✓ | ✓ | ✓ |
| CRUD de orientações (encerramento é lógico, preserva histórico) | — | — | ✓ |
| CRUD das próprias ideias (editar/excluir só enquanto pendente) | ✓ | — | — |
| Consultar todas as ideias, priorizar, aprovar/rejeitar | — | ✓ | consulta |
| Avaliar ideias com IA (individual ou em lote) e ver ranking | — | ✓ | ranking |
| CRUD de projetos e atualização de progresso/resultados | — | ✓ | consulta |
| Relatórios / dashboard (geral, por estratégia, por projeto, por pilar) | — | ✓ | ✓ |
| Recompensas e resgates com pontos | ✓ | — | — |

**Gamificação (agora no backend):** +5 pontos ao cadastrar ideia; +50 na primeira aprovação
(idempotente); resgate debita pontos com `findAndModify` atômico (`pontos >= custo`), sem
possibilidade de saldo negativo.

## 4. Endpoints

Base: `/api`. Todos exigem `Authorization: Bearer <token>`, exceto os marcados como públicos.
Erros seguem o formato `{ timestamp, status, erro, mensagem, path, campos? }`.

### Autenticação
| Método | Rota | Perfil | Payload | Resposta |
|---|---|---|---|---|
| POST | `/auth/login` | público | `{ "email", "senha" }` | `{ token, tipo, expiraEm, usuario{id,nome,email,role,pontos} }` |
| GET | `/auth/me` | todos | — | `usuario` (pontos atualizados) |
| GET | `/health` | público | — | `{ status: "UP" }` |

### Orientações estratégicas
| Método | Rota | Perfil | Payload | Resposta |
|---|---|---|---|---|
| GET | `/orientacoes?incluirInativas=false` | todos (inativas só líder) | — | `[Orientacao]` |
| GET | `/orientacoes/{id}` | todos | — | `Orientacao` |
| GET | `/orientacoes/{id}/historico` | todos | — | `[ {id, data, acao, categoria, campanha, usuarioNome} ]` |
| POST | `/orientacoes` | LIDER | `{ titulo, descricao, pilar, categoria, campanha?, dataInicio?, dataFim?, ativa? }` | `201 Orientacao` |
| PUT | `/orientacoes/{id}` | LIDER | idem | `Orientacao` |
| DELETE | `/orientacoes/{id}` | LIDER | — | `Orientacao` (ativa=false, histórico preservado) |

### Ideias de inovação
| Método | Rota | Perfil | Payload | Resposta |
|---|---|---|---|---|
| GET | `/ideias/minhas` | OPERADOR | — | `[Ideia]` |
| POST | `/ideias` | OPERADOR | `{ titulo, descricao, pilar, orientacaoId? }` | `201 Ideia` (+5 pts) |
| PUT | `/ideias/{id}` | OPERADOR (autor, pendente) | idem | `Ideia` |
| DELETE | `/ideias/{id}` | OPERADOR (autor, pendente) | — | `204` |
| GET | `/ideias?status=&orientacaoId=` | GESTOR, LIDER | — | `[Ideia]` |
| GET | `/ideias/aprovadas` | GESTOR, LIDER | — | `[Ideia]` por prioridade |
| GET | `/ideias/ranking-ia` | GESTOR, LIDER | — | `[Ideia]` por pontuação IA |
| GET | `/ideias/{id}` | todos (operador só as próprias) | — | `Ideia` |
| PATCH | `/ideias/{id}/prioridade` | GESTOR | `{ "prioridade": 0-5 }` | `Ideia` |
| PATCH | `/ideias/{id}/status` | GESTOR | `{ "status": "aprovada" \| "rejeitada" \| "pendente" }` | `Ideia` (+50 pts na 1ª aprovação) |
| POST | `/ideias/{id}/avaliar-ia?aplicarPrioridade=true` | GESTOR | — | `Ideia` com `avaliacaoIa` |
| POST | `/ideias/avaliar-ia?aplicarPrioridade=true` | GESTOR | — | `[Ideia]` pendentes avaliadas |

Formato de `avaliacaoIa`:
```json
{ "pontuacao": 82, "alinhamentoEstrategico": 90, "viabilidade": 75, "impacto": 80,
  "prioridadeSugerida": 5, "justificativa": "...", "modelo": "gemini-3.6-flash", "avaliadoEm": "..." }
```

### Projetos e iniciativas
| Método | Rota | Perfil | Payload | Resposta |
|---|---|---|---|---|
| GET | `/projetos?status=&orientacaoId=` | GESTOR, LIDER | — | `[Projeto]` (com `roi`, `lucro`) |
| GET | `/projetos/{id}` | GESTOR, LIDER | — | `Projeto` |
| POST | `/projetos` | GESTOR | `{ titulo, descricao, pilar, orientacaoId?, ideiaOrigemId?, etapa, status, investimento, retornoFinanceiro, prazoMeses, aumentoProdutividade, reducaoCustos, resultados? }` | `201 Projeto` |
| PUT | `/projetos/{id}` | GESTOR | idem | `Projeto` |
| DELETE | `/projetos/{id}` | GESTOR (não concluído) | — | `204` |

Valores: `etapa` ∈ `planejamento, execucao, monitoramento, concluido` · `status` ∈ `em_andamento, pausado, concluido, cancelado`.

### Relatórios / Dashboard
| Método | Rota | Perfil | Resposta |
|---|---|---|---|
| GET | `/relatorios/resumo` | LIDER, GESTOR | `{ kpis{quantidadeProjetos, investimentoTotal, retornoTotal, lucroTotal, roi, prazoMedioMeses, produtividadeMedia, reducaoCustosMedia}, projetosPorStatus, projetosPorEtapa, totalIdeias, ideiasPendentes, ideiasAprovadas, ideiasRejeitadas, estrategiasAtivas }` |
| GET | `/relatorios/por-estrategia` | LIDER, GESTOR | `[ { orientacaoId, titulo, pilar, categoria, campanha, ativa, quantidadeIdeias, ideiasAprovadas, kpis } ]` |
| GET | `/relatorios/por-estrategia/{orientacaoId}` | LIDER, GESTOR | item acima |
| GET | `/relatorios/por-projeto` | LIDER, GESTOR | `[ { id, titulo, pilar, orientacaoId, etapa, status, investimento, retornoFinanceiro, lucro, roi, prazoMeses, aumentoProdutividade, reducaoCustos } ]` |
| GET | `/relatorios/por-pilar` | LIDER, GESTOR | `[ { pilar, quantidadeIdeias, kpis } ]` |

### Gamificação
| Método | Rota | Perfil | Payload | Resposta |
|---|---|---|---|---|
| GET | `/recompensas` | todos | — | `[Recompensa]` |
| GET | `/resgates/meus` | OPERADOR | — | `[Resgate]` |
| POST | `/resgates` | OPERADOR | `{ "recompensaId" }` | `201 { resgate, saldoAtual }` — `422` se saldo insuficiente |

A especificação completa (schemas e exemplos) está no Swagger: `/swagger-ui.html` · JSON em `/v3/api-docs`.
Uma coleção de requisições pronta está em `docs/requests.http`.

## 5. Integração com IA (Plus)

**Funcionalidade escolhida:** pontuação e priorização automática das ideias de inovação, apoiando
o gestor na seleção dos futuros projetos.

**Modelo:** Google Gemini (`gemini-3.6-flash`, plano gratuito da Gemini API), chamado via
`RestClient` no endpoint `generateContent` com `responseMimeType: application/json`.

**Como funciona:**
1. O gestor chama `POST /api/ideias/{id}/avaliar-ia` (ou `/api/ideias/avaliar-ia` para todas as pendentes
   que ainda não têm avaliação generativa, incluindo as que caíram na heurística local).
2. O `IaService` monta um prompt com as **orientações estratégicas vigentes** (título, pilar, categoria,
   campanha, descrição) e a ideia (título, pilar, vínculo e descrição).
3. O modelo devolve um JSON com `alinhamentoEstrategico`, `viabilidade`, `impacto` (0–100),
   `pontuacao` final ponderada (40/30/30), `prioridadeSugerida` (1–5) e `justificativa` em português.
4. A avaliação é gravada na ideia (`avaliacaoIa`) e, por padrão, a `prioridade` recebe a sugestão —
   o gestor pode ajustar depois via `PATCH /prioridade`. O ranking fica em `GET /api/ideias/ranking-ia`.

**Resiliência (três camadas):**

1. **Retry** para erros transitórios (`503` de sobrecarga, `429` de limite por minuto), até 3
   tentativas, respeitando o `retryDelay` que a própria API sugere; o lote ainda espaça as
   chamadas em 4s.
2. **Cascata de modelos**: a cota gratuita é contada *por modelo* (`quotaId`
   `GenerateRequestsPerDayPerProjectPerModel-FreeTier`), então `GEMINI_MODEL` aceita vários
   modelos e o seguinte assume quando o anterior esgota a cota diária.
3. **Avaliador heurístico local**, que entra só depois disso e registra o motivo na própria
   justificativa (ex.: "cota diaria gratuita da Gemini esgotada"), em vez de apresentar a nota
   como se fosse da IA generativa. O campo `modelo` sempre identifica a origem da avaliação.
Se `GEMINI_API_KEY` não estiver definida ou a chamada falhar mesmo assim, o `AvaliadorHeuristico`
gera uma avaliação local (vínculo com estratégia, tamanho/clareza da descrição e termos de impacto),
identificada com `modelo: "heuristica-local"`. Assim a funcionalidade nunca quebra na demonstração.

## 6. Deploy (opcional)

- **MongoDB Atlas:** crie um cluster M0 gratuito, um usuário de banco e libere o IP `0.0.0.0/0`
  (para o serviço na nuvem); copie a connection string para `MONGODB_URI`.
- **Render / Railway:** crie um Web Service a partir deste repositório usando o `Dockerfile`
  e defina `MONGODB_URI`, `JWT_SECRET` e `GEMINI_API_KEY` nas variáveis de ambiente.
  O health check pode apontar para `/api/health`.
- **App Android:** no emulador use `http://10.0.2.2:8080/`; em aparelho físico use o IP da máquina
  na mesma rede (ex.: `http://192.168.0.10:8080/`) ou a URL pública do deploy.

## 7. Estrutura do projeto
```
src/main/java/br/com/fiap/inovagab/api/
├── InovaGabApiApplication.java
├── config/        SecurityConfig · WebConfig · OpenApiConfig · DataSeeder · *Properties
├── controller/    Auth · Orientacao · Ideia · Projeto · Relatorio · Recompensa · Health
├── service/       Auth · Orientacao · Ideia · Projeto · Relatorio · Recompensa · Pontos · Kpis
├── ia/            GeminiClient · IaService · AvaliadorHeuristico
├── repository/    MongoRepository por coleção
├── domain/        documentos e enums
├── dto/           requests/responses (records + Bean Validation)
├── security/      JwtService · JwtAuthFilter
└── exception/     GlobalExceptionHandler · ApiError · exceções de domínio
src/test/java/...  KpisTest · AvaliadorHeuristicoTest
docs/requests.http  coleção de requisições
```
