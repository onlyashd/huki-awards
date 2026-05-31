# Arquitetura

## Ponta a Ponta

```md
                      +---------------------------------------+
                      |       Banco de dados no Supabase      |
                      |  (Users, Categories, Nominees, Votes) |
                      +-------------------+-------------------+
                                          |
                                          | Conexão PostgreSQL
                                          v

+------------------+ OAuth Callback +-----------------------+ IGDB API +------------+
| Twitch / Discord | <--------------> | Ktor Backend | <--------> | IGDB API |
+------------------+ +-----------+-----------+ +------------+
|
| Ktor HTTP / JSON (Models compartilhados)
v
+-----------------------+
| Frontend Client |
+-----------------------+
```

## Modules

```md
├── build.gradle.kts
├── settings.gradle.kts
│
├── core/ # ← Módulo para lógica compartilhada entre servidor e cliente
│ └── src/
│ └── commonMain/kotlin/ # Modelos de dados compartilhados (User, Category, VoteRequest,
TopTenResult)
│
├── server/ # ← O módulo de backend Ktor
│ ├── build.gradle.kts
│ └── src/
│ └── main/kotlin/ # Roteamento Ktor, retorno de chamada OAuth, serviço de cliente IGDB, lógica de
banco de dados Supabase
│
└── app/ # ← PASTA DE APLICATIVOS CLIENTE ANINHADA (Mantém o frontend separado)
├── shared/ # ← Biblioteca cliente compartilhada (Sem configurações de execução de plataforma!)
│ ├── build.gradle.kts
│ └── src/
│ └── commonMain/kotlin/ # Elementos do Compose Multiplatform, ViewModels e ApiClient
│
└── webApp/ # ← Ponto de entrada de front-end separado, exclusivo para a Web
├── build.gradle.kts
└── src/wasmJsMain/ # index.html, scripts de compilação do ponto de entrada do WebAssembly
```
