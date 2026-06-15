package app.arcane.android.ui.home

import app.arcane.android.domain.model.ArcaneDashboardSnapshot
import app.arcane.android.domain.model.ArcaneEnvironment
import java.util.Locale

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
    val selected: Boolean = false,
    val expandable: Boolean = false,
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
        groups = arcaneNavigationGroups(),
    )
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

private fun arcaneNavigationGroups(): List<NavigationGroup> = listOf(
    NavigationGroup(
        title = "Management",
        items = listOf(
            NavigationItem("Dashboard", selected = true),
            NavigationItem("Projects"),
            NavigationItem("Environments", expandable = true),
            NavigationItem("Customization", expandable = true),
        ),
    ),
    NavigationGroup(
        title = "Resources",
        items = listOf(
            NavigationItem("Containers"),
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
