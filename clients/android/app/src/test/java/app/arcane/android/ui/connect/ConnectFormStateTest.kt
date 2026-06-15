package app.arcane.android.ui.connect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectFormStateTest {
    @Test
    fun `blank URL cannot continue`() {
        val state = ConnectFormState(rawServerUrl = "  ")

        assertFalse(state.canContinue)
        assertEquals("Server URL is required", state.validationMessage)
    }

    @Test
    fun `valid manager URL can continue and exposes normalized origin`() {
        val state = ConnectFormState(rawServerUrl = " HTTPS://arcane.example.com/ ")

        assertTrue(state.canContinue)
        assertEquals("https://arcane.example.com", state.normalizedOrigin)
        assertEquals(null, state.validationMessage)
    }

    @Test
    fun `URL without HTTP scheme shows corrective validation message`() {
        val state = ConnectFormState(rawServerUrl = "arcane.example.com")

        assertFalse(state.canContinue)
        assertEquals("Server URL must start with http:// or https://", state.validationMessage)
    }
}
