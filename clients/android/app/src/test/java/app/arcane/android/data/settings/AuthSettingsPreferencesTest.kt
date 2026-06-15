package app.arcane.android.data.settings

import androidx.datastore.preferences.core.preferencesOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthSettingsPreferencesTest {
    @Test
    fun `empty preferences have no authenticated session`() {
        val settings = preferencesOf().toArcaneSettings()

        assertNull(settings.authSession)
        assertFalse(settings.hasAuthenticatedSession)
    }

    @Test
    fun `stored auth fields map to session state`() {
        val settings = preferencesOf(
            SettingsPreferenceKeys.AuthToken to "access-token",
            SettingsPreferenceKeys.RefreshToken to "refresh-token",
            SettingsPreferenceKeys.AuthTokenExpiresAt to "2026-06-15T18:00:00Z",
            SettingsPreferenceKeys.AuthUsername to "demo",
        ).toArcaneSettings()

        assertTrue(settings.hasAuthenticatedSession)
        assertEquals(
            AuthSession(
                token = "access-token",
                refreshToken = "refresh-token",
                expiresAt = "2026-06-15T18:00:00Z",
                username = "demo",
            ),
            settings.authSession,
        )
    }

    @Test
    fun `partial auth fields do not create a session`() {
        val settings = preferencesOf(
            SettingsPreferenceKeys.AuthToken to "access-token",
            SettingsPreferenceKeys.AuthUsername to "demo",
        ).toArcaneSettings()

        assertNull(settings.authSession)
        assertFalse(settings.hasAuthenticatedSession)
    }
}
