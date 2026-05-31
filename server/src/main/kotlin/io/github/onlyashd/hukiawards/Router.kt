package io.github.onlyashd.hukiawards

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.onlyashd.hukiawards.model.AdminsTable
import io.github.onlyashd.hukiawards.model.AuditLog
import io.github.onlyashd.hukiawards.model.AuditLogsTable
import io.github.onlyashd.hukiawards.model.AuthProvider
import io.github.onlyashd.hukiawards.model.CategoriesTable
import io.github.onlyashd.hukiawards.model.Category
import io.github.onlyashd.hukiawards.model.CategoryRequest
import io.github.onlyashd.hukiawards.model.CategoryStat
import io.github.onlyashd.hukiawards.model.DiscordTokenResponse
import io.github.onlyashd.hukiawards.model.DiscordUser
import io.github.onlyashd.hukiawards.model.GlobalStats
import io.github.onlyashd.hukiawards.model.LeaderboardEntry
import io.github.onlyashd.hukiawards.model.Roles
import io.github.onlyashd.hukiawards.model.Routes.Admin
import io.github.onlyashd.hukiawards.model.Routes.Admins
import io.github.onlyashd.hukiawards.model.Routes.Api
import io.github.onlyashd.hukiawards.model.Routes.CallbackDiscord
import io.github.onlyashd.hukiawards.model.Routes.Categories
import io.github.onlyashd.hukiawards.model.Routes.LoginDiscord
import io.github.onlyashd.hukiawards.model.Routes.Logout
import io.github.onlyashd.hukiawards.model.Routes.Profile
import io.github.onlyashd.hukiawards.model.Routes.Search
import io.github.onlyashd.hukiawards.model.Routes.Settings
import io.github.onlyashd.hukiawards.model.Routes.Share
import io.github.onlyashd.hukiawards.model.Routes.Stats
import io.github.onlyashd.hukiawards.model.Routes.Users
import io.github.onlyashd.hukiawards.model.Routes.Vote
import io.github.onlyashd.hukiawards.model.Routes.Votes
import io.github.onlyashd.hukiawards.model.Settings
import io.github.onlyashd.hukiawards.model.SettingsTable
import io.github.onlyashd.hukiawards.model.UserPrincipal
import io.github.onlyashd.hukiawards.model.UserProfile
import io.github.onlyashd.hukiawards.model.UsersTable
import io.github.onlyashd.hukiawards.model.VoteRequest
import io.github.onlyashd.hukiawards.model.VotesTable
import io.github.onlyashd.hukiawards.service.IgdbService
import io.github.onlyashd.hukiawards.service.ImageService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.isSuccess
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.intercept
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID
import javax.imageio.ImageIO

private const val AUTH_JWT = "auth-jwt"
private val logger = KotlinLogging.logger {}

fun Route.discordRoutes(httpClient: HttpClient) {
    get(LoginDiscord.path) {
        val clientId = System.getenv("DISCORD_CLIENT_ID") ?: ""
        val host = call.request.headers["Host"] ?: "localhost:8080"
        val protocol = if (host.contains("localhost")) "http" else "https"
        val redirectUri = "$protocol://$host/callback/discord"

        val authorizeUrl = "https://discord.com/api/oauth2/authorize" +
                "?client_id=$clientId" +
                "&redirect_uri=${redirectUri}" +
                "&response_type=code" +
                "&scope=identify" +
                "&state=secure_state"

        call.respondRedirect(authorizeUrl)
    }

    get(CallbackDiscord.path) {
        val rawCode = call.request.queryParameters["code"]
            ?: return@get call.respond(HttpStatusCode.BadRequest)
        val code = rawCode.decodeURLQueryComponent()

        val clientId = System.getenv("DISCORD_CLIENT_ID") ?: ""
        val clientSecret =
            System.getenv("DISCORD_CLIENT_SECRET") ?: ""
        val host = call.request.headers["Host"] ?: "localhost:8080"
        val protocol = if (host.contains("localhost")) "http" else "https"
        val expectedRedirectUri = "$protocol://$host/callback/discord"

        val response = httpClient.submitForm(
            url = "https://discord.com/api/oauth2/token",
            formParameters = Parameters.build {
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("grant_type", "authorization_code")
                append("code", code.trim())
                append("redirect_uri", expectedRedirectUri)
            }
        )

        if (!response.status.isSuccess()) {
            return@get call.respond(
                HttpStatusCode.BadRequest,
                "Discord error: ${response.bodyAsText()}"
            )
        }

        val tokenResponse = response.body<DiscordTokenResponse>()
        val discordProfile = httpClient.get("https://discord.com/api/users/@me") {
            bearerAuth(tokenResponse.accessToken)
        }.body<DiscordUser>()

        val username = discordProfile.username.lowercase()
        val name = discordProfile.name ?: username
        val avatarUrl = if (discordProfile.avatarUrl != null) {
            "https://cdn.discordapp.com/avatars/${discordProfile.id}/${discordProfile.avatarUrl}.png"
        } else {
            "https://cdn.discordapp.com/embed/avatars/${(discordProfile.id.toLong() shr 22) % 5}.png"
        }

        val isAdmin = transaction {
            AdminsTable.selectAll().where { AdminsTable.username eq username }.count() > 0
        }
        val role = if (isAdmin) Roles.ADMIN.name else Roles.USER.name

        val dbUserId = transaction {
            val existing = UsersTable.selectAll()
                .where { (UsersTable.username eq username) and (UsersTable.provider eq AuthProvider.DISCORD.name) }
                .singleOrNull()
            if (existing != null) {
                val id = existing[UsersTable.id]
                UsersTable.update({ UsersTable.id eq id }) {
                    it[UsersTable.role] = role
                    it[UsersTable.name] = name
                    it[UsersTable.avatarUrl] = avatarUrl
                }
                id
            } else {
                UsersTable.insert {
                    it[UsersTable.username] = username
                    it[UsersTable.provider] = AuthProvider.DISCORD.name
                    it[UsersTable.name] = name
                    it[UsersTable.avatarUrl] = avatarUrl
                    it[UsersTable.role] = role
                }[UsersTable.id]
            }
        }

        val jwtSecret = System.getenv("JWT_SECRET") ?: ""
        val token = JWT.create()
            .withAudience("goty-users")
            .withIssuer("goty-backend")
            .withClaim("id", dbUserId.toString())
            .withClaim("username", username)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 86400000))
            .sign(Algorithm.HMAC256(jwtSecret))

        val frontendUrl = System.getenv("FRONTEND_URL") ?: "http://localhost:3030"
        val dashboardPath = if (role == "ADMIN") "#/admin/dashboard" else "#/user/dashboard"
        call.respondRedirect("$frontendUrl/$dashboardPath?token=$token")
    }

    get(Logout.path) {
        call.respond(mapOf("status" to "logged out"))
    }
}

fun Route.publicRoutes(igdbService: IgdbService, imageService: ImageService) {
    // HTML Landing Page for Sharing (OpenGraph)
    get("${Share.path}/{id}") {
        val idStr = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val userId = try {
            UUID.fromString(idStr)
        } catch (e: Exception) {
            return@get call.respond(HttpStatusCode.BadRequest)
        }

        val profile = transaction {
            UsersTable.selectAll().where { UsersTable.id eq userId }.map {
                UserProfile(
                    id = it[UsersTable.id].toString(),
                    name = it[UsersTable.name] ?: "Usuário",
                    username = it[UsersTable.username] ?: "",
                    avatarUrl = it[UsersTable.avatarUrl] ?: ""
                )
            }.singleOrNull()
        } ?: return@get call.respond(HttpStatusCode.NotFound)

        val settings = transaction { SettingsTable.selectAll().singleOrNull() }
        val eventName = settings?.get(SettingsTable.eventName) ?: "Huki Awards"

        val host = call.request.headers["Host"] ?: "localhost:8080"
        val protocol = if (host.contains("localhost")) "http" else "https"
        val apiBase = if (host.contains("localhost")) "$protocol://$host" else "$protocol://$host"
        val imageUrl = "$apiBase/api/share/$idStr"
        val shareUrl = "$apiBase/share/$idStr"

        val html = """
            <!DOCTYPE html>
            <html lang="pt-br">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$eventName - ${profile.name}</title>
                <meta property="og:title" content="$eventName - ${profile.name}">
                <meta property="og:description" content="Veja minhas escolhas para o $eventName!">
                <meta property="og:image" content="$imageUrl">
                <meta property="og:image:type" content="image/png">
                <meta property="og:image:width" content="1200">
                <meta property="og:image:height" content="630">
                <meta property="og:type" content="website">
                <meta property="og:url" content="$shareUrl">
                <meta name="twitter:card" content="summary_large_image">
                <meta name="twitter:title" content="$eventName - ${profile.name}">
                <meta name="twitter:description" content="Veja minhas escolhas para o $eventName!">
                <meta name="twitter:image" content="$imageUrl">
                <style>
                    body { background: #121214; color: white; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 100vh; margin: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 20px; box-sizing: border-box; text-align: center; }
                    img { max-width: 100%; max-height: 70vh; border-radius: 12px; box-shadow: 0 20px 50px rgba(0,0,0,0.5); border: 1px solid #333; margin-bottom: 20px; }
                    .btn { margin: 10px; padding: 15px 30px; background: #9696ff; color: white; text-decoration: none; border-radius: 8px; font-weight: bold; transition: transform 0.2s; display: inline-block; min-width: 200px; }
                    .btn:hover { transform: scale(1.05); background: #7a7aff; }
                    .btn-secondary { background: #333; }
                    .btn-secondary:hover { background: #444; }
                    h1 { margin-bottom: 5px; font-size: 2.5rem; }
                    p { margin-top: 0; color: #aaa; margin-bottom: 30px; font-size: 1.2rem; }
                    .container { max-width: 800px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>$eventName</h1>
                    <p>Indicações de ${profile.name}</p>
                    <img src="$imageUrl" alt="Resumo de Votação">
                    <br>
                    <a href="$imageUrl" class="btn" download="${profile.username}-huki-awards.png">Baixar Imagem</a>
                    <a href="/" class="btn btn-secondary">Quero votar também!</a>
                </div>
            </body>
            </html>
        """.trimIndent()
        call.respondText(html, ContentType.Text.Html)
    }

    route(Api.path) {
        imageRoutes(imageService)

        // Public share route (PNG)
        get("${Share.path}/{id}") {
            val idStr = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val userId = try {
                UUID.fromString(idStr)
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.BadRequest, "Invalid User ID")
            }

            // Fetch data
            val profile = transaction {
                UsersTable.selectAll().where { UsersTable.id eq userId }.map {
                    UserProfile(
                        id = it[UsersTable.id].toString(),
                        name = it[UsersTable.name] ?: "",
                        username = it[UsersTable.username] ?: "",
                        avatarUrl = it[UsersTable.avatarUrl] ?: ""
                    )
                }.singleOrNull()
            } ?: return@get call.respond(HttpStatusCode.NotFound, "User profile not found")

            val categories = transaction {
                CategoriesTable.selectAll().orderBy(CategoriesTable.weight to SortOrder.ASC)
                    .map {
                        Category(
                            id = it[CategoriesTable.id].toString(),
                            name = it[CategoriesTable.name] ?: "",
                            description = it[CategoriesTable.description] ?: "",
                            weight = it[CategoriesTable.weight]
                        )
                    }
            }

            val votes = transaction {
                VotesTable.selectAll().where { VotesTable.userId eq userId }.map {
                    VoteRequest(
                        categoryId = it[VotesTable.categoryId].toString(),
                        igdbGameId = it[VotesTable.igdbGameId],
                        gameName = it[VotesTable.gameName] ?: "",
                        gameCoverUrl = it[VotesTable.gameCoverUrl]
                    )
                }
            }

            val settings = transaction {
                SettingsTable.selectAll().singleOrNull()
            }
            val eventName = settings?.get(SettingsTable.eventName) ?: "Huki Awards 2026"

            // Generate Image
            val image = imageService.generateVotingSummary(eventName, profile, categories, votes)

            // Respond with bytes
            val outputStream = ByteArrayOutputStream()
            ImageIO.write(image, "png", outputStream)
            call.respondBytes(outputStream.toByteArray(), ContentType.Image.PNG)
        }

        get(Stats.path) {
            val stats = transaction {
                val totalVotes = VotesTable.selectAll().count()
                val uniqueVoters = VotesTable.select(VotesTable.userId).withDistinct().count()

                val categoryStats = (CategoriesTable leftJoin VotesTable)
                    .select(CategoriesTable.name, VotesTable.id.count())
                    .groupBy(CategoriesTable.name)
                    .orderBy(CategoriesTable.name to SortOrder.ASC)
                    .map {
                        CategoryStat(
                            categoryName = it[CategoriesTable.name] ?: "Unknown",
                            voteCount = it[VotesTable.id.count()]
                        )
                    }

                GlobalStats(totalVotes, uniqueVoters, categoryStats)
            }
            call.respond(stats)
        }

        categoryRoutes(igdbService)
        searchRoutes(igdbService)
        settingsRoutes()
        profileRoutes()
        voteRoutes()
        adminRoutes(imageService)
    }
}

private fun Route.imageRoutes(imageService: ImageService) {
    authenticate(AUTH_JWT) {
        get(Share.path) {
            val user = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            // Fetch data
            val profile = transaction {
                UsersTable.selectAll().where { UsersTable.id eq user.userId }.map {
                    UserProfile(
                        id = it[UsersTable.id].toString(),
                        name = it[UsersTable.name] ?: "",
                        username = it[UsersTable.username] ?: "",
                        avatarUrl = it[UsersTable.avatarUrl] ?: ""
                    )
                }.singleOrNull()
            } ?: return@get call.respond(HttpStatusCode.NotFound, "User profile not found")

            val categories = transaction {
                CategoriesTable.selectAll().orderBy(CategoriesTable.weight to SortOrder.ASC)
                    .map {
                        Category(
                            id = it[CategoriesTable.id].toString(),
                            name = it[CategoriesTable.name] ?: "",
                            description = it[CategoriesTable.description] ?: "",
                            weight = it[CategoriesTable.weight]
                        )
                    }
            }

            val votes = transaction {
                VotesTable.selectAll().where { VotesTable.userId eq user.userId }.map {
                    VoteRequest(
                        categoryId = it[VotesTable.categoryId].toString(),
                        igdbGameId = it[VotesTable.igdbGameId],
                        gameName = it[VotesTable.gameName] ?: "",
                        gameCoverUrl = it[VotesTable.gameCoverUrl]
                    )
                }
            }

            val settings = transaction {
                SettingsTable.selectAll().singleOrNull()
            }
            val eventName = settings?.get(SettingsTable.eventName) ?: "Huki Awards 2026"

            // Generate Image
            val image = imageService.generateVotingSummary(eventName, profile, categories, votes)

            // Respond with bytes
            val outputStream = ByteArrayOutputStream()
            ImageIO.write(image, "png", outputStream)
            call.respondBytes(outputStream.toByteArray(), ContentType.Image.PNG)
        }
    }
}

private fun Route.categoryRoutes(igdbService: IgdbService) {
    route(Categories.path) {
        get {
            val categories = transaction {
                CategoriesTable.selectAll().orderBy(CategoriesTable.weight to SortOrder.ASC)
                    .map {
                        Category(
                            id = it[CategoriesTable.id].toString(),
                            name = it[CategoriesTable.name] ?: "",
                            description = it[CategoriesTable.description] ?: "",
                            weight = it[CategoriesTable.weight]
                        )
                    }
            }
            call.respond(categories)
        }

        // Dynamic Top 10 Leaderboard
        get("/{id}/top10") {
            val catId = UUID.fromString(call.parameters["id"])
            val topGameIdsWithCounts = transaction {
                val countAlias = VotesTable.id.count()
                VotesTable
                    .select(VotesTable.igdbGameId, countAlias)
                    .where { VotesTable.categoryId eq catId }
                    .groupBy(VotesTable.igdbGameId)
                    .orderBy(countAlias to SortOrder.DESC)
                    .limit(10)
                    .map { row ->
                        row[VotesTable.igdbGameId] to row[countAlias].toInt()
                    }
            }

            if (topGameIdsWithCounts.isEmpty()) {
                call.respond(emptyList<LeaderboardEntry>())
                return@get
            }

            val gameIds = topGameIdsWithCounts.map { it.first }
            val igdbMetadata = igdbService.fetchGamesByIds(gameIds)

            val results = topGameIdsWithCounts.mapNotNull { (gameId, voteCount) ->
                val gameMeta = igdbMetadata.find { it.id == gameId } ?: return@mapNotNull null
                LeaderboardEntry(
                    igdbGameId = gameId,
                    title = gameMeta.name,
                    boxArtUrl = gameMeta.coverUrl,
                    voteCount = voteCount
                )
            }
            call.respond(results)
        }
    }
}

private fun Route.searchRoutes(igdbService: IgdbService) {
    get(Search.path) {
        val query = call.request.queryParameters["q"]
        if (query.isNullOrBlank()) {
            return@get call.respond(
                HttpStatusCode.BadRequest,
                "Missing search query parameter 'q'"
            )
        }
        try {
            val games = igdbService.searchGames(query)
            call.respond(games)
        } catch (e: Exception) {
            logger.error { "Failed to resolve metadata from IGDB: ${e.message}" }
            call.respond(
                HttpStatusCode.InternalServerError,
                "Failed to resolve metadata from IGDB"
            )
        }
    }
}

private val versionProperties = java.util.Properties().apply {
    val resource =
        Thread.currentThread().contextClassLoader.getResourceAsStream("version.properties")
    if (resource != null) {
        load(resource)
    }
}
private val currentVersion = versionProperties.getProperty("version") ?: "1.0.0"

private fun Route.settingsRoutes() {
    get(Settings.path) {
        val settings = transaction {
            SettingsTable.selectAll().map {
                Settings(
                    eventName = it[SettingsTable.eventName],
                    votingStart = it.getOrNull(SettingsTable.votingStart),
                    votingEnd = it.getOrNull(SettingsTable.votingEnd),
                    isVotingOpen = it[SettingsTable.isVotingOpen],
                    showDatesToUsers = it[SettingsTable.showDatesToUsers],
                    phase = it[SettingsTable.phase],
                    logoUrl = it[SettingsTable.logoUrl],
                    faviconUrl = it[SettingsTable.faviconUrl],
                    version = currentVersion
                )
            }.singleOrNull() ?: Settings(version = currentVersion)
        }
        call.respond(settings)
    }
}

private fun Route.profileRoutes() {
    authenticate(AUTH_JWT) {
        get(Profile.path) {
            val user = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val profile = transaction {
                UsersTable.selectAll().where { UsersTable.id eq user.userId }.map {
                    UserProfile(
                        id = it[UsersTable.id].toString(),
                        name = it[UsersTable.name] ?: "",
                        username = it[UsersTable.username] ?: "",
                        avatarUrl = it[UsersTable.avatarUrl] ?: "",
                    )
                }.singleOrNull()
            }
            if (profile != null) call.respond(profile)
            else call.respond(HttpStatusCode.NotFound, "User profile not found")
        }
    }

    get(Profile.byId()) {
        val id = call.parameters["id"]
        if (id.isNullOrBlank()) return@get call.respond(
            HttpStatusCode.BadRequest,
            "Missing profile ID"
        )
        try {
            val userId = UUID.fromString(id)
            val profile = transaction {
                UsersTable.selectAll().where { UsersTable.id eq userId }.map {
                    UserProfile(
                        id = it[UsersTable.id].toString(),
                        name = it[UsersTable.name] ?: "",
                        username = it[UsersTable.username] ?: "",
                        avatarUrl = it[UsersTable.avatarUrl] ?: "",
                    )
                }.singleOrNull()
            }
            if (profile != null) call.respond(profile)
            else call.respond(HttpStatusCode.NotFound, "User not found")
        } catch (e: Exception) {
            logger.error { "Failed to parse profile ID: ${e.message}" }
            call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
        }
    }
}

private fun Route.voteRoutes() {
    authenticate(AUTH_JWT) {
        get(Votes.path + "/my") {
            val user = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val targetUserId = if (user.role.uppercase() == "ADMIN") {
                call.request.queryParameters["targetUserId"]?.let { UUID.fromString(it) }
                    ?: user.userId
            } else {
                user.userId
            }

            val myVotes = transaction {
                try {
                    VotesTable.selectAll().where { VotesTable.userId eq targetUserId }.map {
                        VoteRequest(
                            categoryId = it[VotesTable.categoryId].toString(),
                            igdbGameId = it[VotesTable.igdbGameId],
                            gameName = it[VotesTable.gameName] ?: "",
                            gameCoverUrl = it[VotesTable.gameCoverUrl]
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Error fetching votes for user $targetUserId", e)
                    emptyList()
                }
            }
            call.respond(myVotes)
        }

        delete(Votes.path + "/my") {
            val user = call.principal<UserPrincipal>() ?: return@delete call.respond(
                HttpStatusCode.Unauthorized
            )
            transaction {
                VotesTable.deleteWhere { VotesTable.userId eq user.userId }
            }
            call.respond(HttpStatusCode.NoContent, "")
        }

        post(Vote.path) {
            val user = call.principal<UserPrincipal>() ?: return@post call.respond(
                HttpStatusCode.Unauthorized
            )
            val request = call.receive<VoteRequest>()
            logger.info { "User ${user.userId} attempting to vote for category ${request.categoryId} on target ${request.targetUserId}" }

            val catId = try {
                UUID.fromString(request.categoryId)
            } catch (e: Exception) {
                return@post call.respond(HttpStatusCode.BadRequest, "Invalid category ID")
            }

            // If targetUserId is provided and the caller is an admin, vote for that user
            val targetUserId =
                if (request.targetUserId != null && user.role.uppercase() == "ADMIN") {
                    try {
                        UUID.fromString(request.targetUserId)
                    } catch (e: Exception) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            "Invalid target user ID"
                        )
                    }
                } else {
                    user.userId
                }

            val voteResult = try {
                newSuspendedTransaction {
                    val settings = SettingsTable.selectAll().singleOrNull()
                    val isVotingOpen = settings?.get(SettingsTable.isVotingOpen) ?: true

                    if (!isVotingOpen && user.role.uppercase() != "ADMIN") {
                        return@newSuspendedTransaction "CLOSED"
                    }

                    val existingVote = VotesTable.selectAll()
                        .where { (VotesTable.userId eq targetUserId) and (VotesTable.categoryId eq catId) }
                        .singleOrNull()

                    if (existingVote != null) {
                        VotesTable.update({ (VotesTable.userId eq targetUserId) and (VotesTable.categoryId eq catId) }) {
                            it[VotesTable.igdbGameId] = request.igdbGameId
                            it[VotesTable.gameName] = request.gameName
                            it[VotesTable.gameCoverUrl] = request.gameCoverUrl
                        }
                        "UPDATED"
                    } else {
                        VotesTable.insert {
                            it[VotesTable.userId] = targetUserId
                            it[VotesTable.categoryId] = catId
                            it[VotesTable.igdbGameId] = request.igdbGameId
                            it[VotesTable.gameName] = request.gameName
                            it[VotesTable.gameCoverUrl] = request.gameCoverUrl
                        }
                        "SUCCESS"
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to process vote" }
                "ERROR"
            }

            when (voteResult) {
                "UPDATED" -> call.respond(HttpStatusCode.OK, request)
                "CLOSED" -> call.respond(HttpStatusCode.Forbidden, "Voting is closed.")
                "ERROR" -> call.respond(
                    HttpStatusCode.InternalServerError,
                    "Failed to process vote."
                )

                else -> call.respond(HttpStatusCode.Created, request)
            }
        }
    }
}

private fun Route.adminRoutes(imageService: ImageService) {
    authenticate(AUTH_JWT) {
        route(Admin.path) {
            intercept(ApplicationCallPipeline.Call) {
                val user = call.principal<UserPrincipal>()
                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Missing or invalid principal")
                    finish()
                } else if (user.role.uppercase() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, "Admins only")
                    finish()
                } else {
                    proceed()
                }
            }

            route(Users.path) {
                get {
                    val users = transaction {
                        UsersTable.selectAll().map {
                            UserProfile(
                                id = it[UsersTable.id].toString(),
                                name = it[UsersTable.name] ?: "",
                                username = it[UsersTable.username] ?: "",
                                avatarUrl = it[UsersTable.avatarUrl] ?: ""
                            )
                        }
                    }
                    call.respond(users)
                }
            }

            route(Admins.path) {
                get {
                    val admins = transaction {
                        AdminsTable.selectAll().map { it[AdminsTable.username] }
                    }
                    call.respond(admins)
                }

                post("/{username}") {
                    val targetUsername = call.parameters["username"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest)
                    transaction {
                        if (AdminsTable.selectAll()
                                .where { AdminsTable.username eq targetUsername }.count() == 0L
                        ) {
                            AdminsTable.insert { it[username] = targetUsername }
                            UsersTable.update({ UsersTable.username eq targetUsername }) {
                                it[role] = Roles.ADMIN.name
                            }
                        }
                    }
                    val user = call.principal<UserPrincipal>()
                    logAdminAction(
                        user?.username ?: "unknown",
                        "PROMOTE_ADMIN",
                        target = targetUsername
                    )
                    call.respond(HttpStatusCode.Created, "User promoted to admin")
                }

                delete("/{username}") {
                    val targetUsername = call.parameters["username"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    val defaultAdmins = listOf("onlyashd", "hukizan", "sub0")

                    if (defaultAdmins.contains(targetUsername.lowercase())) {
                        return@delete call.respond(
                            HttpStatusCode.Forbidden,
                            "Cannot demote default admins"
                        )
                    }

                    transaction {
                        AdminsTable.deleteWhere { username eq targetUsername }
                        UsersTable.update({ UsersTable.username eq targetUsername }) {
                            it[role] = Roles.USER.name
                        }
                    }
                    val user = call.principal<UserPrincipal>()
                    logAdminAction(
                        user?.username ?: "unknown",
                        "DEMOTE_ADMIN",
                        target = targetUsername
                    )
                    call.respond(HttpStatusCode.OK, "User demoted from admin")
                }
            }

            route(Settings.path) {
                put {
                    val request = call.receive<Settings>()
                    transaction {
                        SettingsTable.update {
                            it[eventName] = request.eventName
                            it[votingStart] = request.votingStart
                            it[votingEnd] = request.votingEnd
                            it[isVotingOpen] = request.isVotingOpen
                            it[showDatesToUsers] = request.showDatesToUsers
                            it[phase] = request.phase
                            it[logoUrl] = request.logoUrl
                            it[faviconUrl] = request.faviconUrl
                        }
                    }
                    val user = call.principal<UserPrincipal>()
                    logAdminAction(
                        user?.username ?: "unknown",
                        "UPDATE_SETTINGS",
                        details = "Event: ${request.eventName}"
                    )
                    call.respond(HttpStatusCode.OK, "Settings updated")
                }
            }

            route(Categories.path) {
                post("/reorder") {
                    val categoryIds = call.receive<List<String>>()
                    transaction {
                        categoryIds.forEachIndexed { index, id ->
                            CategoriesTable.update({ CategoriesTable.id eq UUID.fromString(id) }) {
                                it[weight] = index
                            }
                        }
                    }
                    val user = call.principal<UserPrincipal>()
                    logAdminAction(user?.username ?: "unknown", "REORDER_CATEGORIES")
                    call.respond(HttpStatusCode.OK, "Categories reordered")
                }
            }

            route(Categories.path) {
                post {
                    val request = call.receive<CategoryRequest>()
                    val newId = transaction {
                        CategoriesTable.insert {
                            it[name] = request.name
                            it[description] = request.description
                            it[weight] = request.weight
                        } get CategoriesTable.id
                    }
                    val user = call.principal<UserPrincipal>()
                    logAdminAction(
                        user?.username ?: "unknown",
                        "CREATE_CATEGORY",
                        target = request.name
                    )
                    call.respond(HttpStatusCode.Created, mapOf("id" to newId.toString()))
                }

                put("/{id}") {
                    val catId = UUID.fromString(call.parameters["id"])
                    val request = call.receive<CategoryRequest>()
                    transaction {
                        CategoriesTable.update({ CategoriesTable.id eq catId }) {
                            it[name] = request.name
                            it[description] = request.description
                            it[weight] = request.weight
                        }
                    }
                    val user = call.principal<UserPrincipal>()
                    logAdminAction(
                        user?.username ?: "unknown",
                        "UPDATE_CATEGORY",
                        target = request.name
                    )
                    call.respond(HttpStatusCode.OK, "Category updated")
                }

                delete("/{id}") {
                    val catIdStr = call.parameters["id"]!!
                    val catId = UUID.fromString(catIdStr)
                    val catName = transaction {
                        CategoriesTable.selectAll().where { CategoriesTable.id eq catId }
                            .map { it[CategoriesTable.name] }.singleOrNull()
                    }
                    transaction {
                        CategoriesTable.deleteWhere { id eq catId }
                    }
                    val user = call.principal<UserPrincipal>()
                    logAdminAction(
                        user?.username ?: "unknown",
                        "DELETE_CATEGORY",
                        target = catName ?: catIdStr
                    )
                    call.respond(HttpStatusCode.NoContent, "")
                }
            }

            delete(Votes.byId()) {
                val voteId = UUID.fromString(call.parameters["id"])
                transaction { VotesTable.deleteWhere { id eq voteId } }
                call.respond(HttpStatusCode.NoContent, "")
            }

            route(Votes.path) {
                get("/export") {
                    val csv = transaction {
                        val votes = (VotesTable innerJoin UsersTable innerJoin CategoriesTable)
                            .selectAll()
                            .orderBy(CategoriesTable.name to SortOrder.ASC)
                            .map {
                                listOf(
                                    it[UsersTable.name] ?: "",
                                    it[UsersTable.username] ?: "",
                                    it[CategoriesTable.name] ?: "",
                                    it[VotesTable.gameName] ?: "",
                                    it[VotesTable.igdbGameId].toString()
                                ).joinToString(",") { field ->
                                    "\"${field.replace("\"", "\"\"")}\""
                                }
                            }
                        val header = "User Name,Username,Category,Game Name,IGDB ID"
                        (listOf(header) + votes).joinToString("\n")
                    }

                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Attachment.withParameter(
                            ContentDisposition.Parameters.FileName,
                            "votes_export.csv"
                        )
                            .toString()
                    )
                    call.respondText(csv, ContentType.Text.CSV)
                }

                delete {
                    transaction { VotesTable.deleteAll() }
                    call.respond(HttpStatusCode.NoContent, "")
                }
            }

            get("${Categories.path}/{id}/share") {
                val user = call.principal<UserPrincipal>() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized
                )
                if (user.role != "ADMIN") return@get call.respond(HttpStatusCode.Forbidden)

                val categoryIdStr =
                    call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val categoryId = UUID.fromString(categoryIdStr)

                val categoryName = transaction {
                    CategoriesTable.selectAll().where { CategoriesTable.id eq categoryId }.map {
                        it[CategoriesTable.name]
                    }.singleOrNull()
                } ?: return@get call.respond(HttpStatusCode.NotFound)

                val leaderboard = transaction {
                    val countAlias = VotesTable.id.count()
                    VotesTable.select(
                        VotesTable.igdbGameId,
                        VotesTable.gameName,
                        VotesTable.gameCoverUrl,
                        countAlias
                    )
                        .where { VotesTable.categoryId eq categoryId }
                        .groupBy(
                            VotesTable.igdbGameId,
                            VotesTable.gameName,
                            VotesTable.gameCoverUrl
                        )
                        .orderBy(countAlias to SortOrder.DESC)
                        .map {
                            LeaderboardEntry(
                                igdbGameId = it[VotesTable.igdbGameId],
                                title = it[VotesTable.gameName] ?: "Desconhecido",
                                boxArtUrl = it[VotesTable.gameCoverUrl] ?: "",
                                voteCount = it[countAlias].toInt()
                            )
                        }
                }

                val settings = transaction {
                    SettingsTable.selectAll().singleOrNull()
                }
                val eventName = settings?.get(SettingsTable.eventName) ?: "Huki Awards 2026"

                val image = imageService.generateLeaderboard(eventName, categoryName, leaderboard)
                val outputStream = ByteArrayOutputStream()
                ImageIO.write(image, "png", outputStream)
                call.respondBytes(outputStream.toByteArray(), ContentType.Image.PNG)
            }

            get("${Categories.path}/{id}/winner") {
                val user = call.principal<UserPrincipal>() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized
                )
                if (user.role != "ADMIN") return@get call.respond(HttpStatusCode.Forbidden)

                val categoryIdStr =
                    call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val categoryId = UUID.fromString(categoryIdStr)

                val categoryName = transaction {
                    CategoriesTable.selectAll().where { CategoriesTable.id eq categoryId }.map {
                        it[CategoriesTable.name]
                    }.singleOrNull()
                } ?: return@get call.respond(HttpStatusCode.NotFound)

                val winner = transaction {
                    val countAlias = VotesTable.id.count()
                    VotesTable.select(
                        VotesTable.igdbGameId,
                        VotesTable.gameName,
                        VotesTable.gameCoverUrl,
                        countAlias
                    )
                        .where { VotesTable.categoryId eq categoryId }
                        .groupBy(
                            VotesTable.igdbGameId,
                            VotesTable.gameName,
                            VotesTable.gameCoverUrl
                        )
                        .orderBy(countAlias to SortOrder.DESC)
                        .limit(1)
                        .map {
                            LeaderboardEntry(
                                igdbGameId = it[VotesTable.igdbGameId],
                                title = it[VotesTable.gameName] ?: "Desconhecido",
                                boxArtUrl = it[VotesTable.gameCoverUrl] ?: "",
                                voteCount = it[countAlias].toInt()
                            )
                        }.singleOrNull()
                } ?: return@get call.respond(HttpStatusCode.NotFound, "No votes for this category")

                val settings = transaction {
                    SettingsTable.selectAll().singleOrNull()
                }
                val eventName = settings?.get(SettingsTable.eventName) ?: "Huki Awards 2026"

                val image = imageService.generateWinnerCard(eventName, categoryName, winner)
                val outputStream = ByteArrayOutputStream()
                ImageIO.write(image, "png", outputStream)
                call.respondBytes(outputStream.toByteArray(), ContentType.Image.PNG)
            }

            route("/audit") {
                get {
                    val logs = transaction {
                        AuditLogsTable.selectAll()
                            .orderBy(AuditLogsTable.timestamp to SortOrder.DESC)
                            .limit(100)
                            .map {
                                AuditLog(
                                    id = it[AuditLogsTable.id].toString(),
                                    timestamp = it[AuditLogsTable.timestamp].format(
                                        DateTimeFormatter.ISO_DATE_TIME
                                    ),
                                    adminUsername = it[AuditLogsTable.adminUsername],
                                    action = it[AuditLogsTable.action],
                                    target = it[AuditLogsTable.target],
                                    details = it[AuditLogsTable.details]
                                )
                            }
                    }
                    call.respond(logs)
                }
            }
        }
    }
}

fun logAdminAction(admin: String, action: String, target: String? = null, details: String? = null) {
    transaction {
        AuditLogsTable.insert {
            it[adminUsername] = admin
            it[this.action] = action
            it[this.target] = target
            it[this.details] = details
        }
    }
}
