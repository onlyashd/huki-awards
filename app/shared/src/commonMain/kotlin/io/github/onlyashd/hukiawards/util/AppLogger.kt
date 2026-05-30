package io.github.onlyashd.hukiawards.util

import io.github.aakira.napier.Napier
import io.github.aakira.napier.DebugAntilog
import io.sentry.kotlin.multiplatform.Sentry

object AppLogger {
    private const val DEFAULT_TAG = "HUKI_AWARDS"

    /**
     * Call this exactly once during the application startup cycle (e.g., in your main WASM entry point)
     */
    fun init(isDebug: Boolean) {
        if (isDebug) {
            // Standard console logger layout for local development builds
            Napier.base(DebugAntilog())
        }
    }

    fun d(message: String, tag: String = DEFAULT_TAG) {
        Napier.d(message = message, tag = tag)
    }

    fun i(message: String, tag: String = DEFAULT_TAG) {
        Napier.i(message = message, tag = tag)
    }

    fun w(message: String, throwable: Throwable? = null, tag: String = DEFAULT_TAG) {
        Napier.w(message = message, throwable = throwable, tag = tag)
        if (throwable != null) {
            // Forward significant warning stacktraces to your Sentry Dashboard
            Sentry.captureException(throwable)
        }
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = DEFAULT_TAG) {
        Napier.e(message = message, throwable = throwable, tag = tag)
        if (throwable != null) {
            Sentry.captureException(throwable)
        } else {
            Sentry.captureMessage(message)
        }
    }
}