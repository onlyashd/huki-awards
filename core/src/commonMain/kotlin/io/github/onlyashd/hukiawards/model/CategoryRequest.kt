package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryRequest(
    val name: String,
    val description: String?,
    val weight: Int,
)