package app.arcane.android.ui.home

import app.arcane.android.domain.model.ArcaneContainer
import app.arcane.android.domain.model.ContainerStatusSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class ContainerListStateTest {
    @Test
    fun `containers screen shows web parity stats and running stopped rows`() {
        val state = containersScreenState(
            selectedEnvironmentName = "v0idLab",
            containersState = ContainerListUiState.Content(
                containers = listOf(
                    ArcaneContainer(
                        id = "abc123",
                        name = "arcane-web",
                        image = "ghcr.io/ofkm/arcane:latest",
                        state = "running",
                        status = "Up 3 hours",
                    ),
                    ArcaneContainer(
                        id = "def456",
                        name = "postgres",
                        image = "postgres:16",
                        state = "exited",
                        status = "Exited (0) 2 minutes ago",
                    ),
                ),
                counts = ContainerStatusSummary(runningContainers = 1, stoppedContainers = 1, totalContainers = 2),
            ),
        )

        assertEquals("Containers", state.title)
        assertEquals("v0idLab", state.selectedEnvironmentName)
        assertEquals(
            listOf(
                ResourceStatCard("Total", "2"),
                ResourceStatCard("Running", "1"),
                ResourceStatCard("Stopped", "1"),
            ),
            state.stats,
        )
        assertEquals(
            listOf(
                ContainerRowState("abc123", "arcane-web", "ghcr.io/ofkm/arcane:latest", "Up 3 hours", "Running"),
                ContainerRowState("def456", "postgres", "postgres:16", "Exited (0) 2 minutes ago", "Stopped"),
            ),
            state.rows,
        )
    }

    @Test
    fun `containers screen derives totals when backend counts are absent`() {
        val state = containersScreenState(
            selectedEnvironmentName = "v0idLab",
            containersState = ContainerListUiState.Content(
                containers = listOf(
                    ArcaneContainer(id = "1", name = "web", state = "running"),
                    ArcaneContainer(id = "2", name = "db", state = "created"),
                ),
                counts = null,
            ),
        )

        assertEquals(
            listOf(
                ResourceStatCard("Total", "2"),
                ResourceStatCard("Running", "1"),
                ResourceStatCard("Stopped", "1"),
            ),
            state.stats,
        )
    }

    @Test
    fun `navigation can select containers while preserving selected environment`() {
        val state = homeNavigationDrawerState(
            environments = emptyList(),
            selectedEnvironmentId = "0",
            selectedDestination = HomeDestination.Containers,
        )

        val resources = state.groups.single { it.title == "Resources" }.items
        assertEquals(true, resources.single { it.destination == HomeDestination.Containers }.selected)
        assertEquals(false, state.groups.first().items.single { it.destination == HomeDestination.Dashboard }.selected)
    }

    @Test
    fun `container rows keep id for opening detail screen`() {
        val state = containersScreenState(
            selectedEnvironmentName = "v0idLab",
            containersState = ContainerListUiState.Content(
                containers = listOf(
                    ArcaneContainer(
                        id = "8f6c5b4a3d2e",
                        name = "v0idlab-arcane-1",
                        image = "ghcr.io/getarcaneapp/arcane:latest",
                        state = "running",
                        status = "Up 2 hours (unhealthy)",
                    ),
                ),
                counts = null,
            ),
        )

        assertEquals("8f6c5b4a3d2e", state.rows.single().id)
    }

    @Test
    fun `container detail state uses selected row fields for next drill down`() {
        val detail = containerDetailScreenState(
            selectedEnvironmentName = "v0idLab",
            selectedContainer = ArcaneContainer(
                id = "8f6c5b4a3d2e",
                name = "v0idlab-arcane-1",
                image = "ghcr.io/getarcaneapp/arcane:latest",
                state = "running",
                status = "Up 2 hours (unhealthy)",
            ),
            detailState = ContainerDetailUiState.Loading,
        )

        assertEquals("v0idlab-arcane-1", detail.title)
        assertEquals("Environment: v0idLab", detail.subtitle)
        assertEquals("Running", detail.badge)
        assertEquals(
            listOf(
                ContainerFactRow("Container ID", "8f6c5b4a3d2e"),
                ContainerFactRow("Image", "ghcr.io/getarcaneapp/arcane:latest"),
                ContainerFactRow("Status", "Up 2 hours (unhealthy)"),
            ),
            detail.facts,
        )
    }

    @Test
    fun `back from containers returns dashboard instead of exiting app`() {
        assertEquals(HomeDestination.Dashboard, selectedDestinationAfterBack(HomeDestination.Containers))
    }

    @Test
    fun `back from container detail returns containers list`() {
        assertEquals(HomeDestination.Containers, selectedDestinationAfterBack(HomeDestination.ContainerDetail))
    }
}
