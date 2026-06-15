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

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeStatus(),
        environmentListState,
    ) { status, environments ->
        val selectedEnvironments = if (environments is EnvironmentListUiState.Content) {
            environments.copy(selectedEnvironmentId = status.selectedEnvironmentId)
        } else {
            environments
        }
        HomeUiState.Ready(status = status, environments = selectedEnvironments)
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
        viewModelScope.launch {
            repository.listEnvironments()
                .onSuccess { environments ->
                    val selectedId = (uiState.value as? HomeUiState.Ready)?.status?.selectedEnvironmentId
                    environmentListState.value = environmentListContentState(environments, selectedId)
                }
                .onFailure { error ->
                    environmentListState.value = EnvironmentListUiState.Error(
                        message = error.message ?: "Unable to load environments.",
                    )
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
        }
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(
        val status: ArcaneStatus,
        val environments: EnvironmentListUiState,
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
        selectedEnvironmentId = selectedEnvironmentId,
    )
}
