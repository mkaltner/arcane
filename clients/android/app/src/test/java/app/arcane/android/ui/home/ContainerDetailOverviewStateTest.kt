package app.arcane.android.ui.home

import app.arcane.android.domain.model.ArcaneContainer
import app.arcane.android.domain.model.ArcaneContainerDetail
import org.junit.Assert.assertEquals
import org.junit.Test

class ContainerDetailOverviewStateTest {
    @Test
    fun `container detail overview builds mobile native sections from web parity fields`() {
        val state = containerDetailScreenState(
            selectedEnvironmentName = "v0idLab",
            selectedContainer = ArcaneContainer(
                id = "8f6c5b4a3d2e",
                name = "utilities-webdav-1",
                image = "bytemark/webdav:latest",
                state = "running",
                status = "Up 2 hours (healthy)",
            ),
            detailState = ContainerDetailUiState.Content(
                ArcaneContainerDetail(
                    id = "8f6c5b4a3d2e9f00112233445566778899",
                    name = "utilities-webdav-1",
                    image = "bytemark/webdav:latest",
                    imageId = "sha256:imageabc",
                    created = "2026-06-15T10:15:30Z",
                    status = "running",
                    running = true,
                    startedAt = "2026-06-15T10:16:01Z",
                    primaryIpAddress = "172.19.0.4",
                    restartPolicy = "unless-stopped",
                    autoUpdateEnabled = false,
                    portsSummary = "1 published · 1 exposed",
                    volumesSummary = "2 mounts",
                    networksSummary = "1 network",
                    workingDirectory = "/var/www",
                    entrypoint = "docker-entrypoint.sh",
                    command = "nginx -g daemon off;",
                    composeProject = "utilities",
                    composeService = "webdav",
                ),
            ),
        )

        assertEquals("utilities-webdav-1", state.title)
        assertEquals("v0idLab · Up 2 hours (healthy)", state.subtitle)
        assertEquals("Running", state.badge)
        assertEquals(
            listOf(
                ContainerActionAffordance("Stop", primary = true, enabled = false),
                ContainerActionAffordance("Restart", primary = false, enabled = false),
                ContainerActionAffordance("Redeploy", primary = false, enabled = false),
            ),
            state.actions,
        )
        assertEquals(
            listOf(
                ContainerSectionState(
                    title = "Summary",
                    rows = listOf(
                        ContainerFactRow("Image", "bytemark/webdav:latest"),
                        ContainerFactRow("Uptime", "Up 2 hours (healthy)"),
                        ContainerFactRow("IP address", "172.19.0.4"),
                        ContainerFactRow("Status", "running"),
                    ),
                ),
                ContainerSectionState(
                    title = "Identity & lifecycle",
                    rows = listOf(
                        ContainerFactRow("Container ID", "8f6c5b4a3d2e9f00112233445566778899"),
                        ContainerFactRow("Created", "2026-06-15T10:15:30Z"),
                        ContainerFactRow("Started", "2026-06-15T10:16:01Z"),
                        ContainerFactRow("Restart policy", "unless-stopped"),
                        ContainerFactRow("Auto-update", "Disabled"),
                    ),
                ),
                ContainerSectionState(
                    title = "Connectivity & storage",
                    rows = listOf(
                        ContainerFactRow("Ports", "1 published · 1 exposed"),
                        ContainerFactRow("Volumes", "2 mounts"),
                        ContainerFactRow("Networks", "1 network"),
                    ),
                ),
                ContainerSectionState(
                    title = "Runtime configuration",
                    rows = listOf(
                        ContainerFactRow("Image ID", "sha256:imageabc"),
                        ContainerFactRow("Working directory", "/var/www"),
                        ContainerFactRow("Entrypoint", "docker-entrypoint.sh"),
                        ContainerFactRow("Command", "nginx -g daemon off;"),
                        ContainerFactRow("Compose project", "utilities"),
                        ContainerFactRow("Compose service", "webdav"),
                    ),
                ),
            ),
            state.sections,
        )
        assertEquals(
            listOf(
                ContainerSectionLink("Runtime", "Metrics, logs, and health are next"),
                ContainerSectionLink("Connectivity & Storage", "Network and mount details are next"),
                ContainerSectionLink("Advanced", "Configuration, Compose, Shell, and Inspect are next"),
            ),
            state.futureSections,
        )
    }

    @Test
    fun `container detail overview omits unavailable fields and falls back to selected row`() {
        val state = containerDetailScreenState(
            selectedEnvironmentName = "v0idLab",
            selectedContainer = ArcaneContainer(
                id = "abc123",
                name = "redis",
                image = "redis:7",
                state = "exited",
                status = "Exited (0) 1 minute ago",
            ),
            detailState = ContainerDetailUiState.Loading,
        )

        assertEquals("redis", state.title)
        assertEquals("v0idLab · Exited (0) 1 minute ago", state.subtitle)
        assertEquals("Stopped", state.badge)
        assertEquals(listOf(ContainerActionAffordance("Start", primary = true, enabled = false)), state.actions)
        assertEquals(
            listOf(
                ContainerSectionState(
                    title = "Summary",
                    rows = listOf(
                        ContainerFactRow("Image", "redis:7"),
                        ContainerFactRow("Status", "Exited (0) 1 minute ago"),
                    ),
                ),
                ContainerSectionState(
                    title = "Identity & lifecycle",
                    rows = listOf(ContainerFactRow("Container ID", "abc123")),
                ),
            ),
            state.sections,
        )
    }
}
