package app.arcane.android.data.settings

import androidx.datastore.preferences.core.preferencesOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SettingsPreferencesTest {
    @Test
    fun `empty preferences map to unset settings`() {
        val settings = preferencesOf().toArcaneSettings()

        assertNull(settings.serverOrigin)
        assertNull(settings.selectedEnvironmentId)
    }

    @Test
    fun `stored preferences map to app settings`() {
        val settings = preferencesOf(
            SettingsPreferenceKeys.ServerOrigin to "https://arcane.example.com",
            SettingsPreferenceKeys.SelectedEnvironmentId to "0",
        ).toArcaneSettings()

        assertEquals("https://arcane.example.com", settings.serverOrigin)
        assertEquals("0", settings.selectedEnvironmentId)
    }
}
