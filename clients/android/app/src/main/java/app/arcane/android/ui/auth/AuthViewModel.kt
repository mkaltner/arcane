package app.arcane.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.arcane.android.data.api.ArcaneApiClient
import app.arcane.android.data.api.ArcaneApiException
import app.arcane.android.data.api.LoginRequest
import app.arcane.android.data.settings.AuthSession
import app.arcane.android.data.settings.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiClient: ArcaneApiClient,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {
    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState

    fun onUsernameChanged(username: String) {
        _formState.update { it.copy(username = username, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _formState.update { it.copy(password = password, errorMessage = null) }
    }

    fun submit() {
        val current = _formState.value
        if (!current.canSubmit) return
        _formState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                val response = apiClient.login(
                    LoginRequest(
                        username = current.normalizedUsername,
                        password = current.password,
                    ),
                )
                settingsDataStore.saveAuthSession(
                    AuthSession(
                        token = response.token,
                        refreshToken = response.refreshToken,
                        expiresAt = response.expiresAt,
                        username = response.user.displayName ?: response.user.username,
                    ),
                )
            }.onFailure { error ->
                _formState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.userFacingMessage(),
                    )
                }
            }
        }
    }
}

private fun Throwable.userFacingMessage(): String = when (this) {
    is ArcaneApiException.Unauthenticated -> message ?: "Invalid username or password"
    is ArcaneApiException.Network -> "Unable to reach Arcane server"
    else -> message ?: "Unable to sign in"
}
