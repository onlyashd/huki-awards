package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
data class IgdbGameResponse(
    val id: Long,
    val name: String,
    val cover: IgdbCoverResponse? = null,
    val genres: List<IgdbGenreResponse> = emptyList()
)
