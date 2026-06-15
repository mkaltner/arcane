package app.arcane.android.data.repository

import app.arcane.android.data.api.ComposeInfo
import app.arcane.android.data.api.ContainerDetails
import app.arcane.android.data.api.ContainerMount
import org.junit.Assert.assertEquals
import org.junit.Test

class ContainerDetailMappingTest {
    @Test
    fun `container detail response maps compose and mount facts for detail screen`() {
        val detail = ContainerDetails(
            id = "8f6c5b4a3d2e",
            name = "/v0idlab-arcane-1",
            image = "ghcr.io/getarcaneapp/arcane:latest",
            imageId = "sha256:1234567890abcdef",
            created = "2026-06-15T09:00:00Z",
            mounts = listOf(
                ContainerMount(type = "volume", name = "arcane-data", destination = "/app/data", rw = true),
            ),
            composeInfo = ComposeInfo(projectName = "v0idlab", serviceName = "arcane"),
        ).toArcaneContainerDetail()

        assertEquals("8f6c5b4a3d2e", detail.id)
        assertEquals("v0idlab-arcane-1", detail.name)
        assertEquals("ghcr.io/getarcaneapp/arcane:latest", detail.image)
        assertEquals("sha256:1234567890abcdef", detail.imageId)
        assertEquals("2026-06-15T09:00:00Z", detail.created)
        assertEquals("v0idlab", detail.composeProject)
        assertEquals("arcane", detail.composeService)
        assertEquals(listOf("arcane-data → /app/data (rw)"), detail.mounts)
    }
}
