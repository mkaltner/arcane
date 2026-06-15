package app.arcane.android.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import app.arcane.android.ui.theme.ArcaneColors
import app.arcane.android.ui.theme.ArcaneTheme

data class HomeDashboardSection(
    val name: String,
    val description: String,
    val status: String,
)

fun authenticatedDashboardSections(): List<HomeDashboardSection> = listOf(
    HomeDashboardSection("Environments", "Select the Arcane environment to inspect", "Next"),
    HomeDashboardSection("Containers", "Browse containers after an environment is selected", "Queued"),
    HomeDashboardSection("Images", "Review images and related metadata", "Queued"),
    HomeDashboardSection("Actions", "Start, stop, and restart resources with confirmation", "Planned"),
)

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(uiState = uiState)
}

@Composable
fun HomeScreen(uiState: HomeUiState) {
    Scaffold(containerColor = ArcaneColors.Background) { innerPadding ->
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
        modifier = modifier
            .fillMaxSize()
            .background(ArcaneColors.Background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = ArcaneColors.PrimaryPurple)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Preparing Arcane…", color = ArcaneColors.TextSecondary)
    }
}

@Composable
private fun ReadyContent(status: ArcaneStatus, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ArcaneColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeaderCard(status)
        }
        item {
            NextStepCard()
        }
        items(authenticatedDashboardSections()) { section -> SectionCard(section) }
    }
}

@Composable
private fun HeaderCard(status: ArcaneStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneColors.Surface),
        border = BorderStroke(1.dp, ArcaneColors.Border),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "DASHBOARD",
                style = MaterialTheme.typography.labelMedium,
                color = ArcaneColors.TextSecondary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = status.title,
                style = MaterialTheme.typography.headlineMedium,
                color = ArcaneColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = status.message, style = MaterialTheme.typography.bodyLarge, color = ArcaneColors.TextSecondary)
        }
    }
}

@Composable
private fun NextStepCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneColors.PrimaryPurpleContainer),
        border = BorderStroke(1.dp, ArcaneColors.PrimaryPurple.copy(alpha = 0.45f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Text(
                text = "NEXT STEP",
                style = MaterialTheme.typography.labelMedium,
                color = ArcaneColors.PrimaryPurple,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pick an environment, then we’ll open the resource dashboard.",
                style = MaterialTheme.typography.titleMedium,
                color = ArcaneColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SectionCard(section: HomeDashboardSection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneColors.SurfaceElevated),
        border = BorderStroke(1.dp, ArcaneColors.Border),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = section.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ArcaneColors.TextPrimary,
                )
                StatusPill(text = section.status)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = section.description, style = MaterialTheme.typography.bodyMedium, color = ArcaneColors.TextSecondary)
            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { sectionProgress(section.status) },
                modifier = Modifier.fillMaxWidth(),
                color = sectionColor(section.status),
                trackColor = ArcaneColors.Border,
            )
        }
    }
}

private fun sectionProgress(status: String): Float = when (status) {
    "Next" -> 0.86f
    "Queued" -> 0.42f
    else -> 0.18f
}

@Composable
private fun sectionColor(status: String) = when (status) {
    "Next" -> ArcaneColors.SuccessGreen
    "Queued" -> ArcaneColors.PortBlue
    else -> ArcaneColors.PrimaryPurple
}

@Composable
private fun StatusPill(text: String) {
    val (container, content) = when (text) {
        "Next" -> ArcaneColors.SuccessGreenContainer to ArcaneColors.SuccessGreen
        "Queued" -> ArcaneColors.SurfaceMuted to ArcaneColors.PortBlue
        else -> ArcaneColors.PrimaryPurpleContainer to ArcaneColors.PrimaryPurple
    }
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, content.copy(alpha = 0.35f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = content,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ArcaneTheme(darkTheme = true) {
        HomeScreen(
            uiState = HomeUiState.Ready(
                ArcaneStatus(
                    title = "Arcane Manager ready",
                    message = "Server: https://arcane.example.com. Signed in as demo. Choose an environment to continue.",
                ),
            ),
        )
    }
}
