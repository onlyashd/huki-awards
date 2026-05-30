package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
data class AuditLog(
    val id: String? = null,
    val timestamp: String,
    val adminUsername: String,
    val action: String,
    val target: String?,
    val details: String?
)
