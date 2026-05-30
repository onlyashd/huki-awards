package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
data class GlobalStats(
    val totalVotes: Long,
    val uniqueVoters: Long,
    val categoryStats: List<CategoryStat>
)

@Serializable
data class CategoryStat(
    val categoryName: String,
    val voteCount: Long
)
