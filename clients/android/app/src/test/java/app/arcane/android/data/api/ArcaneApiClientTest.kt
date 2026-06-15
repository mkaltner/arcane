package app.arcane.android.data.api

import app.arcane.android.core.network.ServerUrl
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArcaneApiClientTest {
    private val serverUrl = ServerUrl.parse("https://arcane.example")

    @Test
    fun loginPostsCredentialsAndReturnsTypedTokens() = runBlocking {
        var capturedRequest: HttpRequestData? = null
        val engine = MockEngine { request ->
            capturedRequest = request
            respond(
                content = """
                    {
                      "success": true,
                      "data": {
                        "token": "access-token",
                        "refreshToken": "refresh-token",
                        "expiresAt": "2026-06-15T18:00:00Z",
                        "user": {"id":"u1", "username":"demo", "source":"ignored"}
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = testClient(engine)

        val response = client.login(LoginRequest(username = "demo", password = "password"))

        assertEquals(HttpMethod.Post, capturedRequest?.method)
        assertEquals("https://arcane.example/api/auth/login", capturedRequest?.url.toString())
        assertEquals("access-token", response.token)
        assertEquals("refresh-token", response.refreshToken)
        assertEquals("u1", response.user.id)
        assertEquals("demo", response.user.username)
    }

    @Test
    fun authenticatedRequestsSendBearerAndApiKeyHeaders() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
            assertEquals("api-key", request.headers[ArcaneHeaders.ApiKey])
            respond(
                content = """
                    {
                      "success": true,
                      "data": {
                        "id":"u1",
                        "username":"demo",
                        "roleAssignments": [],
                        "permissionsByEnv": {}
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = testClient(engine, ArcaneAuthState.BearerAndApiKey("access-token", "api-key"))

        val user = client.currentUser()

        assertEquals("u1", user.id)
        assertEquals("demo", user.username)
    }

    @Test
    fun unauthorizedResponsesMapToAuthException() = runBlocking {
        val engine = MockEngine {
            respond(
                content = """{"error":"invalid credentials"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = testClient(engine)

        val error = runCatching { client.currentUser() }.exceptionOrNull()

        assertTrue(error is ArcaneApiException.Unauthenticated)
        assertEquals("invalid credentials", error?.message)
    }

    @Test
    fun listContainersParsesPaginatedMvpPayloadAndIgnoresUnknownFields() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals(
                "https://arcane.example/api/environments/env%2Fone/containers?start=0&limit=20&includeInternal=false",
                request.url.toString(),
            )
            respond(
                content = """
                    {
                      "success": true,
                      "data": [{
                        "id": "c1",
                        "names": ["/web"],
                        "image": "nginx:latest",
                        "state": "running",
                        "status": "Up 2 minutes",
                        "unexpectedBackendField": "ignored"
                      }],
                      "groups": [],
                      "counts": {"runningContainers": 1, "stoppedContainers": 0, "totalContainers": 1},
                      "pagination": {"totalPages":1,"totalItems":1,"currentPage":1,"itemsPerPage":20}
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = testClient(engine)

        val containers = client.listContainers(environmentId = "env/one")

        assertEquals(1, containers.data.size)
        assertEquals("c1", containers.data.single().id)
        assertEquals("running", containers.data.single().state)
        assertEquals(1, containers.counts?.runningContainers)
    }

    @Test
    fun jsonModelsTolerateExtraBackendFields() {
        val json = ArcaneJson.create()

        val user = json.decodeFromString<ArcaneUser>(
            """
                {
                  "id": "u1",
                  "username": "demo",
                  "displayName": "Demo User",
                  "permissionsByEnv": {"env1": ["containers:read"]},
                  "roleAssignments": [],
                  "newServerField": true
                }
            """.trimIndent(),
        )

        assertEquals("u1", user.id)
        assertEquals(listOf("containers:read"), user.permissionsByEnv["env1"])
    }

    private fun testClient(engine: MockEngine, authState: ArcaneAuthState? = null): ArcaneApiClient {
        val json = ArcaneJson.create()
        return ArcaneApiClient(
            httpClient = createArcaneHttpClient(json, engine),
            serverUrlProvider = StaticServerUrlProvider(serverUrl),
            authProvider = StaticArcaneAuthProvider(authState),
            json = json,
        )
    }
}
