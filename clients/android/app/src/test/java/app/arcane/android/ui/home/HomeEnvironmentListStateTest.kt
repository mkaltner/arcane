package app.arcane.android.ui.home

import app.arcane.android.domain.model.ArcaneEnvironment
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeEnvironmentListStateTest {
    @Test
    fun `environment list content marks empty response`() {
        val state = environmentListContentState(
            environments = emptyList(),
            selectedEnvironmentId = null,
        )

        assertEquals(EnvironmentListUiState.Empty, state)
    }

    @Test
    fun `environment list content carries environments and selected id`() {
        val state = environmentListContentState(
            environments = listOf(
                ArcaneEnvironment(
                    id = "local",
                    name = "Local Docker",
                    apiUrl = "unix:///var/run/docker.sock",
                    status = "online",
                    enabled = true,
                    isEdge = false,
                ),
            ),
            selectedEnvironmentId = "local",
        )

        assertEquals(
            EnvironmentListUiState.Content(
                environments = listOf(
                    ArcaneEnvironment(
                        id = "local",
                        name = "Local Docker",
                        apiUrl = "unix:///var/run/docker.sock",
                        status = "online",
                        enabled = true,
                        isEdge = false,
                    ),
                ),
                selectedEnvironmentId = "local",
            ),
            state,
        )
    }
}
