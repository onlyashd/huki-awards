package io.github.onlyashd.hukiawards.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.onlyashd.hukiawards.model.Category
import io.github.onlyashd.hukiawards.util.colors
import io.github.onlyashd.hukiawards.util.typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VotesManagementSubScreen(
    categories: List<Category>,
    onDeleteAll: () -> Unit,
    onExportCsv: () -> Unit,
    onDownloadShareImage: (Category) -> Unit,
    onDownloadWinnerImage: (Category) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Gerenciar votos", style = typography().headlineSmall)

                val tooltipState = rememberTooltipState()
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("Aqui você pode gerar as imagens de divulgação dos resultados e gerenciar a base de dados de votos.")
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

            Button(
                onClick = onExportCsv,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors().secondaryContainer,
                    contentColor = colors().onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar CSV")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Compartilhamento por categoria",
            style = typography().titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colors().surfaceVariant.copy(
                            alpha = 0.5f
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(category.name, style = typography().bodyLarge)
                            Text(
                                category.description ?: "",
                                style = typography().labelMedium,
                                color = colors().onSurfaceVariant
                            )
                        }
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = { PlainTooltip { Text("Baixar Top 10 (Ranking)") } },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = { onDownloadShareImage(category) }) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Gerar imagem Top 10"
                                )
                            }
                        }

                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = { PlainTooltip { Text("Baixar Card de Vencedor") } },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = { onDownloadWinnerImage(category) }) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Gerar imagem Vencedor",
                                    tint = colors().primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onDeleteAll,
            colors = ButtonDefaults.buttonColors(containerColor = colors().error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Limpar todos os votos")
        }
    }
}
