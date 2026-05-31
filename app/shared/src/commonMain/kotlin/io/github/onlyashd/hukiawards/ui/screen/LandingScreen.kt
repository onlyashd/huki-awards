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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.onlyashd.hukiawards.model.Settings
import io.github.onlyashd.hukiawards.model.Strings
import io.github.onlyashd.hukiawards.util.colors
import io.github.onlyashd.hukiawards.util.formatToFriendlyDateTime

@Composable
fun LandingScreen(
    settings: Settings? = null,
    onGoToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = settings?.logoUrl
                ?: "https://static-cdn.jtvnw.net/jtv_user_pictures/7225fcae-f28e-4fa9-a754-5cc2db25c83c-profile_image-70x70.png",
            contentDescription = null,
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = settings?.eventName ?: "Huki Awards",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        val statusText =
            if (settings?.isVotingOpen == true) "Votação Aberta" else "Votação Encerrada"
        val statusColor = if (settings?.isVotingOpen == true) colors().primary else colors().error

        Surface(
            color = statusColor.copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = statusText,
                color = statusColor,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Phase info
        val phaseName = when (settings?.phase) {
            "NOMINATION" -> "Indicações"
            "VOTING" -> "Votação Final"
            else -> "Aguardando"
        }
        val phaseDesc = when (settings?.phase) {
            "NOMINATION" -> Strings.NOMINATION_PHASE_DESC
            "VOTING" -> Strings.VOTING_PHASE_DESC
            else -> "O evento começará em breve!"
        }

        Card(
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = CardDefaults.cardColors(containerColor = colors().surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Fase: $phaseName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = phaseDesc,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = colors().onSurfaceVariant
                )
            }
        }

        if (settings?.showDatesToUsers == true) {
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DateInfoColumn("Início", settings.votingStart?.formatToFriendlyDateTime() ?: "-")
                DateInfoColumn("Término", settings.votingEnd?.formatToFriendlyDateTime() ?: "-")
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = onGoToLogin,
            modifier = Modifier.height(64.dp).fillMaxWidth(0.5f),
            shape = MaterialTheme.shapes.large,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Participar Agora", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Faça login com sua conta do Discord",
            style = MaterialTheme.typography.labelMedium,
            color = colors().onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun DateInfoColumn(label: String, date: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = colors().onSurfaceVariant
        )
        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
