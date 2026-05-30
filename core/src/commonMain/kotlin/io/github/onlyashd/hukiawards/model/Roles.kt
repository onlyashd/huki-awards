package io.github.onlyashd.hukiawards.model

import kotlinx.serialization.Serializable

@Serializable
enum class Roles {
    ADMIN,
    USER,
    UNKNOWN,
}