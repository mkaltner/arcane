package app.arcane.android.ui.connect

import app.arcane.android.core.network.InvalidServerUrlException
import app.arcane.android.core.network.ServerUrl

data class ConnectFormState(
    val rawServerUrl: String = "",
    val isSaving: Boolean = false,
) {
    private val parsedServerUrl: ServerUrl? = runCatching { ServerUrl.parse(rawServerUrl) }.getOrNull()
    private val validationError: String? = runCatching { ServerUrl.parse(rawServerUrl) }
        .exceptionOrNull()
        ?.let { error ->
            when (error) {
                is InvalidServerUrlException -> error.message
                else -> "Server URL is malformed"
            }
        }

    val canContinue: Boolean = parsedServerUrl != null && !isSaving
    val normalizedOrigin: String? = parsedServerUrl?.origin
    val validationMessage: String? = if (rawServerUrl.isBlank()) {
        "Server URL is required"
    } else {
        validationError
    }

    fun requireServerUrl(): ServerUrl = parsedServerUrl ?: throw InvalidServerUrlException(
        validationMessage ?: "Server URL is malformed",
    )
}
