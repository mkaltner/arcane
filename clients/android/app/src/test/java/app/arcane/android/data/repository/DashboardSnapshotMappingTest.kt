package app.arcane.android.data.repository

import app.arcane.android.data.api.DashboardActionItem
import app.arcane.android.data.api.DashboardActionItems
import app.arcane.android.data.api.DashboardImageUsageCounts
import app.arcane.android.data.api.DashboardSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardSnapshotMappingTest {
    @Test
    fun `action items primary count matches web environment board item count`() {
        val snapshot = DashboardSnapshot(
            imageUsageCounts = DashboardImageUsageCounts(
                imagesInUse = 88,
                imagesUnused = 19,
                totalImages = 107,
            ),
            actionItems = DashboardActionItems(
                items = listOf(
                    DashboardActionItem(kind = "image_updates", count = 4),
                    DashboardActionItem(kind = "actionable_vulnerabilities", count = 2498, severity = "critical"),
                ),
            ),
        )

        val mapped = snapshot.toArcaneDashboardSnapshot()

        assertEquals(2, mapped.actionItems.count)
        assertEquals("4 Updates · 2498 Security", mapped.actionItems.summary)
        assertEquals(107, mapped.images.totalImages)
    }
}
