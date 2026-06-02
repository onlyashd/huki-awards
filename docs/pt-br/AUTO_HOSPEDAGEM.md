# Tutorial de Hospedagem Própria

Este guia fornece instruções passo a passo sobre como hospedar sua própria instância do Huki Awards.

## Pré-requisitos

- **Java Development Kit (JDK) 17 ou superior**
- **IntelliJ IDEA** ou **Android Studio** (versão mais recente)
- **Banco de dados PostgreSQL** (por exemplo, Supabase, Neon ou local)
- **Conta de desenvolvedor do Discord** (para autenticação OAuth2)
- **Conta de desenvolvedor do Twitch** (para acesso à API do IGDB)

---

## 1. Configurar o aplicativo do Discord

1. Acesse o [Portal do Desenvolvedor do Discord](https://discord.com/developers/applications).
2. Clique em **Novo aplicativo** e dê um nome a ele.
3. Vá para **OAuth2 -> Geral**.
4. Adicione um URI de redirecionamento: `http://localhost:8080/callback/discord` (Substitua
   `localhost:8080` pelo seu domínio de produção posteriormente).
5. Anote seu **ID do Cliente** e **Segredo do Cliente**.

## 2. Configurar a API do IGDB (Twitch)

1. Acesse o [Console do Desenvolvedor do Twitch](https://dev.twitch.tv/console).
2. Registre um novo aplicativo.
3. Defina o URI de redirecionamento como `http://localhost`.
4. Selecione a Categoria: **Integração de Aplicativos**.
5. Anote seu **ID do Cliente** e gere um **Segredo do Cliente**.

## 3. Configuração do Frontend (local.properties)

Antes de criar o aplicativo web, você deve fornecer o ID do Cliente do Discord para que o frontend
possa iniciar o fluxo de login.

1. Crie um arquivo chamado `local.properties` na **raiz** do projeto.
2. Adicione as seguintes linhas:

``properties
DISCORD_CLIENT_ID=seu_id_do_cliente_discord_aqui
SENTRY_DSN_SERVER=seu_servidor_dsn_sentry_aqui (opcional)
``

Esses valores são incorporados ao binário Web/Wasm durante o processo de compilação.

## 4. Variáveis de Ambiente do Servidor

O servidor requer diversas variáveis de ambiente para funcionar corretamente. Você pode
configurá-las no seu sistema operacional ou por meio de um arquivo `.env` (se estiver usando um
executor que o suporte).

| Variável                | Descrição                                              | Exemplo                                          |
|:------------------------|:-------------------------------------------------------|:-------------------------------------------------|
| `DATABASE_URL`          | URL JDBC para PostgreSQL                               | `jdbc:postgresql://db.example.com:5432/postgres` |
| `DATABASE_USER`         | Nome de usuário do banco de dados                      | `postgres`                                       |
| `DATABASE_PASSWORD`     | Senha do banco de dados                                | `sua_senha`                                      |
| `JWT_SECRET`            | Chave secreta para assinatura de tokens JWT            | `qualquer_string_aleatória_longa`                |
| `DISCORD_CLIENT_ID`     | ID do cliente do aplicativo Discord                    | `1234567890...`                                  |
| `DISCORD_CLIENT_SECRET` | Segredo do cliente do aplicativo Discord               | `ABC...`                                         |
| `IGDB_CLIENT_ID`        | ID do cliente Twitch/IGDB                              | `XYZ...`                                         |
| `IGDB_CLIENT_SECRET`    | Segredo do cliente Twitch/IGDB                         | `123...`                                         |
| `DISCORD_WEBHOOK_URL`   | URL do Webhook do Discord para Compartilhamento        | `https://discord.com/api/webhooks/...`           |
| `SENTRY_DSN_SERVER`     | (Opcional) DSN do Sentry para rastreamento de erros    | `https://...@...sentry.io/...`                   |
| `ALLOWED_HOSTS`         | Lista separada por vírgulas de origens CORS permitidas | `http://localhost:3030,https://my-awards.com`    |
| `REDIRECT_URL`          | URL completa para o callback do Discord                | `https://api.my-awards.com/callback/discord`     |

## 5. Inicialização do Banco de Dados

O servidor usa o **Exposed** como ORM e criará automaticamente as tabelas necessárias na
primeira execução.

- **Instalação Nova**: Use o script [initial_population.sql](../initial_population.sql) para
  configurar seu primeiro usuário administrador, configurações padrão e categorias de exemplo.

## 6. Executando o Aplicativo

### Backend (Servidor Ktor)

Execute o seguinte comando para iniciar o servidor:

```bash
./gradlew :server:run
```

O servidor será iniciado na porta `8080` por padrão.

### Frontend (Compose Web/Wasm)

Para executar o aplicativo web em modo de desenvolvimento:

```bash
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun
```

Para gerar a versão de produção:

```bash
./gradlew :app:webApp:wasmJsBrowserDistribution
```

Os arquivos de distribuição estarão localizados em
`app/webApp/build/dist/wasmJs/productionExecutable`.

## 7. Configuração Inicial

1. Faça login no aplicativo usando o Discord.
2. Para se tornar um administrador, certifique-se de que seu nome de usuário do Discord seja
   adicionado à tabela `admins` por meio do script SQL na etapa 5 ou inserido manualmente no banco
   de dados.
3. Depois de fazer login como administrador, acesse o **Painel de Administração** para criar
   categorias, gerenciar a fase do evento (Indicação vs. Votação) e configurar datas.

## 8. Verificação

Para garantir que tudo esteja funcionando conforme o esperado, siga
nosso [Guia de Teste Manual](GUIA_DE_TESTES.md).