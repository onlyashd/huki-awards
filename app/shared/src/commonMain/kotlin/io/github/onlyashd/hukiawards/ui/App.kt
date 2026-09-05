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
import io.github.onlyashd.hukiawards.ui.screen.LandingScreen
import io.github.onlyashd.hukiawards.ui.screen.LoginScreen
import io.github.onlyashd.hukiawards.ui.screen.RoleErrorScreen
import io.github.onlyashd.hukiawards.ui.screen.UserDashboard
import io.github.onlyashd.hukiawards.ui.theme.AppTheme
import io.github.onlyashd.hukiawards.util.AppLogger
import io.github.onlyashd.hukiawards.util.getRoleFromToken
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponseContainer
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.header.AcceptEncoding
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.json.Json


@Composable
fun App(
    initialToken: String?,
    onTokenChanged: (String?) -> Unit = {},
    onNavigate: (String) -> Unit
) {
    var currentRoute by remember { mutableStateOf("/") }
    var sessionToken by remember { mutableStateOf(initialToken) }

    LaunchedEffect(sessionToken) {
        onTokenChanged(sessionToken)
    }

    var isAdminPreviewMode by remember { mutableStateOf(false) }

    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var settings by remember { mutableStateOf<io.github.onlyashd.hukiawards.model.Settings?>(null) }

    val httpClient = remember {
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
                header(HttpHeaders.AcceptEncoding, AcceptEncoding.Identity)
            }

            install(ResponseObserver) {
                onResponse { response ->
                    println("Response status: ${response.status}")
                }
            }

            val bypassBrotliPlugin = createClientPlugin("BypassBrotliPlugin") {
                client.responsePipeline.intercept(HttpResponsePipeline.Receive) { (typeInfo, responseBody) ->
                    if (responseBody is ByteReadChannel) {
                        proceedWith(HttpResponseContainer(typeInfo, responseBody))
                    } else {
                        proceedWith(subject)
                    }
                }
            }
            install(bypassBrotliPlugin)
        }
    }

    val apiClient = remember(sessionToken) {
        ApiClient(httpClient, sessionToken)
    }

    // Automatically navigate to dashboard if token is present on startup
    LaunchedEffect(Unit) {
        if (!initialToken.isNullOrBlank()) {
            currentRoute = "/dashboard"
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
        try {
            settings = apiClient.get(Routes.Settings)
        } catch (e: Exception) {
            AppLogger.e("Failed to fetch settings", e)
        }

        if (!sessionToken.isNullOrBlank()) {
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
            val isLogged = !sessionToken.isNullOrBlank()
            val userRole = user.second

            when (currentRoute) {
                "/" -> {
                    LandingScreen(
                        settings = settings,
                        onGoToLogin = {
                            currentRoute = if (isLogged) {
                                "/dashboard"
                            } else {
                                "/login"
                            }
                        }
                    )
                }

                "/login" -> {
                    LoginScreen(
                        settings = settings,
                        onLoginRequested = {
                            onNavigate(Server.subPath(LoginDiscord))
                        }
                    )
                }

                "/dashboard" -> {
                    when {
                        !isLogged -> {
                            currentRoute = "/login"
                        }

                        userRole == Roles.ADMIN && !isAdminPreviewMode -> {
                            AdminDashboard(
                                api = apiClient,
                                profile = userProfile,
                                onLogoutRequested = {
                                    sessionToken = null
                                    currentRoute = "/"
                                },
                                onToggleUserView = { isAdminPreviewMode = true }
                            )
                        }

                        userRole == Roles.USER || (userRole == Roles.ADMIN && isAdminPreviewMode) -> {
                            UserDashboard(
                                api = apiClient,
                                profile = userProfile,
                                isAdminPreview = userRole == Roles.ADMIN,
                                onLogoutRequested = {
                                    if (userRole == Roles.ADMIN && isAdminPreviewMode) {
                                        isAdminPreviewMode = false
                                    } else {
                                        sessionToken = null
                                        currentRoute = "/"
                                    }
                                }
                            )
                        }

                        userRole == Roles.UNKNOWN -> {
                            RoleErrorScreen(
                                onLogoutRequested = {
                                    sessionToken = null
                                    currentRoute = "/"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
