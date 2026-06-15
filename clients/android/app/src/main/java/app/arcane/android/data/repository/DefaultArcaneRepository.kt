package app.arcane.android.data.repository

import app.arcane.android.data.api.ArcaneApiClient
import app.arcane.android.data.api.ContainerDetails
import app.arcane.android.data.api.ContainerListResponse
import app.arcane.android.data.api.ContainerMount
import app.arcane.android.data.api.ContainerStatusCounts
import app.arcane.android.data.api.ContainerSummary
import app.arcane.android.data.api.DashboardActionItem
import app.arcane.android.data.api.DashboardSnapshot
import app.arcane.android.data.api.EnvironmentSummary
import app.arcane.android.data.settings.ArcaneSettings
import app.arcane.android.data.settings.SettingsDataStore
import app.arcane.android.domain.model.ActionItemsSummary
import app.arcane.android.domain.model.ArcaneContainer
import app.arcane.android.domain.model.ArcaneContainerDetail
import app.arcane.android.domain.model.ArcaneContainerList
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

    override suspend fun listContainers(environmentId: String): Result<ArcaneContainerList> = runCatching {
        apiClient.listContainers(environmentId = environmentId).toArcaneContainerList()
    }

    override suspend fun getContainer(environmentId: String, containerId: String): Result<ArcaneContainerDetail> = runCatching {
        apiClient.getContainer(environmentId = environmentId, containerId = containerId).toArcaneContainerDetail()
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

internal fun ContainerListResponse.toArcaneContainerList(): ArcaneContainerList = ArcaneContainerList(
    containers = data.map { it.toArcaneContainer() },
    counts = counts?.toContainerStatusSummary(),
)

internal fun ContainerSummary.toArcaneContainer(): ArcaneContainer = ArcaneContainer(
    id = id,
    name = names.firstOrNull()?.trimStart('/')?.takeIf { it.isNotBlank() } ?: id.take(12),
    image = image,
    state = state,
    status = status,
)

internal fun ContainerDetails.toArcaneContainerDetail(): ArcaneContainerDetail {
    val portCounts = ports.distinctBy { port ->
        "${port.publicPort ?: 0}:${port.privatePort}/${port.type}"
    }.fold(0 to 0) { (published, exposed), port ->
        if ((port.publicPort ?: 0) > 0) published + 1 to exposed else published to exposed + 1
    }
    val networkCount = networkSettings.networks.size
    return ArcaneContainerDetail(
        id = id,
        name = name.trimStart('/').takeIf { it.isNotBlank() } ?: id.take(12),
        image = image,
        imageId = imageId,
        created = created,
        status = state.status,
        running = state.running,
        startedAt = state.startedAt,
        primaryIpAddress = networkSettings.networks.values.firstOrNull { it.ipAddress.isNotBlank() }?.ipAddress.orEmpty(),
        restartPolicy = hostConfig.restartPolicy.orEmpty(),
        autoUpdateEnabled = labels.isAutoUpdateEnabled(name),
        portsSummary = portCounts.toPortsSummary(),
        volumesSummary = mounts.size.toCountSummary("mount", "mounts"),
        networksSummary = networkCount.toCountSummary("network", "networks"),
        workingDirectory = config.workingDir,
        entrypoint = config.entrypoint.joinToString(" "),
        command = config.cmd.joinToString(" "),
        composeProject = composeInfo?.projectName,
        composeService = composeInfo?.serviceName,
        mounts = mounts.map { it.toFactText() },
    )
}

private fun Map<String, String>.isAutoUpdateEnabled(containerName: String): Boolean {
    val labelValue = entries.firstOrNull { (key, _) -> key.equals("com.getarcaneapp.arcane.updater", ignoreCase = true) }?.value
    return labelValue?.trim()?.lowercase() !in setOf("false", "0", "no", "off")
}

private fun Pair<Int, Int>.toPortsSummary(): String {
    val (published, exposed) = this
    return when {
        published == 0 && exposed == 0 -> "No ports"
        published > 0 && exposed > 0 -> "$published published · $exposed exposed"
        published > 0 -> published.toCountSummary("published", "published")
        else -> exposed.toCountSummary("exposed", "exposed")
    }
}

private fun Int.toCountSummary(singular: String, plural: String): String = "$this ${if (this == 1) singular else plural}"

private fun ContainerMount.toFactText(): String {
    val sourceLabel = name ?: source ?: type.ifBlank { "mount" }
    val access = if (rw) "rw" else "ro"
    return "$sourceLabel → $destination ($access)"
}

private fun ContainerStatusCounts.toContainerStatusSummary(): ContainerStatusSummary = ContainerStatusSummary(
    runningContainers = runningContainers,
    stoppedContainers = stoppedContainers,
    totalContainers = totalContainers,
)

private fun List<DashboardActionItem>.toActionItemsSummary(): ActionItemsSummary {
    val visibleItems = filter { it.count > 0 }
    val summary = if (visibleItems.isEmpty()) {
        "All clear"
    } else {
        visibleItems.take(2).joinToString(" · ") { item -> "${item.count} ${item.kind.toActionLabel()}" }
    }
    return ActionItemsSummary(count = visibleItems.size, summary = summary)
}

private fun String.toActionLabel(): String = when (this) {
    "stopped_containers" -> "Containers"
    "image_updates" -> "Updates"
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
        "Choose an environment to load dashboard resources."
    } else {
        "Environment: $selectedEnvironmentId. Loading containers, images, storage, and action items."
    }

    return ArcaneStatus(
        title = "Dashboard overview",
        message = "Server: $serverOrigin. Signed in as ${session.username}. $environmentCopy",
        selectedEnvironmentId = selectedEnvironmentId,
    )
}
