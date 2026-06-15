package app.arcane.android.domain.model

data class ArcaneDashboardSnapshot(
    val containers: ContainerStatusSummary = ContainerStatusSummary(),
    val images: ImageUsageSummary = ImageUsageSummary(),
    val actionItems: ActionItemsSummary = ActionItemsSummary(),
)

data class ContainerStatusSummary(
    val runningContainers: Int = 0,
    val stoppedContainers: Int = 0,
    val totalContainers: Int = 0,
)

data class ImageUsageSummary(
    val imagesInUse: Int = 0,
    val imagesUnused: Int = 0,
    val totalImages: Int = 0,
    val totalImageSize: Long = 0,
)

data class ActionItemsSummary(
    val count: Int = 0,
    val summary: String = "All clear",
)
