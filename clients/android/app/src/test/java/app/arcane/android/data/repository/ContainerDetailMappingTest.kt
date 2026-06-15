package app.arcane.android.data.repository

import app.arcane.android.data.api.ComposeInfo
import app.arcane.android.data.api.ContainerDetails
import app.arcane.android.data.api.ContainerHostConfig
import app.arcane.android.data.api.ContainerMount
import app.arcane.android.data.api.ContainerNetwork
import app.arcane.android.data.api.ContainerNetworkSettings
import app.arcane.android.data.api.ContainerPort
import app.arcane.android.data.api.ContainerConfig
import app.arcane.android.data.api.ContainerState
import org.junit.Assert.assertEquals
import org.junit.Test

class ContainerDetailMappingTest {
    @Test
    fun `container details mapping preserves overview parity fields`() {
        val detail = ContainerDetails(
            id = "8f6c5b4a3d2e9f00112233445566778899",
            name = "/utilities-webdav-1",
            image = "bytemark/webdav:latest",
            imageId = "sha256:imageabc",
            created = "2026-06-15T10:15:30Z",
            state = ContainerState(
                status = "running",
                running = true,
                startedAt = "2026-06-15T10:16:01Z",
                finishedAt = "0001-01-01T00:00:00Z",
            ),
            config = ContainerConfig(
                cmd = listOf("nginx", "-g", "daemon off;"),
                entrypoint = listOf("docker-entrypoint.sh"),
                workingDir = "/var/www",
            ),
            hostConfig = ContainerHostConfig(restartPolicy = "unless-stopped"),
            networkSettings = ContainerNetworkSettings(
                networks = mapOf(
                    "arcane" to ContainerNetwork(ipAddress = "172.19.0.4"),
                ),
            ),
            ports = listOf(
                ContainerPort(privatePort = 80, publicPort = 8080, type = "tcp"),
                ContainerPort(privatePort = 443, type = "tcp"),
            ),
            mounts = listOf(
                ContainerMount(name = "webdav-data", destination = "/data", rw = true),
                ContainerMount(source = "/srv/webdav", destination = "/srv", rw = false),
            ),
            labels = mapOf("com.getarcaneapp.arcane.updater" to "false"),
            composeInfo = ComposeInfo(projectName = "utilities", serviceName = "webdav", workingDir = "/srv/compose"),
        ).toArcaneContainerDetail()

        assertEquals("utilities-webdav-1", detail.name)
        assertEquals("running", detail.status)
        assertEquals(true, detail.running)
        assertEquals("2026-06-15T10:16:01Z", detail.startedAt)
        assertEquals("172.19.0.4", detail.primaryIpAddress)
        assertEquals("unless-stopped", detail.restartPolicy)
        assertEquals(false, detail.autoUpdateEnabled)
        assertEquals("1 published · 1 exposed", detail.portsSummary)
        assertEquals("2 mounts", detail.volumesSummary)
        assertEquals("1 network", detail.networksSummary)
        assertEquals("/var/www", detail.workingDirectory)
        assertEquals("docker-entrypoint.sh", detail.entrypoint)
        assertEquals("nginx -g daemon off;", detail.command)
        assertEquals("utilities", detail.composeProject)
        assertEquals("webdav", detail.composeService)
    }
}
