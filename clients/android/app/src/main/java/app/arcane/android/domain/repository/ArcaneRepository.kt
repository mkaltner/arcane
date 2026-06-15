package app.arcane.android.domain.repository

import app.arcane.android.domain.model.ArcaneStatus
import kotlinx.coroutines.flow.Flow

interface ArcaneRepository {
    fun observeStatus(): Flow<ArcaneStatus>
}
