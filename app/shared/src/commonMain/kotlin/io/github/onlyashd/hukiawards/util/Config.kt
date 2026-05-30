package io.github.onlyashd.hukiawards.util

import io.github.onlyashd.hukiawards.shared.AppConfig

/**
 * Safely reads configuration properties baked directly into the WASM client binary
 */
object Config {
    fun getDiscordClientId(): String {
        return AppConfig.DISCORD_CLIENT_ID
    }

    fun getSentryDsn(): String {
        return AppConfig.SENTRY_DSN
    }
}
