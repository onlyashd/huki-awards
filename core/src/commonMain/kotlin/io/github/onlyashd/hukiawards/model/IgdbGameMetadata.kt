package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
data class IgdbGameMetadata(
    val id: Long,
    val name: String,
    val coverUrl: String,
    val genres: List<String> = emptyList()
)
