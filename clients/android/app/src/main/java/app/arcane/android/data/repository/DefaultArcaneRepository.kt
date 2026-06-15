package app.arcane.android.data.repository

import app.arcane.android.data.settings.ArcaneSettings
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
        settings.toArcaneStatus()
    }
}

internal fun ArcaneSettings.toArcaneStatus(): ArcaneStatus {
    val serverOrigin = serverOrigin
    if (serverOrigin == null) {
        return ArcaneStatus(
            title = "Connect to Arcane",
            message = "Enter an Arcane Manager URL to begin setup.",
        )
    }

    val session = authSession
    if (session == null) {
        return ArcaneStatus(
            title = "Arcane Manager connected",
            message = "Server: $serverOrigin. Sign in to continue to environment selection.",
        )
    }

    val environmentCopy = if (selectedEnvironmentId.isNullOrBlank()) {
        "Choose an environment to continue."
    } else {
        "Environment: $selectedEnvironmentId. Resource views are next."
    }

    return ArcaneStatus(
        title = "Arcane Manager ready",
        message = "Server: $serverOrigin. Signed in as ${session.username}. $environmentCopy",
    )
}
