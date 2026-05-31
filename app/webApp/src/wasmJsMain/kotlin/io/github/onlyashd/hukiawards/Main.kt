package io.github.onlyashd.hukiawards

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import io.github.onlyashd.hukiawards.ui.App
import io.github.onlyashd.hukiawards.util.AppLogger
import io.github.onlyashd.hukiawards.util.Config
import io.sentry.kotlin.multiplatform.Sentry
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    println("App is starting...")
    Sentry.init { options ->
        options.dsn = Config.getSentryDsn()
    }

    AppLogger.init(isDebug = true)

    val currentUrlString = window.location.href

    // Parse out the token regardless of whether it's placed before or after the '#' hash symbol
    val tokenFromUrl = when {
        currentUrlString.contains("token=") -> {
            currentUrlString.substringAfter("token=").substringBefore("&")
        }

        else -> null
    }

    if (!tokenFromUrl.isNullOrBlank()) {
        // Strip out the token from the browser bar history cleanly so it doesn't linger visible
        val cleanUrl = currentUrlString.substringBefore("?")
        window.history.replaceState(null, "", cleanUrl)
        AppLogger.i("Session token successfully intercepted from navigation context.")
    }

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App(
            initialToken = tokenFromUrl,
            onNavigate = { targetUrl ->
                window.location.href = targetUrl
            }
        )
    }
}
