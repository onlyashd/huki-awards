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

    // 1. Try to get token from URL (first priority)
    var tokenFromUrl = when {
        currentUrlString.contains("token=") -> {
            currentUrlString.substringAfter("token=").substringBefore("&")
        }
        else -> null
    }

    // 2. If no token in URL, try to get from localStorage (persistence)
    if (tokenFromUrl.isNullOrBlank()) {
        tokenFromUrl = window.localStorage.getItem("session_token")
    } else {
        // If we found a new token in the URL, save it to localStorage
        window.localStorage.setItem("session_token", tokenFromUrl)

        // Strip out the token from the browser bar history cleanly
        val cleanUrl = currentUrlString.substringBefore("?")
        window.history.replaceState(null, "", cleanUrl)
        AppLogger.i("Session token successfully intercepted and persisted.")
    }

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App(
            initialToken = tokenFromUrl,
            onTokenChanged = { newToken ->
                if (newToken == null) {
                    window.localStorage.removeItem("session_token")
                } else {
                    window.localStorage.setItem("session_token", newToken)
                }
            },
            onNavigate = { targetUrl ->
                window.location.href = targetUrl
            }
        )
    }
}
