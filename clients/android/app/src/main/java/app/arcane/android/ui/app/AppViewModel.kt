package app.arcane.android.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        .map { settings ->
            settings.serverOrigin?.let { AppUiState.Ready(serverOrigin = it) }
                ?: AppUiState.NeedsServer
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppUiState.Loading,
        )
}

sealed interface AppUiState {
    data object Loading : AppUiState
    data object NeedsServer : AppUiState
    data class Ready(val serverOrigin: String) : AppUiState
}
