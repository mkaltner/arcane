package app.arcane.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.arcane.android.domain.model.ArcaneStatus
import app.arcane.android.ui.theme.ArcaneTheme

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(uiState = uiState)
}

@Composable
fun HomeScreen(uiState: HomeUiState) {
    Scaffold { innerPadding ->
        when (uiState) {
            HomeUiState.Loading -> LoadingContent(modifier = Modifier.padding(innerPadding))
            is HomeUiState.Ready -> ReadyContent(
                status = uiState.status,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Preparing Arcane…")
    }
}

@Composable
private fun ReadyContent(status: ArcaneStatus, modifier: Modifier = Modifier) {
    val layers = listOf(
        "UI" to "Jetpack Compose + Material 3 home shell",
        "API/Data" to "Ktor client configured with kotlinx serialization",
        "Domain" to "Repository contract and serializable models",
        "Settings" to "DataStore preferences placeholder",
    )
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(text = status.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = status.message, style = MaterialTheme.typography.bodyLarge)
        }
        items(layers) { (name, description) -> LayerCard(name, description) }
    }
}

@Composable
private fun LayerCard(name: String, description: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(0.35f),
            )
            Text(text = description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.65f))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ArcaneTheme {
        HomeScreen(
            uiState = HomeUiState.Ready(
                ArcaneStatus(
                    title = "Arcane Android client scaffold ready",
                    message = "Placeholder preview for the home screen.",
                ),
            ),
        )
    }
}
