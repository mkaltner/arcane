package app.arcane.android.domain.model

data class ArcaneEnvironment(
    val id: String,
    val name: String,
    val apiUrl: String,
    val status: String,
    val enabled: Boolean,
    val isEdge: Boolean,
    val lastSeen: String? = null,
)
