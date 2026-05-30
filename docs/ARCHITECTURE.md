# Architecture

## E2E

```md
                      +---------------------------------------+
                      |          Supabase Database            |
                      |  (Users, Categories, Nominees, Votes) |
                      +-------------------+-------------------+
                                          |
                                          | PostgreSQL Connection
                                          v
+------------------+  OAuth Callback  +-----------------------+  IGDB API  +------------+
| Twitch / Discord | <--------------> |    Ktor Backend       | <--------> |  IGDB API  |
+------------------+                  | (Runs on Render Web)  |            +------------+
                                      +-----------+-----------+
                                                  |
                                                  | Ktor HTTP / JSON (Shared Models)
                                                  v
                                      +-----------------------+
                                      |   Frontend Client     |
                                      | (Runs on Render Static|
                                      |  or Android/Desktop)  |
                                      +-----------------------+
```

## Modules

```md
├── build.gradle.kts
├── settings.gradle.kts
│
├── core/                        # ← Module for shared server-and-client logic
│   └── src/
│       └── commonMain/kotlin/   # Shared data models (User, Category, VoteRequest, TopTenResult)
│
├── server/                      # ← The backend Ktor module
│   ├── build.gradle.kts
│   └── src/
│       └── main/kotlin/         # Ktor routing, OAuth callback, IGDB client service, Supabase DB logic
│
└── app/                         # ← NESTED CLIENT APP FOLDER (Keeps frontend separate)
    ├── shared/                  # ← The shared client library (No platform run configurations!)
    │   ├── build.gradle.kts
    │   └── src/
    │       └── commonMain/kotlin/ # Compose Multiplatform UI elements, ViewModels, and VotingApiClient
    │
    └── webApp/                  # ← Separate Web-only frontend entry point (Runs on Render Static)
        ├── build.gradle.kts
        └── src/wasmJsMain/      # index.html, WebAssembly entry point compilation scripts
```
