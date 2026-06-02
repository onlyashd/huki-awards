package io.github.onlyashd.hukiawards

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.onlyashd.hukiawards.model.AdminsTable
import io.github.onlyashd.hukiawards.model.AuditLogsTable
import io.github.onlyashd.hukiawards.model.CategoriesTable
import io.github.onlyashd.hukiawards.model.SettingsTable
import io.github.onlyashd.hukiawards.model.UserPrincipal
import io.github.onlyashd.hukiawards.model.UsersTable
import io.github.onlyashd.hukiawards.model.VotesTable
import io.github.onlyashd.hukiawards.service.IgdbService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.oauth
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.sentry.Sentry
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

private val logger = KotlinLogging.logger {}

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(
        factory = Netty,
        port = port,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    initSentry()
    configureJwt()
    connectDatabase()

    install(ServerContentNegotiation) {
        json(Json {
            prettyPrint = false
            ignoreUnknownKeys = true
        })
    }

    install(io.ktor.server.plugins.cors.routing.CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)

        val allowedHosts = System.getenv("ALLOWED_HOSTS")?.split(",") ?: listOf("localhost:3030")
        allowedHosts.forEach { host ->
            if (host.startsWith("http")) {
                val uri = java.net.URI(host)
                allowHost(
                    uri.host + (if (uri.port != -1) ":${uri.port}" else ""),
                    schemes = listOf(uri.scheme)
                )
            } else {
                allowHost(host)
            }
        }

        allowCredentials = true
    }

    val httpClient = HttpClient(OkHttp) {
        this.install(ClientContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    logger.info { "Server started and configuration loaded successfully!" }
    val igdbService = IgdbService(httpClient)
    val imageService = io.github.onlyashd.hukiawards.service.ImageService()

    routing {
        discordRoutes(httpClient)
        publicRoutes(httpClient, igdbService, imageService)
    }
}

fun connectDatabase() {
    val config = HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = System.getenv("DATABASE_URL") ?: ""
        username = System.getenv("DATABASE_USER") ?: ""
        password = System.getenv("DATABASE_PASSWORD") ?: ""

        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_SERIALIZABLE"
    }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(
            UsersTable,
            CategoriesTable,
            VotesTable,
            SettingsTable,
            AdminsTable,
            AuditLogsTable
        )

        // Ensure default admins exist
        val defaultAdmins = listOf("onlyashd", "hukizan", "sub0")
        defaultAdmins.forEach { adminUsername ->
            if (AdminsTable.selectAll().where { AdminsTable.username eq adminUsername }
                    .count() == 0L) {
                AdminsTable.insert {
                    it[username] = adminUsername
                }
            }
        }

        // Ensure default settings exist
        if (SettingsTable.selectAll().count() == 0L) {
            SettingsTable.insert {
                it[eventName] = "Huki Awards 2026"
                it[isVotingOpen] = true
                it[showDatesToUsers] = true
            }
        }
    }
}

fun initSentry() {
    Sentry.init { options ->
        options.dsn = System.getenv("SENTRY_DSN_SERVER") ?: ""
        options.tracesSampleRate = 1.0
    }
}

private fun Application.configureJwt() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Huki Awards Server"
            verifier(
                JWT.require(
                    Algorithm.HMAC256(
                        System.getenv("JWT_SECRET") ?: ""
                    )
                )
                    .withAudience("goty-users")
                    .withIssuer("goty-backend")
                    .build()
            )
            validate { credential ->
                val idString = credential.payload.getClaim("id").asString()
                val role = credential.payload.getClaim("role").asString()
                val username = credential.payload.getClaim("username").asString() ?: "unknown"

                if (idString != null && role != null) {
                    // Return your data class directly
                    UserPrincipal(
                        userId = UUID.fromString(idString),
                        username = username,
                        role = role
                    )
                } else {
                    null
                }
            }
        }

        // Configure OAuth providers
        oauth("discord-oauth") {
            client = HttpClient(OkHttp)
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "discord",
                    authorizeUrl = "https://discord.com/api/oauth2/authorize",
                    accessTokenUrl = "https://discord.com/api/oauth2/token",
                    clientId = System.getenv("DISCORD_CLIENT_ID") ?: "",
                    clientSecret = System.getenv("DISCORD_CLIENT_SECRET") ?: "",
                    requestMethod = HttpMethod.Post,
                    defaultScopes = listOf("identify")
                )
            }
            urlProvider = {
                System.getenv("REDIRECT_URL") ?: "http://localhost:8080/callback/discord"
            }
        }
    }
}
