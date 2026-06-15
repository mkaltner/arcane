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

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeStatus(),
        environmentListState,
        dashboardSnapshotState,
    ) { status, environments, dashboardSnapshot ->
        val selectedEnvironments = if (environments is EnvironmentListUiState.Content) {
            environments.copy(selectedEnvironmentId = status.selectedEnvironmentId)
        } else {
            environments
        }
        val selectedEnvironmentId = selectedEnvironments.selectedEnvironmentIdOrNull() ?: status.selectedEnvironmentId
        val environmentList = selectedEnvironments.environmentsOrEmpty()
        HomeUiState.Ready(
            status = status,
            environments = selectedEnvironments,
            operationalDashboard = operationalDashboardState(
                environments = environmentList,
                selectedEnvironmentId = selectedEnvironmentId,
                snapshotState = dashboardSnapshot,
            ),
            navigationDrawer = homeNavigationDrawerState(
                environments = environmentList,
                selectedEnvironmentId = selectedEnvironmentId,
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
                    } else {
                        dashboardSnapshotState.value = DashboardSnapshotUiState.Error("Select an environment to load dashboard resources.")
                    }
                }
                .onFailure { error ->
                    environmentListState.value = EnvironmentListUiState.Error(
                        message = error.message ?: "Unable to load environments.",
                    )
                    dashboardSnapshotState.value = DashboardSnapshotUiState.Error("Unable to load environments.")
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
        }
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
