package app.arcane.android.domain.repository

import app.arcane.android.domain.model.ArcaneContainerDetail
import app.arcane.android.domain.model.ArcaneContainerList
import app.arcane.android.domain.model.ArcaneDashboardSnapshot
import app.arcane.android.domain.model.ArcaneEnvironment
import app.arcane.android.domain.model.ArcaneStatus
import kotlinx.coroutines.flow.Flow

interface ArcaneRepository {
    fun observeStatus(): Flow<ArcaneStatus>
    suspend fun listEnvironments(): Result<List<ArcaneEnvironment>>
    suspend fun getDashboard(environmentId: String): Result<ArcaneDashboardSnapshot>
    suspend fun listContainers(environmentId: String): Result<ArcaneContainerList>
    suspend fun getContainer(environmentId: String, containerId: String): Result<ArcaneContainerDetail>
    suspend fun selectEnvironment(environmentId: String)
}
