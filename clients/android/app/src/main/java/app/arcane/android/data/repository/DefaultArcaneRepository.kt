package app.arcane.android.data.repository

import app.arcane.android.domain.model.ArcaneStatus
import app.arcane.android.domain.repository.ArcaneRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultArcaneRepository @Inject constructor() : ArcaneRepository {
    override fun observeStatus(): Flow<ArcaneStatus> = flowOf(
        ArcaneStatus(
            title = "Arcane client scaffold ready",
            message = "Compose, Material 3, Hilt, Ktor, serialization, and DataStore are configured.",
        ),
    )
}
