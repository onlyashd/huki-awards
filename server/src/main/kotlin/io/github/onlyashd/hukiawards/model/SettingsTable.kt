package io.github.onlyashd.hukiawards.model

import org.jetbrains.exposed.sql.Table

object SettingsTable : Table("settings") {
    val id = integer("id").autoIncrement()
    val eventName = text("event_name").default("Huki Awards 2026")
    val votingStart = text("voting_start").nullable()
    val votingEnd = text("voting_end").nullable()
    val isVotingOpen = bool("is_voting_open").default(true)
    val showDatesToUsers = bool("show_dates_to_users").default(true)
    val phase = text("phase").default("NOMINATION")
    val logoUrl = text("logo_url").nullable()
    val faviconUrl = text("favicon_url").nullable()

    override val primaryKey = PrimaryKey(id)
}
