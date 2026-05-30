package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String? = null,
    val name: String,
    val description: String,
    val weight: Int // For sorting categories
)
