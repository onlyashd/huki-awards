package io.github.onlyashd.hukiawards.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import huki_awards.app.shared.generated.resources.Res
import io.github.onlyashd.hukiawards.model.Category
import io.github.onlyashd.hukiawards.model.UserProfile
import io.github.onlyashd.hukiawards.model.VoteRequest
import io.github.onlyashd.hukiawards.util.colors
import io.github.onlyashd.hukiawards.util.formatToFriendly
import io.github.onlyashd.hukiawards.util.typography
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun VotingOverviewSubScreen(
    profile: UserProfile?,
    categories: List<Category>,
    userVotes: List<VoteRequest>,
    isFinalPhase: Boolean = false,
    onEditRequested: () -> Unit,
    onDownloadRequested: () -> Unit,
    onShareRequested: () -> Unit
) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val dateStr = now.formatToFriendly()

    val phaseLabel = if (isFinalPhase) "Seus votos" else "Suas indicações"
    val emptyStateLabel = if (isFinalPhase) "Não indicado" else "Não indicada"
    val generatedLabel = if (isFinalPhase) "Gerado" else "Gerada"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // User Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = profile?.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = profile?.name ?: "Usuário",
                    style = typography().headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$phaseLabel para o Huki Awards 2026",
                    style = typography().bodyMedium,
                    color = colors().onSurfaceVariant
                )
                Text(
                    text = "$generatedLabel em: $dateStr",
                    style = typography().labelSmall,
                    color = colors().onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Text(
            text = "Resumo",
            style = typography().titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                val vote = userVotes.find { it.categoryId == category.id }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colors().surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (vote?.gameCoverUrl != null) {
                            AsyncImage(
                                model = vote.gameCoverUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(45.dp, 60.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column {
                            Text(
                                text = category.name,
                                style = typography().labelMedium,
                                color = colors().primary
                            )
                            Text(
                                text = vote?.gameName ?: emptyStateLabel,
                                style = typography().bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onEditRequested,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Alterar", maxLines = 1)
            }
            Button(
                onClick = onDownloadRequested,
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = Res.getUri("drawable/download.png"),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(LocalContentColor.current)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Baixar", maxLines = 1)
            }
            Button(
                onClick = onShareRequested,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Enviar para o Discord", maxLines = 1)
            }
        }
    }
}
