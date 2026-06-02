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
import io.github.onlyashd.hukiawards.util.shapes
import io.github.onlyashd.hukiawards.util.typography

@Composable
fun LoginScreen(
    settings: Settings? = null,
    onLoginRequested: () -> Unit
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
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = settings?.eventName ?: "Huki Awards",
            fontWeight = FontWeight.Bold,
            style = typography().headlineLarge,
            textAlign = TextAlign.Center
        )

        val statusText = if (settings?.isVotingOpen == true) "Votação Aberta" else "Votação Fechada"
        val statusColor = if (settings?.isVotingOpen == true) colors().primary else colors().error

        Surface(
            color = statusColor.copy(alpha = 0.1f),
            shape = shapes().small,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = statusText,
                color = statusColor,
                style = typography().labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Text(
            text = "Fase atual: $phaseName",
            style = typography().titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = phaseDesc,
            style = typography().bodyMedium,
            textAlign = TextAlign.Center,
            color = colors().onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (settings?.showDatesToUsers == true) {
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Início", style = typography().labelMedium)
                    Text(
                        settings.votingStart?.formatToFriendlyDateTime() ?: "-",
                        style = typography().bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Término", style = typography().labelMedium)
                    Text(
                        settings.votingEnd?.formatToFriendlyDateTime() ?: "-",
                        style = typography().bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onLoginRequested,
            modifier = Modifier.height(56.dp).fillMaxWidth(0.6f),
            shape = shapes().medium
        ) {
            Text("Entrar com Discord", style = typography().titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Faça login para participar e deixar seu voto!",
            style = typography().labelSmall,
            color = colors().onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
