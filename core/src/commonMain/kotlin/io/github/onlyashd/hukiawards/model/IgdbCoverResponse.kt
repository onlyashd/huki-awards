package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
data class IgdbCoverResponse(
    val id: Long,
    val url: String
)
