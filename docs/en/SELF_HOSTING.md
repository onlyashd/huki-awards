# Self-Hosting Tutorial

This guide provides step-by-step instructions on how to host your own instance of Huki Awards.

## Prerequisites

- **Java Development Kit (JDK) 17 or higher**
- **IntelliJ IDEA** or **Android Studio** latest version
- **PostgreSQL Database** (e.g., Supabase, Neon, or local)
- **Discord Developer Account** (for OAuth2 authentication)
- **Twitch Developer Account** (for IGDB API access)

---

## 1. Setup Discord Application

1. Go to the [Discord Developer Portal](https://discord.com/developers/applications).
2. Click **New Application** and give it a name.
3. Go to **OAuth2 -> General**.
4. Add a Redirect URI: `http://localhost:8080/callback/discord` (Replace `localhost:8080` with your
   production domain later).
5. Note your **Client ID** and **Client Secret**.

## 2. Setup IGDB (Twitch) API

1. Go to the [Twitch Developer Console](https://dev.twitch.tv/console).
2. Register a new application.
3. Set the Redirect URI to `http://localhost`.
4. Select Category: **Application Integration**.
5. Note your **Client ID** and generate a **Client Secret**.

## 3. Frontend Configuration (local.properties)

Before building the web application, you must provide the Discord Client ID so the frontend can
initiate the login flow.

1. Create a file named `local.properties` in the **root** of the project.
2. Add the following lines:
   ```properties
   DISCORD_CLIENT_ID=your_discord_client_id_here
   SENTRY_DSN_SERVER=your_sentry_dsn_here (optional)
   ```

These values are baked into the Web/Wasm binary during the build process.

## 4. Server Environment Variables

The server requires several environment variables to function correctly. You can set these in your
OS or via a `.env` file (if using a runner that supports it).

| Variable                | Description                                  | Example                                          |
|:------------------------|:---------------------------------------------|:-------------------------------------------------|
| `DATABASE_URL`          | JDBC URL for PostgreSQL                      | `jdbc:postgresql://db.example.com:5432/postgres` |
| `DATABASE_USER`         | Database username                            | `postgres`                                       |
| `DATABASE_PASSWORD`     | Database password                            | `your_password`                                  |
| `JWT_SECRET`            | Secret key for signing JWT tokens            | `any_long_random_string`                         |
| `DISCORD_CLIENT_ID`     | Discord Application Client ID                | `1234567890...`                                  |
| `DISCORD_CLIENT_SECRET` | Discord Application Client Secret            | `ABC...`                                         |
| `IGDB_CLIENT_ID`        | Twitch/IGDB Client ID                        | `XYZ...`                                         |
| `IGDB_CLIENT_SECRET`    | Twitch/IGDB Client Secret                    | `123...`                                         |
| `SENTRY_DSN_SERVER`     | (Optional) Sentry DSN for error tracking     | `https://...@...sentry.io/...`                   |
| `ALLOWED_HOSTS`         | Comma-separated list of allowed CORS origins | `http://localhost:3030,https://my-awards.com`    |
| `REDIRECT_URL`          | Full URL for the Discord callback            | `https://api.my-awards.com/callback/discord`     |

## 5. Database Initialization

The server uses **Exposed** as an ORM and will automatically create the necessary tables on the
first run.

- **Fresh Install**: Use the [initial_population.sql](../initial_population.sql) script to set up
  your first admin user, default settings, and sample categories.

## 6. Running the Application

### Backend (Ktor Server)

Run the following command to start the server:

```bash
./gradlew :server:run
```

The server will start on port `8080` by default.

### Frontend (Compose Web/Wasm)

To run the web application in development mode:

```bash
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun
```

To build the production version:

```bash
./gradlew :app:webApp:wasmJsBrowserDistribution
```

The distribution files will be located in `app/webApp/build/dist/wasmJs/productionExecutable`.

## 7. Initial Configuration

1. Log in to the application using Discord.
2. To become an admin, ensure your Discord username is added to the `admins` table via the SQL
   script in step 5, or manually inserted into the database.
3. Once logged in as an admin, access the **Admin Dashboard** to create categories, manage the event
   phase (Nomination vs Voting), and configure dates.

## 8. Verification

To ensure everything is working as expected, follow our [Manual Testing Guide](TESTING_GUIDE.md).
