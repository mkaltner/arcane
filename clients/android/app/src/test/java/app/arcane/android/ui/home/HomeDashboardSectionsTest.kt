package app.arcane.android.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeDashboardSectionsTest {
    @Test
    fun `authenticated dashboard starts with environment and resource sections`() {
        val sections = authenticatedDashboardSections()

        assertEquals(
            listOf(
                HomeDashboardSection("Environments", "Select the Arcane environment to inspect", "Next"),
                HomeDashboardSection("Containers", "Browse containers after an environment is selected", "Queued"),
                HomeDashboardSection("Images", "Review images and related metadata", "Queued"),
                HomeDashboardSection("Actions", "Start, stop, and restart resources with confirmation", "Planned"),
            ),
            sections,
        )
    }
}
