package io.github.onlyashd.hukiawards.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun String.formatToFriendlyDateTime(): String {
    if (this.isBlank()) return this
    return try {
        // Try as Instant first (standard ISO with Z or offset)
        val instant = Instant.parse(this)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        dateTime.formatToFriendly()
    } catch (e: Exception) {
        try {
            // Try as LocalDateTime (ISO without Z/offset, e.g. 2023-12-31T23:59:59)
            val dateTime = kotlinx.datetime.LocalDateTime.parse(this)
            dateTime.formatToFriendly()
        } catch (e2: Exception) {
            try {
                // Try as LocalDate (e.g. 2023-12-31)
                val date = kotlinx.datetime.LocalDate.parse(this)
                val day = date.dayOfMonth.toString().padStart(2, '0')
                val month = date.monthNumber.toString().padStart(2, '0')
                "$day/$month/${date.year}"
            } catch (e3: Exception) {
                this // Fallback to raw string
            }
        }
    }
}

fun kotlinx.datetime.LocalDateTime.formatToFriendly(): String {
    val day = dayOfMonth.toString().padStart(2, '0')
    val month = monthNumber.toString().padStart(2, '0')
    val year = this.year
    val hour = this.hour.toString().padStart(2, '0')
    val minute = this.minute.toString().padStart(2, '0')

    return "$day/$month/$year - $hour:$minute"
}
