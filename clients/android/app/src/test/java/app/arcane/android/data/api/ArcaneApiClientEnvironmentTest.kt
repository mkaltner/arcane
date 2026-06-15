package app.arcane.android.data.api

import app.arcane.android.core.network.ServerUrl
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ArcaneApiClientEnvironmentTest {
    @Test
    fun `list environments calls authenticated environments endpoint`() = runBlocking {
        var requestedUrl = ""
        var authorizationHeader: String? = null
        val engine = MockEngine { request ->
            requestedUrl = request.url.toString()
            authorizationHeader = request.headers[HttpHeaders.Authorization]
            respond(
                content = """
                    {
                      "success": true,
                      "data": [
                        {
                          "id": "0",
                          "name": "Local Docker",
                          "apiUrl": "unix:///var/run/docker.sock",
                          "status": "online",
                          "enabled": true,
                          "isEdge": false
                        }
                      ],
                      "pagination": {
                        "totalPages": 1,
                        "totalItems": 1,
                        "currentPage": 1,
                        "itemsPerPage": 20
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = ArcaneApiClient(
            httpClient = createArcaneHttpClient(ArcaneJson.create(), engine),
            serverUrlProvider = StaticServerUrlProvider(ServerUrl.parse("https://arcane.example")),
            authProvider = StaticArcaneAuthProvider(ArcaneAuthState.Bearer("access-token")),
        )

        val response = client.listEnvironments()

        assertEquals("https://arcane.example/api/environments?start=0&limit=20", requestedUrl)
        assertEquals("Bearer access-token", authorizationHeader)
        assertEquals("Local Docker", response.data.single().name)
    }
}
