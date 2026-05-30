package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
data class VoteRequest(
    val categoryId: String,
    val igdbGameId: Long,
    val gameName: String,
    val gameCoverUrl: String? = null,
    val targetUserId: String? = null
)
