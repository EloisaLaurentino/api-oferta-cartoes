# api-oferta-cartoes

API REST que recebe os dados de um cliente e retorna os cartões de crédito para os quais ele é elegível, de acordo com renda, idade e UF de residência. Desenvolvida como resposta a um desafio técnico — este README documenta tanto o "como rodar" quanto as decisões de design por trás do código, para facilitar a avaliação e a evolução futura do projeto.

## Stack e por quê

- **Java 21 + Spring Boot 3.5** — linguagem/framework pedidos pelo enunciado (Web, Validation, Actuator).
- **springdoc-openapi** — gera Swagger UI e contrato OpenAPI a partir das anotações do controller, sem manter documentação manual desatualizada.
- **Maven Wrapper** — qualquer pessoa roda o build com a versão certa do Maven sem instalar nada além do JDK.
- **JUnit 5 / Mockito** — testes unitários de domínio isolados do Spring + testes de integração via `MockMvc`/`@SpringBootTest`.
- **Sem banco de dados** — o catálogo de produtos é estático e configurável via `application.yml`; persistência foi avaliada como escopo desnecessário para o problema proposto (ver seção "O que eu deixei de fazer, e por quê").

## Como rodar

### Opção 1 — Maven Wrapper

Pré-requisito: JDK 21 instalado e `JAVA_HOME` configurado.

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

### Opção 2 — Docker

Não exige Java nem Maven instalados na máquina, só Docker. O `Dockerfile` usa multi-stage build: a imagem final contém apenas o JRE e o `.jar`, roda com usuário não-root.

```bash
docker build -t cartoes-api .
docker run -p 8080:8080 cartoes-api
```

## Rodando os testes

```bash
./mvnw test
```

## Endpoints

### `POST /cartoes`

Recebe a solicitação de um cliente e retorna os cartões ofertados.

**Request:**

```json
{
  "cliente": {
    "nome": "Cliente Teste",
    "cpf": "123.456.789-10",
    "idade": 25,
    "data_nascimento": "2000-01-01",
    "uf": "SP",
    "renda_mensal": 4000,
    "email": "cliente@teste.com",
    "telefone_whatsapp": "11999992020"
  }
}
```

**Respostas possíveis:**

| Status | Quando |
|---|---|
| `200 OK` | Cliente elegível a pelo menos um cartão |
| `204 No Content` | Solicitação válida, mas nenhum cartão aprovado |
| `400 Bad Request` | Payload inválido (campo obrigatório ausente, formato errado, idade abaixo do mínimo) |
| `422 Unprocessable Entity` | Regra de negócio não atendida (ex.: renda mensal abaixo do mínimo de todos os produtos) |

Payload de erro (400/422/500), inspirado na estrutura sugerida no enunciado (que por sua vez remete à RFC 9457 — problem details):

```json
{
  "codigo": "422",
  "mensagem": "Solicitação não pôde ser processada.",
  "detalhe_erro": {
    "app": "cartoes-api",
    "tipo_erro": "RENDA_MINIMA_NAO_ATENDIDA",
    "mensagem_interna": "A renda mensal informada não atende aos critérios de análise de crédito."
  }
}
```

Exceções não mapeadas caem num handler genérico que devolve `500` sem vazar stacktrace, e loga o erro internamente — ponto de atenção de segurança (não expor detalhes internos ao cliente da API).

### `GET /actuator/health`

Healthcheck (`{"status":"UP"}`). Só `health` e `info` são expostos publicamente — os demais endpoints do Actuator (env, beans, etc.) ficam desabilitados para não vazar informação interna da aplicação.

### Documentação interativa

Com a aplicação rodando:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Contrato OpenAPI: `http://localhost:8080/v3/api-docs`

## Regras de negócio

Produtos disponíveis:

| Produto | Limite | Renda mínima | Anuidade |
|---|---|---|---|
| `CARTAO_SEM_ANUIDADE` | R$ 1.000,00 | R$ 3.500,00 | R$ 0,00 |
| `CARTAO_DE_PARCEIROS` | R$ 3.000,00 | R$ 5.500,00 | R$ 10,00 |
| `CARTAO_COM_CASHBACK` | R$ 5.000,00 | R$ 7.500,00 | R$ 20,00 |

Regras de restrição por idade/UF (avaliadas na ordem abaixo — a primeira que se aplicar define os cartões elegíveis; a ordem importa porque a regra de SP 25-30 anos é mais específica e precisa vencer a regra genérica de SP):

1. **SP, entre 25 e 30 anos**: todos os cartões liberados (dentro da faixa de renda).
2. **Menor de 25 anos**: apenas `CARTAO_SEM_ANUIDADE`.
3. **Reside em SP** (fora da regra acima): apenas `CARTAO_SEM_ANUIDADE` e `CARTAO_COM_CASHBACK`.
4. Nenhuma regra aplicável: sem restrição adicional além da renda.

Idade mínima para solicitar (18 anos) e os valores de cada produto são configuráveis em [`application.yml`](src/main/resources/application.yml) — atende ao requisito de "regras que podem mudar no futuro" sem precisar recompilar o código.

## Arquitetura e decisões de design

O código separa **domínio puro** (sem dependência do Spring) da **camada de aplicação/infraestrutura**:

- `domain` — modelos e regras de elegibilidade. `AvaliadorElegibilidade` orquestra uma lista de `RegraDeRestricao` (Strategy pattern): cada regra de idade/UF é uma classe própria, testável isoladamente, e novas regras se plugam sem alterar as existentes (Open/Closed).
- `application` — `SolicitacaoCartaoService` orquestra o caso de uso (valida pré-condições, monta o perfil, delega ao domínio); não conhece HTTP.
- `api` — `CartaoController`, DTOs de request/response e `GlobalExceptionHandler` (tradução de exceções de domínio para status HTTP).
- `config` — `CartoesProperties` (configuração externalizada), `AppConfig` (fiação dos beans de domínio) e `OpenApiConfig`.

**Por que essa separação:** o domínio (regras de elegibilidade) é a parte que mais muda e mais precisa de teste rápido e determinístico. Mantê-lo em Java puro, com um `Clock` injetável em vez de `LocalDate.now()` direto, permite testar cálculo de idade sem depender da data real do sistema — e permite trocar a camada web (por exemplo, adicionar um consumidor de fila) sem tocar nas regras de negócio.

**Trade-off consciente:** não há camada de persistência. O catálogo de produtos vive em memória, montado a partir do `application.yml` na subida da aplicação. Para o escopo do desafio (regras de elegibilidade sobre um catálogo fixo) isso evita complexidade desnecessária; se o catálogo precisar ser administrado dinamicamente (CRUD de produtos), a introdução de um banco de dados seria o próximo passo natural.

## Testes

50+ testes cobrindo:

- Regras de elegibilidade isoladas (idade, UF, combinações, ordem de precedência das regras) — `AvaliadorElegibilidadeTest`.
- Casos de erro, não só o caminho feliz: renda insuficiente, idade abaixo do mínimo, payload malformado, campos ausentes/inválidos.
- Integração ponta a ponta via `MockMvc` (`CartaoControllerIntegrationTest`, `CartaoControllerSemCartaoTest`) cobrindo os 4 status HTTP possíveis (200/204/400/422).
- Configuração externalizada (`CartoesPropertiesTest`) e contrato de observabilidade/documentação (`HealthCheckIntegrationTest`, `OpenApiDocsIntegrationTest`).

## Extras implementados

- **Observabilidade**: `/actuator/health`.
- **Documentação da API**: Swagger UI + OpenAPI via springdoc.
- **Containerização**: `Dockerfile` multi-stage, usuário não-root, `.dockerignore`.
- **Configurações**: idade mínima e parâmetros de cada produto (limite, renda mínima, anuidade) via `application.yml`, não hardcoded.
- **Tratamento de erros**: handler global centralizado, mensagens de validação por campo, sem vazar stacktrace ao cliente.

