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
