package app.arcane.android.data.repository

import app.arcane.android.data.api.EnvironmentSummary
import app.arcane.android.domain.model.ArcaneEnvironment
import org.junit.Assert.assertEquals
import org.junit.Test

class EnvironmentMappingTest {
    @Test
    fun `api environment summary maps to domain environment`() {
        val summary = EnvironmentSummary(
            id = "edge-1",
            name = "Production Edge",
            apiUrl = "https://edge.example/api",
            status = "online",
            enabled = true,
            isEdge = true,
            lastSeen = "2026-06-15T20:30:00Z",
        )

        assertEquals(
            ArcaneEnvironment(
                id = "edge-1",
                name = "Production Edge",
                apiUrl = "https://edge.example/api",
                status = "online",
                enabled = true,
                isEdge = true,
                lastSeen = "2026-06-15T20:30:00Z",
            ),
            summary.toArcaneEnvironment(),
        )
    }
}
