package app.arcane.android.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.arcane.android.data.settings.ArcaneSettings
import app.arcane.android.data.settings.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore,
) : ViewModel() {
    val uiState: StateFlow<AppUiState> = settingsDataStore.settings
        .map { settings -> settings.toAppUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppUiState.Loading,
        )
}

sealed interface AppUiState {
    data object Loading : AppUiState
    data object NeedsServer : AppUiState
    data class NeedsAuthentication(val serverOrigin: String) : AppUiState
    data class Authenticated(val serverOrigin: String, val username: String) : AppUiState
}

fun ArcaneSettings.toAppUiState(): AppUiState {
    val origin = serverOrigin ?: return AppUiState.NeedsServer
    val session = authSession ?: return AppUiState.NeedsAuthentication(origin)
    return AppUiState.Authenticated(serverOrigin = origin, username = session.username)
}
