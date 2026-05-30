package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
enum class AuthProvider {
    DISCORD,
    TWITCH,
}