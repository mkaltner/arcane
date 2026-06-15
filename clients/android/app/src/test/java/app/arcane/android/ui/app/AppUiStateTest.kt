package app.arcane.android.ui.app

import app.arcane.android.data.settings.ArcaneSettings
import app.arcane.android.data.settings.AuthSession
import org.junit.Assert.assertEquals
import org.junit.Test

class AppUiStateTest {
    @Test
    fun `server without session routes to authentication`() {
        val state = ArcaneSettings(serverOrigin = "https://arcane.example").toAppUiState()

        assertEquals(AppUiState.NeedsAuthentication("https://arcane.example"), state)
    }

    @Test
    fun `server with session routes to authenticated app`() {
        val state = ArcaneSettings(
            serverOrigin = "https://arcane.example",
            authSession = AuthSession(
                token = "access-token",
                refreshToken = "refresh-token",
                expiresAt = "2026-06-15T18:00:00Z",
                username = "demo",
            ),
        ).toAppUiState()

        assertEquals(AppUiState.Authenticated(serverOrigin = "https://arcane.example", username = "demo"), state)
    }
}
