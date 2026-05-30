package io.github.onlyashd.hukiawards.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.onlyashd.hukiawards.client.ApiClient
import io.github.onlyashd.hukiawards.model.Routes
import io.github.onlyashd.hukiawards.model.Settings
import io.github.onlyashd.hukiawards.util.AppLogger
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsManagementSubScreen(api: ApiClient) {
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
            Text("Configurações Globais", style = MaterialTheme.typography.headlineMedium)

            val tooltipState = rememberTooltipState()
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text("Configurações que afetam todo o sistema, como o nome do evento e o status da votação.")
                    }
                },
                state = tooltipState
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
                    label = { Text("Nome do Evento") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: Huki Awards 2026") }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Período de Votação Aberto", modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings?.isVotingOpen ?: false,
                        onCheckedChange = { isOpen ->
                            settings = settings?.copy(isVotingOpen = isOpen)
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Mostrar datas para usuários", modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings?.showDatesToUsers ?: false,
                        onCheckedChange = { show ->
                            settings = settings?.copy(showDatesToUsers = show)
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fase do Evento")
                        Text(
                            text = if (settings?.phase == "NOMINATION") "Fase de Indicações (Usuários sugerem jogos)" else "Fase Final (Usuários votam em finalistas)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    value = settings?.votingStart ?: "",
                    onValueChange = { settings = settings?.copy(votingStart = it) },
                    label = { Text("Início da Votação (ISO-8601)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: 2023-12-01T00:00:00") }
                )

                OutlinedTextField(
                    value = settings?.votingEnd ?: "",
                    onValueChange = { settings = settings?.copy(votingEnd = it) },
                    label = { Text("Fim da Votação (ISO-8601)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: 2023-12-31T23:59:59") }
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                settings?.let {
                                    api.put<Unit>(Routes.Settings, "", it, isAdmin = true)
                                }
                            } catch (e: Exception) {
                                AppLogger.e("Failed to save settings: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Salvar Alterações")
                }
            }
        }
    }
}
