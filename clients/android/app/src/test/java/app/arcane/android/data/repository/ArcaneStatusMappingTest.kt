package app.arcane.android.data.repository

import app.arcane.android.data.settings.ArcaneSettings
import app.arcane.android.data.settings.AuthSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArcaneStatusMappingTest {
    @Test
    fun `authenticated session shows signed-in environment dashboard copy`() {
        val status = ArcaneSettings(
            serverOrigin = "https://arcane.example",
            authSession = AuthSession(
                token = "access-token",
                refreshToken = "refresh-token",
                expiresAt = "2026-06-15T18:00:00Z",
                username = "demo",
            ),
        ).toArcaneStatus()

        assertEquals("Dashboard overview", status.title)
        assertTrue(status.message.contains("Server: https://arcane.example"))
        assertTrue(status.message.contains("Signed in as demo"))
        assertTrue(status.message.contains("Choose an environment to load dashboard resources"))
        assertFalse(status.message.contains("Resource views are next"))
    }

    @Test
    fun `connected server without session still asks for authentication`() {
        val status = ArcaneSettings(serverOrigin = "https://arcane.example").toArcaneStatus()

        assertEquals("Arcane Manager connected", status.title)
        assertEquals(
            "Server: https://arcane.example. Sign in to continue to environment selection.",
            status.message,
        )
    }
}
