package app.arcane.android.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ServerUrlTest {
    @Test
    fun `normalizes origin and derives api base`() {
        val config = ServerUrl.parse(" http://10.0.2.2:3552/ ")

        assertEquals("http://10.0.2.2:3552", config.origin)
        assertEquals("http://10.0.2.2:3552/api", config.apiBaseUrl)
    }

    @Test
    fun `preserves https host and path when deriving api base`() {
        val config = ServerUrl.parse("https://example.com/arcane/")

        assertEquals("https://example.com/arcane", config.origin)
        assertEquals("https://example.com/arcane/api", config.apiBaseUrl)
    }

    @Test
    fun `strips web app route before deriving api base`() {
        val config = ServerUrl.parse("https://arcane.v0idlab.net/dashboard")

        assertEquals("https://arcane.v0idlab.net", config.origin)
        assertEquals("https://arcane.v0idlab.net/api", config.apiBaseUrl)
    }

    @Test
    fun `strips api suffix before deriving api base`() {
        val config = ServerUrl.parse("https://arcane.v0idlab.net/api")

        assertEquals("https://arcane.v0idlab.net", config.origin)
        assertEquals("https://arcane.v0idlab.net/api", config.apiBaseUrl)
    }

    @Test
    fun `rejects missing scheme`() {
        assertThrows(InvalidServerUrlException::class.java) {
            ServerUrl.parse("example.com")
        }
    }

    @Test
    fun `rejects unsupported scheme`() {
        assertThrows(InvalidServerUrlException::class.java) {
            ServerUrl.parse("ftp://example.com")
        }
    }

    @Test
    fun `rejects urls without a host`() {
        assertThrows(InvalidServerUrlException::class.java) {
            ServerUrl.parse("https:///api")
        }
    }
}
