package io.github.onlyashd.hukiawards.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.onlyashd.hukiawards.client.ApiClient
import io.github.onlyashd.hukiawards.model.Routes
import io.github.onlyashd.hukiawards.model.Settings
import io.github.onlyashd.hukiawards.model.Strings
import io.github.onlyashd.hukiawards.util.AppLogger
import io.github.onlyashd.hukiawards.util.colors
import io.github.onlyashd.hukiawards.util.typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsManagementSubScreen(
    api: ApiClient,
    onShowSnackbar: (String) -> Unit
) {
    var settings by remember { mutableStateOf<Settings?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            settings = api.get(Routes.Settings)
        } catch (e: Exception) {
            AppLogger.e("Failed to fetch settings: ${e.message}")
        }
    }

    if (settings == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(Strings.GLOBAL_SETTINGS, style = typography().headlineMedium)

            val tooltipState = rememberTooltipState()
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text(Strings.GLOBAL_SETTINGS_HELP)
                    }
                },
                state = tooltipState
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = colors().onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = settings?.eventName ?: "",
                    onValueChange = { settings = settings?.copy(eventName = it) },
                    label = { Text(Strings.EVENT_NAME) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: Huki Awards 2026") }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Strings.VOTING_PERIOD_OPEN, modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings?.isVotingOpen ?: false,
                        onCheckedChange = { isOpen ->
                            settings = settings?.copy(isVotingOpen = isOpen)
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Strings.SHOW_DATES_TO_USERS, modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings?.showDatesToUsers ?: false,
                        onCheckedChange = { show ->
                            settings = settings?.copy(showDatesToUsers = show)
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(Strings.EVENT_PHASE)
                        Text(
                            text = if (settings?.phase == "NOMINATION") Strings.NOMINATION_PHASE_DESC else Strings.VOTING_PHASE_DESC,
                            style = typography().labelSmall,
                            color = colors().onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings?.phase == "VOTING",
                        onCheckedChange = { isVoting ->
                            settings =
                                settings?.copy(phase = if (isVoting) "VOTING" else "NOMINATION")
                        }
                    )
                }

                OutlinedTextField(
                    value = settings?.logoUrl ?: "",
                    onValueChange = { settings = settings?.copy(logoUrl = it) },
                    label = { Text(Strings.LOGO_URL) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: https://example.com/logo.png") }
                )

                OutlinedTextField(
                    value = settings?.faviconUrl ?: "",
                    onValueChange = { settings = settings?.copy(faviconUrl = it) },
                    label = { Text(Strings.FAVICON_URL) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: https://example.com/favicon.ico") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    var startDateInput by remember(settings?.votingStart) {
                        mutableStateOf(isoToDmY(settings?.votingStart ?: ""))
                    }
                    var endDateInput by remember(settings?.votingEnd) {
                        mutableStateOf(isoToDmY(settings?.votingEnd ?: ""))
                    }

                    OutlinedTextField(
                        value = startDateInput,
                        onValueChange = {
                            startDateInput = it
                            if (isValidDmY(it)) {
                                settings = settings?.copy(votingStart = dmyToIso(it, "00:00:00"))
                            }
                        },
                        label = { Text(Strings.START_DATE_LABEL) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ex: 01/12/2023") }
                    )

                    OutlinedTextField(
                        value = endDateInput,
                        onValueChange = {
                            endDateInput = it
                            if (isValidDmY(it)) {
                                settings = settings?.copy(votingEnd = dmyToIso(it, "23:59:59"))
                            }
                        },
                        label = { Text(Strings.END_DATE_LABEL) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ex: 31/12/2023") }
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                settings?.let {
                                    api.put<Unit>(Routes.Settings, "", it, isAdmin = true)
                                    onShowSnackbar(Strings.SETTINGS_SAVED_SUCCESS)
                                }
                            } catch (e: Exception) {
                                AppLogger.e("Failed to save settings: ${e.message}")
                                onShowSnackbar(Strings.ERROR_SAVE_SETTINGS)
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(Strings.SAVE_CHANGES)
                }
            }
        }
    }
}

private fun isoToDmY(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        val parts = iso.split("T")[0].split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
    } catch (e: Exception) {
        iso
    }
}

private fun dmyToIso(dmy: String, time: String): String {
    return try {
        val parts = dmy.split("/")
        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}T$time" else dmy
    } catch (e: Exception) {
        dmy
    }
}

private fun isValidDmY(dmy: String): Boolean {
    val regex = """^\d{2}/\d{2}/\d{4}$""".toRegex()
    return regex.matches(dmy)
}
