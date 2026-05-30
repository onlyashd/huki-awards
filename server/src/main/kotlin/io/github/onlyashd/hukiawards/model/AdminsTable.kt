package io.github.onlyashd.hukiawards.model

import org.jetbrains.exposed.sql.Table

object AdminsTable : Table("admins") {
    val id = integer("id").autoIncrement()
    val username = text("username").uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}
