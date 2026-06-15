package app.arcane.android.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthFormStateTest {
    @Test
    fun `blank credentials cannot submit`() {
        val state = AuthFormState(username = " ", password = "")

        assertFalse(state.canSubmit)
        assertEquals("Username is required", state.validationMessage)
    }

    @Test
    fun `username and password can submit with trimmed username`() {
        val state = AuthFormState(username = " demo ", password = "secret")

        assertTrue(state.canSubmit)
        assertEquals("demo", state.normalizedUsername)
        assertEquals(null, state.validationMessage)
    }

    @Test
    fun `password is required after username`() {
        val state = AuthFormState(username = "demo", password = " ")

        assertFalse(state.canSubmit)
        assertEquals("Password is required", state.validationMessage)
    }
}
