package io.github.onlyashd.hukiawards.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
data class JwtPayload(
    val id: String = "",
    val role: String = "user"
)

private val json = Json { ignoreUnknownKeys = true }

@OptIn(ExperimentalEncodingApi::class)
fun getRoleFromToken(token: String): Pair<String?, String> {
    try {
        // JWT format: header.payload.signature
        val parts = token.split(".")
        if (parts.size < 2) return Pair(null, "user")

        var payloadBase64 = parts[1]
        // Add padding if missing
        while (payloadBase64.length % 4 != 0) {
            payloadBase64 += "="
        }
        
        val jsonString = Base64.decode(payloadBase64).decodeToString()
        val payload = json.decodeFromString<JwtPayload>(jsonString)
        return Pair(payload.id, payload.role)
    } catch (e: Exception) {
        println("JWT Decode Error: ${e.message}")
        return Pair(null, "user") // Default fallback
    }
}
