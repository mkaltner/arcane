package app.arcane.android.ui.home

import app.arcane.android.domain.model.ArcaneEnvironment
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeNavigationDrawerStateTest {
    @Test
    fun `drawer mirrors Arcane sidebar grouping and selected environment`() {
        val drawer = homeNavigationDrawerState(
            environments = listOf(
                ArcaneEnvironment(
                    id = "0",
                    name = "v0idLab",
                    apiUrl = "unix:///var/run/docker.sock",
                    status = "online",
                    enabled = true,
                    isEdge = false,
                ),
            ),
            selectedEnvironmentId = "0",
            activityCount = 8,
        )

        assertEquals("0", drawer.selectedEnvironmentId)
        assertEquals("v0idLab", drawer.environmentName)
        assertEquals("unix:///var/run/docker.sock", drawer.environmentSubtitle)
        assertEquals(
            listOf(
                EnvironmentSelectorOption(
                    id = "0",
                    name = "v0idLab",
                    subtitle = "unix:///var/run/docker.sock",
                    selected = true,
                ),
            ),
            drawer.environmentOptions,
        )
        assertEquals(8, drawer.activityCount)
        assertEquals(
            listOf(
                NavigationGroup(
                    title = "Management",
                    items = listOf(
                        NavigationItem("Dashboard", destination = HomeDestination.Dashboard, selected = true),
                        NavigationItem("Projects"),
                        NavigationItem("Environments", expandable = true),
                        NavigationItem("Customization", expandable = true),
                    ),
                ),
                NavigationGroup(
                    title = "Resources",
                    items = listOf(
                        NavigationItem("Containers", destination = HomeDestination.Containers),
                        NavigationItem("Images", expandable = true),
                        NavigationItem("Updates"),
                        NavigationItem("Networks", expandable = true),
                        NavigationItem("Volumes"),
                    ),
                ),
                NavigationGroup(
                    title = "Swarm",
                    items = listOf(NavigationItem("Cluster")),
                ),
                NavigationGroup(
                    title = "Administration",
                    items = listOf(
                        NavigationItem("Event Log"),
                        NavigationItem("Settings", expandable = true),
                    ),
                ),
            ),
            drawer.groups,
        )
    }
}
