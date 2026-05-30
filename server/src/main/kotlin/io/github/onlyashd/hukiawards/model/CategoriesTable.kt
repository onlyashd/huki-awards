package io.github.onlyashd.hukiawards.model

import org.jetbrains.exposed.sql.Table

object CategoriesTable : Table("categories") {
    val id = uuid("id").autoGenerate()
    val name = text("name").nullable()
    val description = text("description").nullable()
    val weight = integer("weight").default(0)

    override val primaryKey = PrimaryKey(id)
}
