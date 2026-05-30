package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordUser(
    val id: String,
    val username: String,
    @SerialName("global_name")
    val name: String? = null,
    @SerialName("avatar")
    val avatarUrl: String? = null,
)
