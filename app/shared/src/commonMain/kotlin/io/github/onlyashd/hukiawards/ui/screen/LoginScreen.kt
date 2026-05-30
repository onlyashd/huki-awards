package io.github.onlyashd.hukiawards.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun LoginScreen(onLoginRequested: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = "https://static-cdn.jtvnw.net/jtv_user_pictures/7225fcae-f28e-4fa9-a754-5cc2db25c83c-profile_image-70x70.png",
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Huki Awards",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "2026",
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = onLoginRequested) {
            Text("Entrar com Discord")
        }
    }
}
