package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val igdbGameId: Long,
    val title: String,
    val boxArtUrl: String,
    val voteCount: Int
)