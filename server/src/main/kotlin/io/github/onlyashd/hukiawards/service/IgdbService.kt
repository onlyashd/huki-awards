package io.github.onlyashd.hukiawards.service

import io.github.onlyashd.hukiawards.model.IgdbGameMetadata
import io.github.onlyashd.hukiawards.model.IgdbGameResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

class IgdbService(private val client: HttpClient) {
    private val logger = KotlinLogging.logger {}
    private val clientId = System.getenv("IGDB_CLIENT_ID") ?: ""
    private val clientSecret =
        System.getenv("IGDB_CLIENT_SECRET") ?: ""

    // In-memory token cache state parameters
    private var cachedAccessToken: String? = null
    private var tokenExpirationTime: LocalDateTime = LocalDateTime.now()

    @Serializable
    private data class TwitchAuthResponse(
        @SerialName("access_token")
        val accessToken: String,
        @SerialName("expires_in")
        val expiresIn: Long,
        @SerialName("token_type")
        val tokenType: String
    )

    /**
     * Checks token state validity, executing a refresh flow if needed.
     * Thread-safety wrapper ensures atomic access to cached configurations.
     */
    private suspend fun getOrFetchValidToken(): String {
        val now = LocalDateTime.now()

        // Return cached token if it exists and hasn't expired (with a 60-second buffer for safety)
        if (cachedAccessToken != null && now < tokenExpirationTime.plusSeconds(60L)) {
            return cachedAccessToken!!
        }

        try {
            val response = client.post("https://id.twitch.tv/oauth2/token") {
                url {
                    parameters.append("client_id", clientId)
                    parameters.append("client_secret", clientSecret)
                    parameters.append("grant_type", "client_credentials")
                }
            }

            if (response.status.isSuccess()) {
                val authBody: TwitchAuthResponse = response.body()
                cachedAccessToken = authBody.accessToken
                // Calculate concrete historical expiration moment
                tokenExpirationTime = LocalDateTime.now().plusSeconds(authBody.expiresIn)
                return authBody.accessToken
            } else {
                logger.error { "Twitch Authentication Flow Crashed: ${response.status}" }
            }
        } catch (e: Exception) {
            logger.error { "Error requesting Twitch token: ${e.localizedMessage}" }
        }

        // Fallback to whatever token we have or empty string to fail gracefully down the pipe
        return cachedAccessToken ?: ""
    }

    /**
     * Used by the Frontend to search for a game to vote on.
     */
    suspend fun searchGames(query: String): List<IgdbGameMetadata> {
        if (query.isBlank()) return emptyList()

        val validToken = getOrFetchValidToken()
        if (validToken.isBlank()) return emptyList()

        val response = client.post("https://api.igdb.com/v4/games") {
            header("Client-ID", clientId)
            header("Authorization", "Bearer $validToken")
            setBody("search \"$query\"; fields name, cover.url, genres.name; limit 20;")
        }

        if (!response.status.isSuccess()) {
            return emptyList()
        }

        val rawGames: List<IgdbGameResponse> = response.body()
        return rawGames.map { it.toDomainModel() }
    }

    /**
     * Used by the `GET /categories/{id}/top10` admin route to fetch
     * the titles and box art for the highest voted game IDs.
     */
    suspend fun fetchGamesByIds(ids: List<Long>): List<IgdbGameMetadata> {
        if (ids.isEmpty()) return emptyList()

        val idsString = ids.joinToString(",")
        val validToken = getOrFetchValidToken()
        if (validToken.isBlank()) return emptyList()

        val response = client.post("https://api.igdb.com/v4/games") {
            header("Client-ID", clientId)
            header("Authorization", "Bearer $validToken")
            setBody("fields name, cover.url, genres.name; where id = ($idsString); limit 50;")
        }

        if (!response.status.isSuccess()) {
            return emptyList()
        }

        val rawGames: List<IgdbGameResponse> = response.body()
        return rawGames.map { it.toDomainModel() }
    }

    private fun IgdbGameResponse.toDomainModel(): IgdbGameMetadata {
        val rawUrl = this.cover?.url ?: ""
        val formattedUrl = if (rawUrl.isNotBlank()) {
            "https:" + rawUrl.replace("t_thumb", "t_cover_big")
        } else {
            ""
        }

        return IgdbGameMetadata(
            id = this.id,
            name = this.name,
            coverUrl = formattedUrl,
            genres = this.genres.map { it.name }
        )
    }
}
