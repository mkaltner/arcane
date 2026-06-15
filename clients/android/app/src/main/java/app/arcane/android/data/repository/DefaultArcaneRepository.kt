package app.arcane.android.data.repository

import app.arcane.android.data.api.ArcaneApiClient
import app.arcane.android.data.api.DashboardActionItem
import app.arcane.android.data.api.DashboardSnapshot
import app.arcane.android.data.api.EnvironmentSummary
import app.arcane.android.data.settings.ArcaneSettings
import app.arcane.android.data.settings.SettingsDataStore
import app.arcane.android.domain.model.ActionItemsSummary
import app.arcane.android.domain.model.ArcaneDashboardSnapshot
import app.arcane.android.domain.model.ArcaneEnvironment
import app.arcane.android.domain.model.ArcaneStatus
import app.arcane.android.domain.model.ContainerStatusSummary
import app.arcane.android.domain.model.ImageUsageSummary
import app.arcane.android.domain.repository.ArcaneRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultArcaneRepository @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val apiClient: ArcaneApiClient,
) : ArcaneRepository {
    override fun observeStatus(): Flow<ArcaneStatus> = settingsDataStore.settings.map { settings ->
        settings.toArcaneStatus()
    }

    override suspend fun listEnvironments(): Result<List<ArcaneEnvironment>> = runCatching {
        apiClient.listEnvironments().data.map { summary -> summary.toArcaneEnvironment() }
    }

    override suspend fun getDashboard(environmentId: String): Result<ArcaneDashboardSnapshot> = runCatching {
        apiClient.getDashboard(environmentId).toArcaneDashboardSnapshot()
    }

    override suspend fun selectEnvironment(environmentId: String) {
        settingsDataStore.selectEnvironment(environmentId)
    }
}

internal fun EnvironmentSummary.toArcaneEnvironment(): ArcaneEnvironment = ArcaneEnvironment(
    id = id,
    name = name,
    apiUrl = apiUrl,
    status = status,
    enabled = enabled,
    isEdge = isEdge,
    lastSeen = lastSeen,
)

internal fun DashboardSnapshot.toArcaneDashboardSnapshot(): ArcaneDashboardSnapshot = ArcaneDashboardSnapshot(
    containers = ContainerStatusSummary(
        runningContainers = containers.counts.runningContainers,
        stoppedContainers = containers.counts.stoppedContainers,
        totalContainers = containers.counts.totalContainers,
    ),
    images = ImageUsageSummary(
        imagesInUse = imageUsageCounts.imagesInUse,
        imagesUnused = imageUsageCounts.imagesUnused,
        totalImages = imageUsageCounts.totalImages,
        totalImageSize = imageUsageCounts.totalImageSize,
    ),
    actionItems = actionItems.items.toActionItemsSummary(),
)

private fun List<DashboardActionItem>.toActionItemsSummary(): ActionItemsSummary {
    val total = sumOf { it.count }
    val summary = if (isEmpty() || total == 0) {
        "All clear"
    } else {
        take(2).joinToString(" · ") { item -> "${item.count} ${item.kind.toActionLabel()}" }
    }
    return ActionItemsSummary(count = total, summary = summary)
}

private fun String.toActionLabel(): String = when (this) {
    "stopped_containers" -> "Containers"
    "image_updates" -> "Image updates"
    "actionable_vulnerabilities" -> "Security"
    "expiring_keys" -> "API keys"
    else -> replace('_', ' ').replaceFirstChar { char -> char.uppercase() }
}

internal fun ArcaneSettings.toArcaneStatus(): ArcaneStatus {
    val serverOrigin = serverOrigin
    if (serverOrigin == null) {
        return ArcaneStatus(
            title = "Connect to Arcane",
            message = "Enter an Arcane Manager URL to begin setup.",
        )
    }

    val session = authSession
    if (session == null) {
        return ArcaneStatus(
            title = "Arcane Manager connected",
            message = "Server: $serverOrigin. Sign in to continue to environment selection.",
        )
    }

    val environmentCopy = if (selectedEnvironmentId.isNullOrBlank()) {
        "Choose an environment to continue."
    } else {
        "Environment: $selectedEnvironmentId. Resource views are next."
    }

    return ArcaneStatus(
        title = "Arcane Manager ready",
        message = "Server: $serverOrigin. Signed in as ${session.username}. $environmentCopy",
        selectedEnvironmentId = selectedEnvironmentId,
    )
}
