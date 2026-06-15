package app.arcane.android.data.repository

import app.arcane.android.data.api.ContainerListResponse
import app.arcane.android.data.api.ContainerStatusCounts
import app.arcane.android.data.api.ContainerSummary
import app.arcane.android.data.api.PaginationResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class ContainerMappingTest {
    @Test
    fun `container list response maps slash names and counts to domain models`() {
        val response = ContainerListResponse(
            success = true,
            data = listOf(
                ContainerSummary(
                    id = "abc123",
                    names = listOf("/arcane-web"),
                    image = "ghcr.io/ofkm/arcane:latest",
                    state = "running",
                    status = "Up 3 hours",
                ),
            ),
            counts = ContainerStatusCounts(runningContainers = 1, stoppedContainers = 0, totalContainers = 1),
            pagination = PaginationResponse(totalPages = 1, totalItems = 1, currentPage = 1, itemsPerPage = 20),
        )

        val mapped = response.toArcaneContainerList()

        assertEquals("arcane-web", mapped.containers.single().name)
        assertEquals("ghcr.io/ofkm/arcane:latest", mapped.containers.single().image)
        assertEquals(1, mapped.counts?.runningContainers)
    }

    @Test
    fun `container summary falls back to short id when name is missing`() {
        val mapped = ContainerSummary(id = "1234567890abcdef", names = emptyList()).toArcaneContainer()

        assertEquals("1234567890ab", mapped.name)
    }
}
