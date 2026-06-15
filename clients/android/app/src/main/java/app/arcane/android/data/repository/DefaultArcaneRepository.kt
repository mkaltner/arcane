package app.arcane.android.data.repository

import app.arcane.android.data.settings.SettingsDataStore
import app.arcane.android.domain.model.ArcaneStatus
import app.arcane.android.domain.repository.ArcaneRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultArcaneRepository @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : ArcaneRepository {
    override fun observeStatus(): Flow<ArcaneStatus> = settingsDataStore.settings.map { settings ->
        val serverOrigin = settings.serverOrigin
        if (serverOrigin == null) {
            ArcaneStatus(
                title = "Connect to Arcane",
                message = "Enter an Arcane Manager URL to begin setup.",
            )
        } else {
            ArcaneStatus(
                title = "Arcane Manager connected",
                message = "Server: $serverOrigin. Authentication and environment selection are next.",
            )
        }
    }
}
