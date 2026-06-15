package app.arcane.android.ui.connect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.arcane.android.data.settings.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {
    var formState by mutableStateOf(ConnectFormState())
        private set

    fun onServerUrlChanged(value: String) {
        formState = formState.copy(rawServerUrl = value)
    }

    fun continueToServer() {
        if (!formState.canContinue) return
        val serverUrl = formState.requireServerUrl()
        viewModelScope.launch {
            formState = formState.copy(isSaving = true)
            settingsDataStore.saveServerUrl(serverUrl)
            formState = formState.copy(isSaving = false)
        }
    }
}
