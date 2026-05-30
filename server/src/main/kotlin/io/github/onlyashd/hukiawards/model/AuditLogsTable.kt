package io.github.onlyashd.hukiawards.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object AuditLogsTable : Table("audit_logs") {
    val id = uuid("id").autoGenerate()
    val timestamp = datetime("timestamp").default(LocalDateTime.now())
    val adminUsername = text("admin_username")
    val action = text("action")
    val target = text("target").nullable()
    val details = text("details").nullable()

    override val primaryKey = PrimaryKey(id)
}
