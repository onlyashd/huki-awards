package io.github.onlyashd.hukiawards.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.onlyashd.hukiawards.client.ApiClient
import io.github.onlyashd.hukiawards.model.Routes
import io.github.onlyashd.hukiawards.util.AppLogger
import kotlinx.coroutines.launch

@Composable
fun AdminsManagementSubScreen(api: ApiClient) {
    var adminUsernames by remember { mutableStateOf(emptyList<String>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newAdminUsername by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val defaultAdmins = listOf("onlyashd", "hukizan", "sub0")

    val refreshAdmins = {
        coroutineScope.launch {
            try {
                adminUsernames = api.get(Routes.Admins)
            } catch (e: Exception) {
                AppLogger.e("Failed to fetch admins: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshAdmins()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Gerenciar Administradores", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Promover Usuário")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(adminUsernames) { username ->
                val isDefault = defaultAdmins.contains(username.lowercase())
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(username, style = MaterialTheme.typography.bodyLarge)
                            if (isDefault) {
                                Text(
                                    "Administrador Padrão",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (!isDefault) {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    try {
                                        api.delete<Unit>(Routes.Admins, username, isAdmin = true)
                                        refreshAdmins()
                                    } catch (e: Exception) {
                                        AppLogger.e("Failed to demote admin: ${e.message}")
                                    }
                                }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remover",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Promover Usuário") },
            text = {
                Column {
                    Text("Digite o username do Discord para promover a administrador:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newAdminUsername,
                        onValueChange = { newAdminUsername = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAdminUsername.isNotBlank()) {
                            coroutineScope.launch {
                                try {
                                    api.post<Unit>(
                                        Routes.Admins,
                                        "",
                                        id = newAdminUsername,
                                        isAdmin = true
                                    )
                                    showAddDialog = false
                                    newAdminUsername = ""
                                    refreshAdmins()
                                } catch (e: Exception) {
                                    AppLogger.e("Failed to promote user: ${e.message}")
                                }
                            }
                        }
                    }
                ) {
                    Text("Promover")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
