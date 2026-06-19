package com.verdy.presentation.screen.plants.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verdy.domain.model.Plant
import com.verdy.domain.model.PlantEnvironment
import com.verdy.domain.usecase.environment.CreateEnvironmentUseCase
import com.verdy.domain.usecase.environment.DeleteEnvironmentUseCase
import com.verdy.domain.usecase.environment.GetAllEnvironmentsUseCase
import com.verdy.domain.usecase.environment.UpdateEnvironmentUseCase
import com.verdy.domain.usecase.plant.DeletePlantUseCase
import com.verdy.domain.usecase.plant.GetAllPlantsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlantGroup(
    val environment: PlantEnvironment?,
    val plants: List<Plant>
)

data class PlantListUiState(
    val plants: List<Plant> = emptyList(),
    val environments: List<PlantEnvironment> = emptyList(),
    val groups: List<PlantGroup> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val deletedPlantName: String? = null,
    val showManageEnvironmentsDialog: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PlantListViewModel @Inject constructor(
    private val getAllPlants: GetAllPlantsUseCase,
    private val getAllEnvironments: GetAllEnvironmentsUseCase,
    private val createEnvironmentUseCase: CreateEnvironmentUseCase,
    private val updateEnvironment: UpdateEnvironmentUseCase,
    private val deleteEnvironmentUseCase: DeleteEnvironmentUseCase,
    private val deletePlant: DeletePlantUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlantListUiState())
    val uiState: StateFlow<PlantListUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(getAllPlants(), getAllEnvironments()) { plants, environments ->
                val query = _uiState.value.searchQuery
                val filtered = plants.filter(query)
                val groups = buildGroups(filtered, environments)
                _uiState.value.copy(
                    plants = plants,
                    environments = environments,
                    groups = groups,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = state.plants.filter(query)
            state.copy(
                searchQuery = query,
                groups = buildGroups(filtered, state.environments)
            )
        }
    }

    fun showManageEnvironments() {
        _uiState.update { it.copy(showManageEnvironmentsDialog = true) }
    }

    fun dismissManageEnvironments() {
        _uiState.update { it.copy(showManageEnvironmentsDialog = false) }
    }

    fun renameEnvironment(id: Long, name: String) {
        viewModelScope.launch {
            updateEnvironment(id, name).fold(
                onSuccess = { dismissManageEnvironments() },
                onFailure = { e -> _uiState.update { it.copy(error = e.message) } }
            )
        }
    }

    fun deleteEnvironment(id: Long) {
        viewModelScope.launch {
            deleteEnvironmentUseCase(id).fold(
                onSuccess = { },
                onFailure = { e -> _uiState.update { it.copy(error = e.message) } }
            )
        }
    }

    fun createEnvironment(name: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            createEnvironmentUseCase(name).fold(
                onSuccess = { id -> onCreated(id) },
                onFailure = { e -> _uiState.update { it.copy(error = e.message) } }
            )
        }
    }

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            deletePlant(plant.id)
            _uiState.update { it.copy(deletedPlantName = plant.customName) }
        }
    }

    fun clearDeletedMessage() {
        _uiState.update { it.copy(deletedPlantName = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun environmentNameFor(plant: Plant): String? =
        plant.environmentId?.let { id ->
            _uiState.value.environments.find { it.id == id }?.name
        }

    private fun buildGroups(
        plants: List<Plant>,
        environments: List<PlantEnvironment>
    ): List<PlantGroup> {
        if (plants.isEmpty()) return emptyList()
        val groups = mutableListOf<PlantGroup>()
        environments.forEach { env ->
            val envPlants = plants.filter { it.environmentId == env.id }
            if (envPlants.isNotEmpty()) {
                groups.add(PlantGroup(environment = env, plants = envPlants))
            }
        }
        val unassigned = plants.filter { it.environmentId == null }
        if (unassigned.isNotEmpty()) {
            groups.add(PlantGroup(environment = null, plants = unassigned))
        }
        return groups
    }

    private fun List<Plant>.filter(query: String): List<Plant> {
        if (query.isBlank()) return this
        val q = query.lowercase()
        return filter {
            it.customName.lowercase().contains(q) ||
                it.commonName.lowercase().contains(q) ||
                it.scientificName?.lowercase()?.contains(q) == true
        }
    }
}
