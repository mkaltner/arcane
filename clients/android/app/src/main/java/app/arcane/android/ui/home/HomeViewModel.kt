package app.arcane.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.arcane.android.domain.model.ArcaneEnvironment
import app.arcane.android.domain.model.ArcaneStatus
import app.arcane.android.domain.repository.ArcaneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ArcaneRepository,
) : ViewModel() {
    private val environmentListState = MutableStateFlow<EnvironmentListUiState>(EnvironmentListUiState.Loading)
    private val dashboardSnapshotState = MutableStateFlow<DashboardSnapshotUiState>(DashboardSnapshotUiState.Loading)
    private val containerListState = MutableStateFlow<ContainerListUiState>(ContainerListUiState.Loading)
    private val containerDetailState = MutableStateFlow<ContainerDetailUiState>(ContainerDetailUiState.Loading)
    private val selectedDestination = MutableStateFlow(HomeDestination.Dashboard)
    private val selectedContainerId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        listOf(
            repository.observeStatus(),
            environmentListState,
            dashboardSnapshotState,
            containerListState,
            containerDetailState,
            selectedDestination,
            selectedContainerId,
        ),
    ) { values ->
        val status = values[0] as ArcaneStatus
        val environments = values[1] as EnvironmentListUiState
        val dashboardSnapshot = values[2] as DashboardSnapshotUiState
        val containers = values[3] as ContainerListUiState
        val containerDetail = values[4] as ContainerDetailUiState
        val destination = values[5] as HomeDestination
        val containerId = values[6] as String?
        val selectedEnvironments = if (environments is EnvironmentListUiState.Content) {
            environments.copy(selectedEnvironmentId = status.selectedEnvironmentId)
        } else {
            environments
        }
        val selectedEnvironmentId = selectedEnvironments.selectedEnvironmentIdOrNull() ?: status.selectedEnvironmentId
        val environmentList = selectedEnvironments.environmentsOrEmpty()
        val operationalDashboard = operationalDashboardState(
            environments = environmentList,
            selectedEnvironmentId = selectedEnvironmentId,
            snapshotState = dashboardSnapshot,
        )
        val selectedContainer = (containers as? ContainerListUiState.Content)
            ?.containers
            ?.firstOrNull { it.id == containerId }
        HomeUiState.Ready(
            status = status,
            environments = selectedEnvironments,
            operationalDashboard = operationalDashboard,
            navigationDrawer = homeNavigationDrawerState(
                environments = environmentList,
                selectedEnvironmentId = selectedEnvironmentId,
                selectedDestination = destination,
            ),
            selectedDestination = destination,
            containers = containersScreenState(
                selectedEnvironmentName = operationalDashboard.selectedEnvironmentName,
                containersState = containers,
            ),
            selectedContainerId = containerId,
            containerDetail = containerDetailScreenState(
                selectedEnvironmentName = operationalDashboard.selectedEnvironmentName,
                selectedContainer = selectedContainer,
                detailState = containerDetail,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading,
    )

    init {
        refreshEnvironments()
    }

    fun refreshEnvironments() {
        environmentListState.value = EnvironmentListUiState.Loading
        dashboardSnapshotState.value = DashboardSnapshotUiState.Loading
        containerListState.value = ContainerListUiState.Loading
        viewModelScope.launch {
            repository.listEnvironments()
                .onSuccess { environments ->
                    val selectedId = (uiState.value as? HomeUiState.Ready)?.status?.selectedEnvironmentId
                    val resolvedSelectedId = selectedEnvironmentIdForList(environments, selectedId)
                    if (selectedId.isNullOrBlank() && resolvedSelectedId != null) {
                        repository.selectEnvironment(resolvedSelectedId)
                    }
                    environmentListState.value = environmentListContentState(environments, resolvedSelectedId)
                    if (resolvedSelectedId != null) {
                        loadDashboard(resolvedSelectedId)
                        if (selectedDestination.value == HomeDestination.Containers) {
                            loadContainers(resolvedSelectedId)
                        }
                    } else {
                        dashboardSnapshotState.value = DashboardSnapshotUiState.Error("Select an environment to load dashboard resources.")
                        containerListState.value = ContainerListUiState.Error("Select an environment to load containers.")
                    }
                }
                .onFailure { error ->
                    environmentListState.value = EnvironmentListUiState.Error(
                        message = error.message ?: "Unable to load environments.",
                    )
                    dashboardSnapshotState.value = DashboardSnapshotUiState.Error("Unable to load environments.")
                    containerListState.value = ContainerListUiState.Error("Unable to load environments.")
                }
        }
    }

    fun selectEnvironment(environmentId: String) {
        viewModelScope.launch {
            repository.selectEnvironment(environmentId)
            environmentListState.update { state ->
                if (state is EnvironmentListUiState.Content) {
                    state.copy(selectedEnvironmentId = environmentId)
                } else {
                    state
                }
            }
            loadDashboard(environmentId)
            if (selectedDestination.value == HomeDestination.Containers) {
                loadContainers(environmentId)
            }
        }
    }

    fun selectDestination(destination: HomeDestination) {
        selectedDestination.value = destination
        if (destination != HomeDestination.ContainerDetail) {
            selectedContainerId.value = null
        }
        if (destination == HomeDestination.Containers) {
            val environmentId = (uiState.value as? HomeUiState.Ready)?.navigationDrawer?.selectedEnvironmentId
            if (environmentId != null) {
                viewModelScope.launch { loadContainers(environmentId) }
            } else {
                containerListState.value = ContainerListUiState.Error("Select an environment to load containers.")
            }
        }
    }

    fun selectContainer(containerId: String) {
        selectedContainerId.value = containerId
        selectedDestination.value = HomeDestination.ContainerDetail
        val environmentId = (uiState.value as? HomeUiState.Ready)?.navigationDrawer?.selectedEnvironmentId
        if (environmentId != null) {
            viewModelScope.launch { loadContainerDetail(environmentId, containerId) }
        } else {
            containerDetailState.value = ContainerDetailUiState.Error("Select an environment to load container details.")
        }
    }

    fun navigateBack(): Boolean {
        val nextDestination = selectedDestinationAfterBack(selectedDestination.value) ?: return false
        selectedDestination.value = nextDestination
        if (nextDestination != HomeDestination.ContainerDetail) {
            selectedContainerId.value = null
        }
        return true
    }

    private suspend fun loadDashboard(environmentId: String) {
        dashboardSnapshotState.value = DashboardSnapshotUiState.Loading
        repository.getDashboard(environmentId)
            .onSuccess { snapshot ->
                dashboardSnapshotState.value = DashboardSnapshotUiState.Content(snapshot)
            }
            .onFailure { error ->
                dashboardSnapshotState.value = DashboardSnapshotUiState.Error(
                    message = error.message ?: "Unable to load dashboard resources.",
                )
            }
    }

    private suspend fun loadContainers(environmentId: String) {
        containerListState.value = ContainerListUiState.Loading
        repository.listContainers(environmentId)
            .onSuccess { list ->
                containerListState.value = list.toContainerListUiState()
            }
            .onFailure { error ->
                containerListState.value = ContainerListUiState.Error(
                    message = error.message ?: "Unable to load containers.",
                )
            }
    }

    private suspend fun loadContainerDetail(environmentId: String, containerId: String) {
        containerDetailState.value = ContainerDetailUiState.Loading
        repository.getContainer(environmentId, containerId)
            .onSuccess { detail ->
                containerDetailState.value = ContainerDetailUiState.Content(detail)
            }
            .onFailure { error ->
                containerDetailState.value = ContainerDetailUiState.Error(
                    message = error.message ?: "Unable to load container details.",
                )
            }
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(
        val status: ArcaneStatus,
        val environments: EnvironmentListUiState,
        val operationalDashboard: OperationalDashboardState = operationalDashboardState(
            environments = emptyList(),
            selectedEnvironmentId = status.selectedEnvironmentId,
            snapshotState = DashboardSnapshotUiState.Loading,
        ),
        val navigationDrawer: HomeNavigationDrawerState = homeNavigationDrawerState(
            environments = emptyList(),
            selectedEnvironmentId = status.selectedEnvironmentId,
        ),
        val selectedDestination: HomeDestination = HomeDestination.Dashboard,
        val containers: ContainersScreenState = containersScreenState(
            selectedEnvironmentName = status.selectedEnvironmentId.orEmpty(),
            containersState = ContainerListUiState.Loading,
        ),
        val selectedContainerId: String? = null,
        val containerDetail: ContainerDetailScreenState = containerDetailScreenState(
            selectedEnvironmentName = status.selectedEnvironmentId.orEmpty(),
            selectedContainer = null,
            detailState = ContainerDetailUiState.Loading,
        ),
    ) : HomeUiState
}

sealed interface EnvironmentListUiState {
    data object Loading : EnvironmentListUiState
    data object Empty : EnvironmentListUiState
    data class Error(val message: String) : EnvironmentListUiState
    data class Content(
        val environments: List<ArcaneEnvironment>,
        val selectedEnvironmentId: String?,
    ) : EnvironmentListUiState
}

fun environmentListContentState(
    environments: List<ArcaneEnvironment>,
    selectedEnvironmentId: String?,
): EnvironmentListUiState = if (environments.isEmpty()) {
    EnvironmentListUiState.Empty
} else {
    EnvironmentListUiState.Content(
        environments = environments,
        selectedEnvironmentId = selectedEnvironmentIdForList(environments, selectedEnvironmentId),
    )
}

fun selectedEnvironmentIdForList(
    environments: List<ArcaneEnvironment>,
    selectedEnvironmentId: String?,
): String? = when {
    environments.isEmpty() -> null
    !selectedEnvironmentId.isNullOrBlank() -> selectedEnvironmentId
    else -> environments.first().id
}

private fun EnvironmentListUiState.environmentsOrEmpty(): List<ArcaneEnvironment> =
    if (this is EnvironmentListUiState.Content) environments else emptyList()

private fun EnvironmentListUiState.selectedEnvironmentIdOrNull(): String? =
    if (this is EnvironmentListUiState.Content) selectedEnvironmentId else null
