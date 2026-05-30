package io.github.onlyashd.hukiawards.model

import java.util.UUID

data class UserPrincipal(
    val userId: UUID,
    val username: String,
    val role: String
)
