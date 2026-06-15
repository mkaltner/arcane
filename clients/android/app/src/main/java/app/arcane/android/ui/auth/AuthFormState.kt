package app.arcane.android.ui.auth

data class AuthFormState(
    val username: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val normalizedUsername: String = username.trim()

    val validationMessage: String? = when {
        normalizedUsername.isBlank() -> "Username is required"
        password.isBlank() -> "Password is required"
        else -> null
    }

    val canSubmit: Boolean = validationMessage == null && !isSubmitting
}
