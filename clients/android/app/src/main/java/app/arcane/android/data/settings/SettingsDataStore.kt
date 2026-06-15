package app.arcane.android.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.arcane.android.core.network.ServerUrl
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.arcaneSettingsDataStore by preferencesDataStore(name = "arcane_settings")

data class ArcaneSettings(
    val serverOrigin: String? = null,
    val selectedEnvironmentId: String? = null,
) {
    val hasServer: Boolean = serverOrigin != null
}

internal object SettingsPreferenceKeys {
    val ServerOrigin = stringPreferencesKey("server_origin")
    val SelectedEnvironmentId = stringPreferencesKey("selected_environment_id")
}

internal fun Preferences.toArcaneSettings(): ArcaneSettings = ArcaneSettings(
    serverOrigin = this[SettingsPreferenceKeys.ServerOrigin],
    selectedEnvironmentId = this[SettingsPreferenceKeys.SelectedEnvironmentId],
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.arcaneSettingsDataStore

    val settings: Flow<ArcaneSettings> = dataStore.data.map { preferences ->
        preferences.toArcaneSettings()
    }

    val serverOrigin: Flow<String?> = settings.map { it.serverOrigin }

    val selectedEnvironmentId: Flow<String?> = settings.map { it.selectedEnvironmentId }

    suspend fun saveServerUrl(serverUrl: ServerUrl) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferenceKeys.ServerOrigin] = serverUrl.origin
        }
    }

    suspend fun selectEnvironment(environmentId: String) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferenceKeys.SelectedEnvironmentId] = environmentId
        }
    }

    suspend fun clearSelectedEnvironment() {
        dataStore.edit { preferences ->
            preferences.remove(SettingsPreferenceKeys.SelectedEnvironmentId)
        }
    }
}
