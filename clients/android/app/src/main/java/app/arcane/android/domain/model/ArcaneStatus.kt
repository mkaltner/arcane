package app.arcane.android.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ArcaneStatus(
    val title: String,
    val message: String,
)
