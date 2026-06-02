package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val username: String,
    val avatarUrl: String? = null,
    val discordId: String? = null,
)
