package io.github.onlyashd.hukiawards.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.onlyashd.hukiawards.client.ApiClient
import io.github.onlyashd.hukiawards.model.Roles
import io.github.onlyashd.hukiawards.model.Routes
import io.github.onlyashd.hukiawards.model.Routes.LoginDiscord
import io.github.onlyashd.hukiawards.model.Routes.Server
import io.github.onlyashd.hukiawards.model.UserProfile
import io.github.onlyashd.hukiawards.ui.screen.AdminDashboard
import io.github.onlyashd.hukiawards.ui.screen.LoginScreen
import io.github.onlyashd.hukiawards.ui.screen.RoleErrorScreen
import io.github.onlyashd.hukiawards.ui.screen.UserDashboard
import io.github.onlyashd.hukiawards.ui.theme.AppTheme
import io.github.onlyashd.hukiawards.util.AppLogger
import io.github.onlyashd.hukiawards.util.getRoleFromToken
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


@Composable
fun App(
    initialToken: String?,
    onNavigate: (String) -> Unit
) {
    var sessionToken by remember { mutableStateOf(initialToken) }
    var isAdminPreviewMode by remember { mutableStateOf(false) }

    var userProfile by remember { mutableStateOf<UserProfile?>(null) }

    val apiClient = remember(sessionToken) {
        sessionToken?.let {
            ApiClient(
                HttpClient {
                    install(ContentNegotiation) {
                        json(Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            encodeDefaults = true
                        })
                    }
                    install(DefaultRequest) {
                        header(HttpHeaders.Accept, ContentType.Application.Json)
                        header(HttpHeaders.ContentType, ContentType.Application.Json)
                    }
                },
                it
            )
        }
    }

    val user by remember(sessionToken) {
        derivedStateOf {
            if (sessionToken.isNullOrBlank()) {
                Pair(null, Roles.UNKNOWN)
            } else {
                try {
                    val raw = getRoleFromToken(sessionToken!!)
                    AppLogger.i("Raw role string extracted from token: '${raw.second}'")

                    // Strip any stray characters (like parenthetical tuples) just in case
                    when (raw.second.trim().uppercase().replace(Regex("[()\\s]"), "")) {
                        "ADMIN" -> Pair(raw.first, Roles.ADMIN)
                        "USER" -> Pair(raw.first, Roles.USER)
                        else -> {
                            AppLogger.w("Unrecognized role found in token payload: '${raw.second}'")
                            Pair(raw.first, Roles.UNKNOWN)
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.e("Failed to decode user role claim from JWT string", e)
                    Pair(null, Roles.UNKNOWN)
                }
            }
        }
    }

    LaunchedEffect(sessionToken, user) {
        if (!sessionToken.isNullOrBlank() && apiClient != null) {
            try {
                userProfile = apiClient.get(Routes.Profile, user.first)
                AppLogger.i("Successfully loaded user profile context for: ${userProfile?.username}")
            } catch (e: Exception) {
                AppLogger.e("Failed to fetch profile info from endpoint", e)
                userProfile = null
            }
        } else {
            userProfile = null
        }
    }

    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                sessionToken.isNullOrBlank() -> {
                    LoginScreen(onLoginRequested = {
                        onNavigate(Server.subPath(LoginDiscord))
                    })
                }

                user.second == Roles.ADMIN && !isAdminPreviewMode -> {
                    AdminDashboard(
                        api = apiClient!!,
                        profile = userProfile,
                        onLogoutRequested = {
                            sessionToken = null
                            onNavigate("/")
                        },
                        onToggleUserView = { isAdminPreviewMode = true }
                    )
                }

                user.second == Roles.USER || (user.second == Roles.ADMIN && isAdminPreviewMode) -> {
                    UserDashboard(
                        api = apiClient!!,
                        profile = userProfile,
                        isAdminPreview = user.second == Roles.ADMIN,
                        onLogoutRequested = {
                            if (user.second == Roles.ADMIN) {
                                isAdminPreviewMode = false
                            } else {
                                sessionToken = null
                                onNavigate("/")
                            }
                        }
                    )
                }

                user.second == Roles.UNKNOWN -> {
                    RoleErrorScreen(
                        onLogoutRequested = {
                            sessionToken = null
                            onNavigate("/")
                        }
                    )
                }
            }
        }
    }
}
