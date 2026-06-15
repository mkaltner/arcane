package app.arcane.android.ui.home

import app.arcane.android.domain.model.ArcaneDashboardSnapshot
import app.arcane.android.domain.model.ArcaneContainer
import app.arcane.android.domain.model.ArcaneContainerDetail
import app.arcane.android.domain.model.ArcaneContainerList
import app.arcane.android.domain.model.ArcaneEnvironment
import app.arcane.android.domain.model.ContainerStatusSummary
import java.util.Locale

enum class HomeDestination {
    Dashboard,
    Containers,
    ContainerDetail,
}

sealed interface DashboardSnapshotUiState {
    data object Loading : DashboardSnapshotUiState
    data class Content(val snapshot: ArcaneDashboardSnapshot) : DashboardSnapshotUiState
    data class Error(val message: String) : DashboardSnapshotUiState
}

data class DashboardMetric(
    val label: String,
    val value: String,
    val detail: String,
)

data class DashboardResourceEntry(
    val name: String,
    val value: String,
    val description: String,
    val badge: String,
    val destination: HomeDestination? = null,
)

data class OperationalDashboardState(
    val selectedEnvironmentName: String,
    val snapshotState: DashboardSnapshotUiState,
    val metrics: List<DashboardMetric>,
    val resourceEntries: List<DashboardResourceEntry>,
)

data class HomeNavigationDrawerState(
    val selectedEnvironmentId: String?,
    val environmentName: String,
    val environmentSubtitle: String,
    val environmentOptions: List<EnvironmentSelectorOption>,
    val activityCount: Int,
    val groups: List<NavigationGroup>,
)

data class EnvironmentSelectorOption(
    val id: String,
    val name: String,
    val subtitle: String,
    val selected: Boolean,
)

data class NavigationGroup(
    val title: String,
    val items: List<NavigationItem>,
)

data class NavigationItem(
    val label: String,
    val destination: HomeDestination? = null,
    val selected: Boolean = false,
    val expandable: Boolean = false,
)

sealed interface ContainerListUiState {
    data object Loading : ContainerListUiState
    data class Content(
        val containers: List<ArcaneContainer>,
        val counts: ContainerStatusSummary?,
    ) : ContainerListUiState
    data class Error(val message: String) : ContainerListUiState
}

data class ResourceStatCard(val label: String, val value: String)

data class ContainerRowState(
    val id: String,
    val name: String,
    val image: String,
    val status: String,
    val badge: String,
)

data class ContainersScreenState(
    val title: String,
    val selectedEnvironmentName: String,
    val state: ContainerListUiState,
    val stats: List<ResourceStatCard>,
    val rows: List<ContainerRowState>,
)

sealed interface ContainerDetailUiState {
    data object Loading : ContainerDetailUiState
    data class Content(val detail: ArcaneContainerDetail) : ContainerDetailUiState
    data class Error(val message: String) : ContainerDetailUiState
}

data class ContainerFactRow(val label: String, val value: String)

data class ContainerActionAffordance(
    val label: String,
    val primary: Boolean,
    val enabled: Boolean,
)

data class ContainerSectionState(
    val title: String,
    val rows: List<ContainerFactRow>,
)

data class ContainerSectionLink(
    val title: String,
    val description: String,
)

data class ContainerDetailScreenState(
    val title: String,
    val subtitle: String,
    val badge: String,
    val facts: List<ContainerFactRow>,
    val sections: List<ContainerSectionState>,
    val actions: List<ContainerActionAffordance>,
    val futureSections: List<ContainerSectionLink>,
    val detailState: ContainerDetailUiState,
)

fun operationalDashboardState(
    environments: List<ArcaneEnvironment>,
    selectedEnvironmentId: String?,
    snapshotState: DashboardSnapshotUiState,
): OperationalDashboardState {
    val selected = environments.firstOrNull { it.id == selectedEnvironmentId }
    val selectedName = selected?.name?.takeIf { it.isNotBlank() } ?: selectedEnvironmentId.orEmpty()
    val metrics = when (snapshotState) {
        is DashboardSnapshotUiState.Content -> snapshotState.snapshot.dashboardMetrics()
        DashboardSnapshotUiState.Loading,
        is DashboardSnapshotUiState.Error,
        -> emptyList()
    }
    val resourceEntries = when (snapshotState) {
        is DashboardSnapshotUiState.Content -> snapshotState.snapshot.resourceEntries()
        DashboardSnapshotUiState.Loading,
        is DashboardSnapshotUiState.Error,
        -> emptyList()
    }
    return OperationalDashboardState(
        selectedEnvironmentName = selectedName,
        snapshotState = snapshotState,
        metrics = metrics,
        resourceEntries = resourceEntries,
    )
}

fun homeNavigationDrawerState(
    environments: List<ArcaneEnvironment>,
    selectedEnvironmentId: String?,
    activityCount: Int = 8,
    selectedDestination: HomeDestination = HomeDestination.Dashboard,
): HomeNavigationDrawerState {
    val selected = environments.firstOrNull { it.id == selectedEnvironmentId } ?: environments.firstOrNull()
    return HomeNavigationDrawerState(
        selectedEnvironmentId = selected?.id,
        environmentName = selected?.name?.takeIf { it.isNotBlank() } ?: selected?.id ?: "No environment",
        environmentSubtitle = selected?.apiUrl ?: "Select an environment",
        environmentOptions = environments.map { environment ->
            EnvironmentSelectorOption(
                id = environment.id,
                name = environment.name.ifBlank { environment.id },
                subtitle = environment.apiUrl,
                selected = environment.id == selected?.id,
            )
        },
        activityCount = activityCount,
        groups = arcaneNavigationGroups(selectedDestination),
    )
}

fun containersScreenState(
    selectedEnvironmentName: String,
    containersState: ContainerListUiState,
): ContainersScreenState {
    val containers = (containersState as? ContainerListUiState.Content)?.containers.orEmpty()
    val counts = (containersState as? ContainerListUiState.Content)?.counts ?: containers.derivedCounts()
    return ContainersScreenState(
        title = "Containers",
        selectedEnvironmentName = selectedEnvironmentName,
        state = containersState,
        stats = listOf(
            ResourceStatCard("Total", counts.totalContainers.toString()),
            ResourceStatCard("Running", counts.runningContainers.toString()),
            ResourceStatCard("Stopped", counts.stoppedContainers.toString()),
        ),
        rows = containers.map { container ->
            ContainerRowState(
                id = container.id,
                name = container.name,
                image = container.image,
                status = container.status.ifBlank { container.state.ifBlank { "No status" } },
                badge = if (container.state.equals("running", ignoreCase = true)) "Running" else "Stopped",
            )
        },
    )
}

fun ArcaneContainerList.toContainerListUiState(): ContainerListUiState = ContainerListUiState.Content(
    containers = containers,
    counts = counts,
)

fun containerDetailScreenState(
    selectedEnvironmentName: String,
    selectedContainer: ArcaneContainer?,
    detailState: ContainerDetailUiState,
): ContainerDetailScreenState {
    val detail = (detailState as? ContainerDetailUiState.Content)?.detail
    val title = detail?.name?.ifBlank { selectedContainer?.name.orEmpty() }
        ?: selectedContainer?.name
        ?: "Container details"
    val image = detail?.image?.takeIf { it.isNotBlank() } ?: selectedContainer?.image.orEmpty()
    val detailStatus = detail?.status?.takeIf { it.isNotBlank() }
    val rowStatus = selectedContainer?.status?.takeIf { it.isNotBlank() } ?: selectedContainer?.state.orEmpty()
    val status = detailStatus ?: rowStatus
    val subtitleStatus = rowStatus.takeIf { it.isNotBlank() } ?: status
    val running = detail?.running ?: selectedContainer?.state.equals("running", ignoreCase = true)
    val id = detail?.id?.takeIf { it.isNotBlank() } ?: selectedContainer?.id.orEmpty()
    val sections = buildContainerDetailSections(detail, id, image, status, subtitleStatus)
    val legacyFacts = buildList {
        if (id.isNotBlank()) add(ContainerFactRow("Container ID", id))
        if (image.isNotBlank()) add(ContainerFactRow("Image", image))
        if (status.isNotBlank()) add(ContainerFactRow("Status", status))
        detail?.imageId?.takeIf { it.isNotBlank() }?.let { add(ContainerFactRow("Image ID", it)) }
        detail?.created?.takeIf { it.isNotBlank() }?.let { add(ContainerFactRow("Created", it)) }
        detail?.composeProject?.takeIf { it.isNotBlank() }?.let { add(ContainerFactRow("Compose project", it)) }
        detail?.composeService?.takeIf { it.isNotBlank() }?.let { add(ContainerFactRow("Compose service", it)) }
        detail?.mounts.orEmpty().forEachIndexed { index, mount -> add(ContainerFactRow("Mount ${index + 1}", mount)) }
    }
    return ContainerDetailScreenState(
        title = title,
        subtitle = listOf(selectedEnvironmentName, subtitleStatus).filter { it.isNotBlank() }.joinToString(" · "),
        badge = if (running) "Running" else "Stopped",
        facts = legacyFacts,
        sections = sections,
        actions = containerDetailActions(running),
        futureSections = listOf(
            ContainerSectionLink("Runtime", "Metrics, logs, and health are next"),
            ContainerSectionLink("Connectivity & Storage", "Network and mount details are next"),
            ContainerSectionLink("Advanced", "Configuration, Compose, Shell, and Inspect are next"),
        ),
        detailState = detailState,
    )
}

private fun buildContainerDetailSections(
    detail: ArcaneContainerDetail?,
    id: String,
    image: String,
    status: String,
    uptime: String,
): List<ContainerSectionState> = buildList {
    section(
        "Summary",
        listOfNotNull(
            "Image" rowIfValue image,
            "Uptime" rowIfValue detail?.let { if (it.running) uptime else "" }.orEmpty(),
            "IP address" rowIfValue detail?.primaryIpAddress.orEmpty(),
            "Status" rowIfValue (detail?.status?.takeIf { it.isNotBlank() } ?: status),
        ),
    )
    section(
        "Identity & lifecycle",
        listOfNotNull(
            "Container ID" rowIfValue id,
            "Created" rowIfValue detail?.created.orEmpty(),
            "Started" rowIfValue detail?.startedAt.orEmpty(),
            "Restart policy" rowIfValue detail?.restartPolicy.orEmpty(),
            detail?.autoUpdateEnabled?.let { ContainerFactRow("Auto-update", if (it) "Enabled" else "Disabled") },
        ),
    )
    section(
        "Connectivity & storage",
        listOfNotNull(
            "Ports" rowIfValue detail?.portsSummary.orEmpty(),
            "Volumes" rowIfValue detail?.volumesSummary.orEmpty(),
            "Networks" rowIfValue detail?.networksSummary.orEmpty(),
        ),
    )
    section(
        "Runtime configuration",
        listOfNotNull(
            "Image ID" rowIfValue detail?.imageId.orEmpty(),
            "Working directory" rowIfValue detail?.workingDirectory.orEmpty(),
            "Entrypoint" rowIfValue detail?.entrypoint.orEmpty(),
            "Command" rowIfValue detail?.command.orEmpty(),
            "Compose project" rowIfValue detail?.composeProject.orEmpty(),
            "Compose service" rowIfValue detail?.composeService.orEmpty(),
        ),
    )
}

private infix fun String.rowIfValue(value: String): ContainerFactRow? =
    value.takeIf { it.isNotBlank() }?.let { ContainerFactRow(this, it) }

private fun MutableList<ContainerSectionState>.section(title: String, rows: List<ContainerFactRow>) {
    if (rows.isNotEmpty()) add(ContainerSectionState(title, rows))
}

private fun containerDetailActions(running: Boolean): List<ContainerActionAffordance> = if (running) {
    listOf(
        ContainerActionAffordance("Stop", primary = true, enabled = false),
        ContainerActionAffordance("Restart", primary = false, enabled = false),
        ContainerActionAffordance("Redeploy", primary = false, enabled = false),
    )
} else {
    listOf(ContainerActionAffordance("Start", primary = true, enabled = false))
}

fun selectedDestinationAfterBack(destination: HomeDestination): HomeDestination? = when (destination) {
    HomeDestination.ContainerDetail -> HomeDestination.Containers
    HomeDestination.Containers -> HomeDestination.Dashboard
    HomeDestination.Dashboard -> null
}

private fun ArcaneDashboardSnapshot.dashboardMetrics(): List<DashboardMetric> = listOf(
    DashboardMetric(
        label = "Containers",
        value = containers.totalContainers.toString(),
        detail = "${containers.runningContainers} Running · ${containers.stoppedContainers} Stopped",
    ),
    DashboardMetric(
        label = "Images",
        value = images.totalImages.toString(),
        detail = "${images.imagesInUse} In Use · ${images.imagesUnused} Unused",
    ),
    DashboardMetric(
        label = "Storage",
        value = images.totalImageSize.formatBytes(),
        detail = "${images.imagesUnused} unused images",
    ),
    DashboardMetric(
        label = "Action items",
        value = actionItems.count.toString(),
        detail = actionItems.summary,
    ),
)

private fun ArcaneDashboardSnapshot.resourceEntries(): List<DashboardResourceEntry> = listOf(
    DashboardResourceEntry(
        name = "Containers",
        value = containers.totalContainers.toString(),
        description = "${containers.runningContainers} running · ${containers.stoppedContainers} stopped",
        badge = if (containers.stoppedContainers > 0) "Review" else "Healthy",
        destination = HomeDestination.Containers,
    ),
    DashboardResourceEntry(
        name = "Images",
        value = images.totalImages.toString(),
        description = "${images.imagesInUse} in use · ${images.imagesUnused} unused · ${images.totalImageSize.formatBytes()}",
        badge = if (images.imagesUnused > 0) "Cleanup" else "Healthy",
    ),
    DashboardResourceEntry(
        name = "Action items",
        value = actionItems.count.toString(),
        description = actionItems.summary,
        badge = if (actionItems.count > 0) "Attention" else "Clear",
    ),
)

private fun arcaneNavigationGroups(selectedDestination: HomeDestination): List<NavigationGroup> = listOf(
    NavigationGroup(
        title = "Management",
        items = listOf(
            NavigationItem("Dashboard", destination = HomeDestination.Dashboard, selected = selectedDestination == HomeDestination.Dashboard),
            NavigationItem("Projects"),
            NavigationItem("Environments", expandable = true),
            NavigationItem("Customization", expandable = true),
        ),
    ),
    NavigationGroup(
        title = "Resources",
        items = listOf(
            NavigationItem("Containers", destination = HomeDestination.Containers, selected = selectedDestination == HomeDestination.Containers),
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
)

private fun List<ArcaneContainer>.derivedCounts(): ContainerStatusSummary {
    val running = count { it.state.equals("running", ignoreCase = true) }
    return ContainerStatusSummary(
        runningContainers = running,
        stoppedContainers = size - running,
        totalContainers = size,
    )
}

private fun Long.formatBytes(): String {
    if (this <= 0L) return "0B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = toDouble()
    var unitIndex = 0
    while (value >= 1000.0 && unitIndex < units.lastIndex) {
        value /= 1000.0
        unitIndex += 1
    }
    return if (value >= 10 || unitIndex == 0) {
        "${value.toInt()}${units[unitIndex]}"
    } else {
        String.format(Locale.US, "%.1f%s", value, units[unitIndex])
    }
}
