package app.arcane.android.domain.model

data class ArcaneContainerList(
    val containers: List<ArcaneContainer>,
    val counts: ContainerStatusSummary?,
)

data class ArcaneContainer(
    val id: String,
    val name: String,
    val image: String = "",
    val state: String = "",
    val status: String = "",
)

data class ArcaneContainerDetail(
    val id: String,
    val name: String,
    val image: String = "",
    val imageId: String = "",
    val created: String = "",
    val status: String = "",
    val running: Boolean = false,
    val startedAt: String = "",
    val primaryIpAddress: String = "",
    val restartPolicy: String = "",
    val autoUpdateEnabled: Boolean? = null,
    val portsSummary: String = "",
    val volumesSummary: String = "",
    val networksSummary: String = "",
    val workingDirectory: String = "",
    val entrypoint: String = "",
    val command: String = "",
    val composeProject: String? = null,
    val composeService: String? = null,
    val mounts: List<String> = emptyList(),
)
