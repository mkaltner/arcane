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
        val local = ArcaneEnvironment(
            id = "local",
            name = "Local Docker",
            apiUrl = "unix:///var/run/docker.sock",
            status = "online",
            enabled = true,
            isEdge = false,
        )
        val state = environmentListContentState(
            environments = listOf(local),
            selectedEnvironmentId = "local",
        )

        assertEquals(
            EnvironmentListUiState.Content(
                environments = listOf(local),
                selectedEnvironmentId = "local",
            ),
            state,
        )
    }

    @Test
    fun `environment list defaults to first environment when none is selected`() {
        val first = ArcaneEnvironment(
            id = "first",
            name = "v0idLab",
            apiUrl = "http://192.168.1.5:3552",
            status = "online",
            enabled = true,
            isEdge = false,
        )
        val second = ArcaneEnvironment(
            id = "second",
            name = "cloudLab",
            apiUrl = "http://216.98.139.34:3553",
            status = "online",
            enabled = true,
            isEdge = false,
        )

        val state = environmentListContentState(
            environments = listOf(first, second),
            selectedEnvironmentId = null,
        )

        assertEquals(
            EnvironmentListUiState.Content(
                environments = listOf(first, second),
                selectedEnvironmentId = "first",
            ),
            state,
        )
    }
}
