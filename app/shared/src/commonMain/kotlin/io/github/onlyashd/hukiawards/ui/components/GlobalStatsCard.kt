package io.github.onlyashd.hukiawards.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.onlyashd.hukiawards.model.GlobalStats
import io.github.onlyashd.hukiawards.model.Strings
import io.github.onlyashd.hukiawards.util.colors
import io.github.onlyashd.hukiawards.util.typography

@Composable
fun GlobalStatsCard(stats: GlobalStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors().surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                Strings.GENERAL_STATS,
                style = typography().titleLarge,
                color = colors().primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItem(
                    icon = Icons.Default.ThumbUp,
                    label = Strings.TOTAL_VOTES,
                    value = stats.totalVotes.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    icon = Icons.Default.Person,
                    label = Strings.UNIQUE_ELECTORS,
                    value = stats.uniqueVoters.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            if (stats.categoryStats.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    Strings.CATEGORY_PARTICIPATION,
                    style = typography().titleSmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                stats.categoryStats.forEach { cat ->
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(cat.categoryName, style = typography().bodyMedium)
                            Text(
                                Strings.VOTE_COUNT.replace("%d", cat.voteCount.toString()),
                                style = typography().labelLarge
                            )
                        }
                        LinearProgressIndicator(
                            progress = { if (stats.totalVotes > 0) cat.voteCount.toFloat() / stats.totalVotes.toFloat() else 0f },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            color = colors().primary,
                            trackColor = colors().surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = colors().surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colors().primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, style = typography().labelMedium, color = colors().onSurfaceVariant)
                Text(value, style = typography().headlineSmall, color = colors().onSurface)
            }
        }
    }
}
