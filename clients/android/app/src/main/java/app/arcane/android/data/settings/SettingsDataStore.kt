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

data class AuthSession(
    val token: String,
    val refreshToken: String,
    val expiresAt: String,
    val username: String,
)

data class ArcaneSettings(
    val serverOrigin: String? = null,
    val selectedEnvironmentId: String? = null,
    val authSession: AuthSession? = null,
) {
    val hasServer: Boolean = serverOrigin != null
    val hasAuthenticatedSession: Boolean = authSession != null
}

internal object SettingsPreferenceKeys {
    val ServerOrigin = stringPreferencesKey("server_origin")
    val SelectedEnvironmentId = stringPreferencesKey("selected_environment_id")
    val AuthToken = stringPreferencesKey("auth_token")
    val RefreshToken = stringPreferencesKey("refresh_token")
    val AuthTokenExpiresAt = stringPreferencesKey("auth_token_expires_at")
    val AuthUsername = stringPreferencesKey("auth_username")
}

internal fun Preferences.toArcaneSettings(): ArcaneSettings {
    val token = this[SettingsPreferenceKeys.AuthToken]
    val refreshToken = this[SettingsPreferenceKeys.RefreshToken]
    val expiresAt = this[SettingsPreferenceKeys.AuthTokenExpiresAt]
    val username = this[SettingsPreferenceKeys.AuthUsername]
    val authSession = if (
        token.isNullOrBlank() ||
        refreshToken.isNullOrBlank() ||
        expiresAt.isNullOrBlank() ||
        username.isNullOrBlank()
    ) {
        null
    } else {
        AuthSession(
            token = token,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            username = username,
        )
    }

    return ArcaneSettings(
        serverOrigin = this[SettingsPreferenceKeys.ServerOrigin],
        selectedEnvironmentId = this[SettingsPreferenceKeys.SelectedEnvironmentId],
        authSession = authSession,
    )
}

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
            clearAuthPreferences(preferences)
            preferences.remove(SettingsPreferenceKeys.SelectedEnvironmentId)
        }
    }

    suspend fun saveAuthSession(authSession: AuthSession) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferenceKeys.AuthToken] = authSession.token
            preferences[SettingsPreferenceKeys.RefreshToken] = authSession.refreshToken
            preferences[SettingsPreferenceKeys.AuthTokenExpiresAt] = authSession.expiresAt
            preferences[SettingsPreferenceKeys.AuthUsername] = authSession.username
        }
    }

    suspend fun clearAuthSession() {
        dataStore.edit { preferences ->
            clearAuthPreferences(preferences)
            preferences.remove(SettingsPreferenceKeys.SelectedEnvironmentId)
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

private fun clearAuthPreferences(preferences: androidx.datastore.preferences.core.MutablePreferences) {
    preferences.remove(SettingsPreferenceKeys.AuthToken)
    preferences.remove(SettingsPreferenceKeys.RefreshToken)
    preferences.remove(SettingsPreferenceKeys.AuthTokenExpiresAt)
    preferences.remove(SettingsPreferenceKeys.AuthUsername)
}
