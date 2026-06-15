package app.arcane.android.data.api

import app.arcane.android.core.network.ServerUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

object ArcaneJson {
    fun create(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = false
    }
}

fun interface ServerUrlProvider {
    suspend fun currentServerUrl(): ServerUrl
}

class StaticServerUrlProvider(
    private val serverUrl: ServerUrl,
) : ServerUrlProvider {
    override suspend fun currentServerUrl(): ServerUrl = serverUrl
}

sealed class ArcaneAuthState {
    data class Bearer(val token: String) : ArcaneAuthState()
    data class ApiKey(val apiKey: String) : ArcaneAuthState()
    data class BearerAndApiKey(val token: String, val apiKey: String) : ArcaneAuthState()
}

fun interface ArcaneAuthProvider {
    suspend fun currentAuthState(): ArcaneAuthState?
}

class StaticArcaneAuthProvider(
    private val authState: ArcaneAuthState? = null,
) : ArcaneAuthProvider {
    override suspend fun currentAuthState(): ArcaneAuthState? = authState
}

sealed class ArcaneApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Network(message: String, cause: Throwable? = null) : ArcaneApiException(message, cause)
    class Serialization(message: String, cause: Throwable? = null) : ArcaneApiException(message, cause)
    class Unauthenticated(message: String) : ArcaneApiException(message)
    class Forbidden(message: String) : ArcaneApiException(message)
    class NotFound(message: String) : ArcaneApiException(message)
    class Server(statusCode: Int, message: String) : ArcaneApiException("HTTP $statusCode: $message")
    class UnexpectedStatus(statusCode: Int, message: String) : ArcaneApiException("HTTP $statusCode: $message")
}

fun createArcaneHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
    configureArcaneHttpClient(json)
}

fun createArcaneHttpClient(json: Json, engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    configureArcaneHttpClient(json)
}

private fun HttpClientConfigScope.configureArcaneHttpClient(json: Json) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(json)
    }
    defaultRequest {
        contentType(ContentType.Application.Json)
    }
}

private typealias HttpClientConfigScope = io.ktor.client.HttpClientConfig<*>

@Singleton
class ArcaneApiClient @Inject constructor(
    private val httpClient: HttpClient,
    private val serverUrlProvider: ServerUrlProvider,
    private val authProvider: ArcaneAuthProvider = StaticArcaneAuthProvider(),
    private val json: Json = ArcaneJson.create(),
) {
    suspend fun login(request: LoginRequest): LoginResponse = post<ApiResponse<LoginResponse>>("auth/login", body = request).data

    suspend fun refreshToken(request: RefreshTokenRequest): TokenRefreshResponse =
        post<ApiResponse<TokenRefreshResponse>>("auth/refresh", body = request).data

    suspend fun logout(): MessageResponse = post<ApiResponse<MessageResponse>>("auth/logout").data

    suspend fun currentUser(): ArcaneUser = get<ApiResponse<ArcaneUser>>("auth/me").data

    suspend fun listEnvironments(
        search: String? = null,
        start: Int = 0,
        limit: Int = 20,
    ): EnvironmentListResponse = get("environments${paginationQuery(search = search, start = start, limit = limit)}")

    suspend fun getEnvironment(environmentId: String): EnvironmentSummary =
        get<ApiResponse<EnvironmentSummary>>("environments/${environmentId.pathSegment()}").data

    suspend fun getDashboard(environmentId: String): DashboardSnapshot =
        get<DashboardResponse>("environments/${environmentId.pathSegment()}/dashboard").data

    suspend fun listContainers(
        environmentId: String,
        search: String? = null,
        start: Int = 0,
        limit: Int = 20,
        includeInternal: Boolean = false,
    ): ContainerListResponse = get(
        "environments/${environmentId.pathSegment()}/containers" +
            paginationQuery(search = search, start = start, limit = limit, extra = "includeInternal=$includeInternal"),
    )

    suspend fun getContainer(environmentId: String, containerId: String): ContainerDetails =
        get<ApiResponse<ContainerDetails>>("environments/${environmentId.pathSegment()}/containers/${containerId.pathSegment()}").data

    suspend fun startContainer(environmentId: String, containerId: String): MessageResponse =
        containerAction(environmentId, containerId, "start")

    suspend fun stopContainer(environmentId: String, containerId: String): MessageResponse =
        containerAction(environmentId, containerId, "stop")

    suspend fun restartContainer(environmentId: String, containerId: String): MessageResponse =
        containerAction(environmentId, containerId, "restart")

    suspend fun listProjects(
        environmentId: String,
        search: String? = null,
        start: Int = 0,
        limit: Int = 20,
    ): ProjectListResponse = get(
        "environments/${environmentId.pathSegment()}/projects${paginationQuery(search = search, start = start, limit = limit)}",
    )

    suspend fun getProject(environmentId: String, projectId: String): ProjectDetails =
        get<ApiResponse<ProjectDetails>>("environments/${environmentId.pathSegment()}/projects/${projectId.pathSegment()}").data

    suspend fun getProjectRuntime(environmentId: String, projectId: String): ProjectRuntime =
        get<ApiResponse<ProjectRuntime>>("environments/${environmentId.pathSegment()}/projects/${projectId.pathSegment()}/runtime").data

    suspend fun deployProject(
        environmentId: String,
        projectId: String,
        request: DeployProjectRequest? = null,
    ): MessageResponse = post<ApiResponse<MessageResponse>>(
        "environments/${environmentId.pathSegment()}/projects/${projectId.pathSegment()}/up",
        body = request ?: DeployProjectRequest(),
    ).data

    suspend fun stopProject(environmentId: String, projectId: String): MessageResponse =
        post<ApiResponse<MessageResponse>>("environments/${environmentId.pathSegment()}/projects/${projectId.pathSegment()}/down").data

    suspend fun restartProject(environmentId: String, projectId: String): MessageResponse =
        post<ApiResponse<MessageResponse>>("environments/${environmentId.pathSegment()}/projects/${projectId.pathSegment()}/restart").data

    private suspend fun containerAction(environmentId: String, containerId: String, action: String): MessageResponse =
        post<ApiResponse<MessageResponse>>(
            "environments/${environmentId.pathSegment()}/containers/${containerId.pathSegment()}/$action",
        ).data

    private suspend inline fun <reified T> get(path: String): T = request(path) {
        get(urlFor(path)) {
            applyAuthHeaders()
        }
    }

    private suspend inline fun <reified T> post(path: String, body: Any? = null): T = request(path) {
        post(urlFor(path)) {
            headers.append(io.ktor.http.HttpHeaders.ContentType, ContentType.Application.Json.toString())
            contentType(ContentType.Application.Json)
            applyAuthHeaders()
            if (body != null) setBody(body)
        }
    }

    private suspend inline fun <reified T> request(
        path: String,
        crossinline block: suspend HttpClient.() -> io.ktor.client.statement.HttpResponse,
    ): T = try {
        val response = httpClient.block()
        response.body<T>()
    } catch (error: RedirectResponseException) {
        throw mapHttpError(error.response.status, error.response.bodyAsText())
    } catch (error: ClientRequestException) {
        throw mapHttpError(error.response.status, error.response.bodyAsText())
    } catch (error: ServerResponseException) {
        throw mapHttpError(error.response.status, error.response.bodyAsText())
    } catch (error: JsonConvertException) {
        throw ArcaneApiException.Serialization("Failed to parse Arcane API response for $path", error)
    } catch (error: SerializationException) {
        throw ArcaneApiException.Serialization("Failed to parse Arcane API response for $path", error)
    } catch (error: IOException) {
        throw ArcaneApiException.Network("Unable to reach Arcane server", error)
    }

    private suspend fun urlFor(path: String): String = serverUrlProvider.currentServerUrl().apiBaseUrl.trimEnd('/') + "/" + path.trimStart('/')

    private suspend fun HttpRequestBuilder.applyAuthHeaders() {
        when (val authState = authProvider.currentAuthState()) {
            is ArcaneAuthState.ApiKey -> header(ArcaneHeaders.ApiKey, authState.apiKey)
            is ArcaneAuthState.Bearer -> bearerAuth(authState.token)
            is ArcaneAuthState.BearerAndApiKey -> {
                bearerAuth(authState.token)
                header(ArcaneHeaders.ApiKey, authState.apiKey)
            }
            null -> Unit
        }
    }

    private fun mapHttpError(status: HttpStatusCode, body: String): ArcaneApiException {
        val message = runCatching { json.decodeFromString(ErrorResponse.serializer(), body).bestMessage() }
            .getOrElse { body.ifBlank { status.description } }
        return when (status) {
            HttpStatusCode.Unauthorized -> ArcaneApiException.Unauthenticated(message)
            HttpStatusCode.Forbidden -> ArcaneApiException.Forbidden(message)
            HttpStatusCode.NotFound -> ArcaneApiException.NotFound(message)
            else -> if (status.value >= 500) {
                ArcaneApiException.Server(status.value, message)
            } else {
                ArcaneApiException.UnexpectedStatus(status.value, message)
            }
        }
    }
}

private fun String.pathSegment(): String = encodeURLPathPart()

private fun paginationQuery(search: String?, start: Int, limit: Int, extra: String? = null): String {
    val params = buildList {
        add("start=$start")
        add("limit=$limit")
        if (!search.isNullOrBlank()) add("search=${search.encodeURLPathPart()}")
        if (!extra.isNullOrBlank()) add(extra)
    }
    return params.joinToString(prefix = "?", separator = "&")
}
