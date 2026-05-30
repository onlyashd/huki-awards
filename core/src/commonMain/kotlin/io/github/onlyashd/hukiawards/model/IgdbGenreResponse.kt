package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
data class IgdbGenreResponse(
    val id: Long,
    val name: String
)
