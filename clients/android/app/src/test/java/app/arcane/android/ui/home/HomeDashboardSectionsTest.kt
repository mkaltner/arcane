package app.arcane.android.ui.home

import app.arcane.android.domain.model.ActionItemsSummary
import app.arcane.android.domain.model.ArcaneDashboardSnapshot
import app.arcane.android.domain.model.ArcaneEnvironment
import app.arcane.android.domain.model.ContainerStatusSummary
import app.arcane.android.domain.model.ImageUsageSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeDashboardSectionsTest {
    @Test
    fun `resource entries are derived from loaded dashboard data instead of queued placeholders`() {
        val dashboard = operationalDashboardState(
            environments = listOf(
                ArcaneEnvironment(
                    id = "cloud",
                    name = "cloudLab",
                    apiUrl = "http://216.98.139.34:3553",
                    status = "online",
                    enabled = true,
                    isEdge = true,
                ),
            ),
            selectedEnvironmentId = "cloud",
            snapshotState = DashboardSnapshotUiState.Content(
                ArcaneDashboardSnapshot(
                    containers = ContainerStatusSummary(runningContainers = 7, stoppedContainers = 2, totalContainers = 9),
                    images = ImageUsageSummary(imagesInUse = 8, imagesUnused = 0, totalImages = 8, totalImageSize = 512_000_000L),
                    actionItems = ActionItemsSummary(count = 0, summary = "All clear"),
                ),
            ),
        )

        assertEquals(
            listOf(
                DashboardResourceEntry("Containers", "9", "7 running · 2 stopped", "Review"),
                DashboardResourceEntry("Images", "8", "8 in use · 0 unused · 512MB", "Healthy"),
                DashboardResourceEntry("Action items", "0", "All clear", "Clear"),
            ),
            dashboard.resourceEntries,
        )
    }
}
