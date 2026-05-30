package io.github.onlyashd.hukiawards.model

import org.jetbrains.exposed.sql.Table

object VotesTable : Table("votes") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val categoryId = uuid("category_id").references(CategoriesTable.id)
    val igdbGameId = long("igdb_game_id") // Stores the raw IGDB ID directly
    val gameName = text("game_name").nullable()
    val gameCoverUrl = text("game_cover_url").nullable()

    override val primaryKey = PrimaryKey(id)
}
