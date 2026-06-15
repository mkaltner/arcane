package app.arcane.android.ui.home

import app.arcane.android.domain.model.ActionItemsSummary
import app.arcane.android.domain.model.ArcaneDashboardSnapshot
import app.arcane.android.domain.model.ArcaneEnvironment
import app.arcane.android.domain.model.ContainerStatusSummary
import app.arcane.android.domain.model.ImageUsageSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeOperationalDashboardStateTest {
    @Test
    fun `dashboard overview sections summarize selected environment snapshot`() {
        val selected = ArcaneEnvironment(
            id = "env-1",
            name = "v0idLab",
            apiUrl = "http://192.168.1.5:3552",
            status = "online",
            enabled = true,
            isEdge = false,
        )
        val snapshot = ArcaneDashboardSnapshot(
            containers = ContainerStatusSummary(
                runningContainers = 5,
                stoppedContainers = 0,
                totalContainers = 5,
            ),
            images = ImageUsageSummary(
                imagesInUse = 4,
                imagesUnused = 6,
                totalImages = 10,
                totalImageSize = 1_800_000_000L,
            ),
            actionItems = ActionItemsSummary(count = 1, summary = "1 Security"),
        )

        val dashboard = operationalDashboardState(
            environments = listOf(selected),
            selectedEnvironmentId = "env-1",
            snapshotState = DashboardSnapshotUiState.Content(snapshot),
        )

        assertEquals("v0idLab", dashboard.selectedEnvironmentName)
        assertEquals(
            listOf(
                DashboardMetric("Containers", "5", "5 Running · 0 Stopped"),
                DashboardMetric("Images", "10", "4 In Use · 6 Unused"),
                DashboardMetric("Storage", "1.8GB", "6 unused images"),
                DashboardMetric("Action items", "1", "1 Security"),
            ),
            dashboard.metrics,
        )
    }

    @Test
    fun `dashboard falls back to selected id only when environment name is unavailable`() {
        val dashboard = operationalDashboardState(
            environments = emptyList(),
            selectedEnvironmentId = "env-1",
            snapshotState = DashboardSnapshotUiState.Loading,
        )

        assertEquals("env-1", dashboard.selectedEnvironmentName)
        assertEquals(DashboardSnapshotUiState.Loading, dashboard.snapshotState)
    }
}
