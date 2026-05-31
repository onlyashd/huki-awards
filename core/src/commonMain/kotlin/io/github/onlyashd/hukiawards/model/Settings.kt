package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val eventName: String = "Huki Awards 2026",
    val votingStart: String? = null, // ISO-8601
    val votingEnd: String? = null,   // ISO-8601
    val isVotingOpen: Boolean = true,
    val showDatesToUsers: Boolean = true,
    val phase: String = "NOMINATION", // NOMINATION or VOTING
    val logoUrl: String? = null,
    val faviconUrl: String? = null,
    val version: String = "1.0.0"
)
