package io.github.onlyashd.hukiawards.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
data class JwtPayload(
    val aud: String = "",
    val iss: String = "",
    val id: String = "",
    val exp: Long = 0,
    val role: String
)

@OptIn(ExperimentalEncodingApi::class)
fun getRoleFromToken(token: String): Pair<String?, String> {
    try {
        // JWT format: header.payload.signature
        val payloadBase64 = token.split(".")[1]
        val jsonString = Base64.decode(payloadBase64).decodeToString()
        val role = Json.decodeFromString<JwtPayload>(jsonString).role
        val id = Json.decodeFromString<JwtPayload>(jsonString).id
        return Pair(id, role)
    } catch (e: Exception) {
        print(e.stackTraceToString())
        return Pair(null, "user") // Default fallback
    }
}
