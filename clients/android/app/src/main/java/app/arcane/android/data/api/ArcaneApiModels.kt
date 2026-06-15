package app.arcane.android.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

object ArcaneHeaders {
    const val ApiKey = "X-Api-Key"
}

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T,
)

@Serializable
data class PaginatedResponse<T>(
    val success: Boolean,
    val data: List<T> = emptyList(),
    val pagination: PaginationResponse,
)

@Serializable
data class PaginationResponse(
    val totalPages: Long = 0,
    val totalItems: Long = 0,
    val currentPage: Int = 1,
    val itemsPerPage: Int = 0,
    val grandTotalItems: Long? = null,
)

@Serializable
data class ErrorResponse(
    val error: String? = null,
    val title: String? = null,
    val detail: String? = null,
    val message: String? = null,
) {
    fun bestMessage(): String = error ?: detail ?: message ?: title ?: "Arcane API request failed"
}

@Serializable
data class MessageResponse(
    val message: String,
    val activityId: String? = null,
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)

@Serializable
data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val expiresAt: String,
    val user: ArcaneUser,
)

@Serializable
data class TokenRefreshResponse(
    val token: String,
    val refreshToken: String,
    val expiresAt: String,
)

@Serializable
data class ArcaneUser(
    val id: String,
    val username: String,
    val displayName: String? = null,
    val email: String? = null,
    val roleAssignments: List<RoleAssignmentSummary> = emptyList(),
    val permissionsByEnv: Map<String, List<String>> = emptyMap(),
    val isGlobalAdmin: Boolean = false,
    val canDelete: Boolean = false,
    val oidcSubjectId: String? = null,
    val locale: String? = null,
    val fontSize: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val requiresPasswordChange: Boolean = false,
)

@Serializable
data class RoleAssignmentSummary(
    val roleId: String,
    val environmentId: String? = null,
    val source: String,
)

@Serializable
data class EnvironmentSummary(
    val id: String,
    val name: String = "",
    val apiUrl: String,
    val status: String,
    val enabled: Boolean,
    val isEdge: Boolean = false,
    val lastSeen: String? = null,
    val edgeTransport: String? = null,
    val lastEdgeTransport: String? = null,
    val edgeSecurityMode: String? = null,
    val edgeSessionId: String? = null,
    val edgeAgentInstance: String? = null,
    val edgeCapabilities: List<String> = emptyList(),
)

typealias EnvironmentListResponse = PaginatedResponse<EnvironmentSummary>

@Serializable
data class ContainerListResponse(
    val success: Boolean,
    val data: List<ContainerSummary> = emptyList(),
    val groups: List<ContainerSummaryGroup> = emptyList(),
    val counts: ContainerStatusCounts? = null,
    val pagination: PaginationResponse,
)

@Serializable
data class ContainerStatusCounts(
    val runningContainers: Int = 0,
    val stoppedContainers: Int = 0,
    val totalContainers: Int = 0,
)

@Serializable
data class ContainerSummaryGroup(
    val groupName: String,
    val items: List<ContainerSummary> = emptyList(),
)

@Serializable
data class ContainerSummary(
    val id: String,
    val names: List<String> = emptyList(),
    val image: String = "",
    val imageId: String = "",
    val command: String = "",
    val created: Long = 0,
    val ports: List<ContainerPort> = emptyList(),
    val labels: Map<String, String> = emptyMap(),
    val state: String = "",
    val status: String = "",
    val hostConfig: HostConfig = HostConfig(),
    val networkSettings: NetworkSettings = NetworkSettings(),
    val mounts: List<ContainerMount> = emptyList(),
    val iconLightUrl: String? = null,
    val iconDarkUrl: String? = null,
    val updateInfo: JsonElement? = null,
    val redeployDisabled: Boolean = false,
)

@Serializable
data class ContainerDetails(
    val id: String,
    val name: String = "",
    val image: String = "",
    val imageId: String = "",
    val created: String = "",
    val state: JsonElement? = null,
    val config: JsonElement? = null,
    val hostConfig: JsonElement? = null,
    val networkSettings: JsonElement? = null,
    val mounts: List<ContainerMount> = emptyList(),
    val composeInfo: ComposeInfo? = null,
)

@Serializable
data class ContainerPort(
    val ip: String? = null,
    val privatePort: Int = 0,
    val publicPort: Int? = null,
    val type: String = "tcp",
)

@Serializable
data class ContainerMount(
    val type: String = "",
    val name: String? = null,
    val source: String? = null,
    val destination: String = "",
    val driver: String? = null,
    val mode: String? = null,
    val rw: Boolean = false,
    val propagation: String? = null,
)

@Serializable
data class HostConfig(
    val networkMode: String? = null,
)

@Serializable
data class NetworkSettings(
    val networks: Map<String, JsonElement> = emptyMap(),
)

@Serializable
data class ComposeInfo(
    val projectName: String,
    val serviceName: String,
    val workingDir: String? = null,
    val configFiles: String? = null,
)

@Serializable
data class ProjectListResponse(
    val success: Boolean,
    val data: List<ProjectDetails> = emptyList(),
    val pagination: PaginationResponse,
)

@Serializable
data class ProjectDetails(
    val id: String,
    val name: String = "",
    val path: String? = null,
    val status: String? = null,
    val services: List<JsonElement> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class ProjectRuntime(
    val services: List<JsonElement> = emptyList(),
)

@Serializable
data class DeployProjectRequest(
    val pullPolicy: String? = null,
    val forceRecreate: Boolean = false,
    val removeOrphans: Boolean = false,
)

@Serializable
data class DashboardSnapshot(
    val containers: JsonElement? = null,
    val images: JsonElement? = null,
    val volumes: JsonElement? = null,
    val networks: JsonElement? = null,
    val system: JsonElement? = null,
    val version: JsonElement? = null,
)

typealias DashboardResponse = ApiResponse<DashboardSnapshot>
