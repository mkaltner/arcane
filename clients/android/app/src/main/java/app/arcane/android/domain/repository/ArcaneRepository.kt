package app.arcane.android.domain.repository

import app.arcane.android.domain.model.ArcaneStatus
import app.arcane.android.domain.model.ArcaneEnvironment
import kotlinx.coroutines.flow.Flow

interface ArcaneRepository {
    fun observeStatus(): Flow<ArcaneStatus>
    suspend fun listEnvironments(): Result<List<ArcaneEnvironment>>
    suspend fun selectEnvironment(environmentId: String)
}
