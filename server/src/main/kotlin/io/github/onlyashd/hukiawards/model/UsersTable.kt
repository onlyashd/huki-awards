package io.github.onlyashd.hukiawards.model

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val username = text("username").nullable()
    val name = text("name").nullable()
    val avatarUrl = text("avatar_url").nullable()
    val provider = text("provider").default("DISCORD")
    val role = text("role").default("USER")
    val discordId = text("discord_id").nullable()

    override val primaryKey = PrimaryKey(id)
}
