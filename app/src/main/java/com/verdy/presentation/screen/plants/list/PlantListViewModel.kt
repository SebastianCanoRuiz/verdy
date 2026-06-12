package com.verdy.presentation.screen.plants.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verdy.domain.model.Plant
import com.verdy.domain.usecase.plant.DeletePlantUseCase
import com.verdy.domain.usecase.plant.GetAllPlantsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlantListUiState(
    val plants: List<Plant> = emptyList(),
    val filteredPlants: List<Plant> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val deletedPlantName: String? = null
)

@HiltViewModel
class PlantListViewModel @Inject constructor(
    private val getAllPlants: GetAllPlantsUseCase,
    private val deletePlant: DeletePlantUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlantListUiState())
    val uiState: StateFlow<PlantListUiState> = _uiState

    init {
        viewModelScope.launch {
            getAllPlants().collect { plants ->
                val query = _uiState.value.searchQuery
                _uiState.update {
                    it.copy(
                        plants = plants,
                        filteredPlants = plants.filter(query),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredPlants = it.plants.filter(query)
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
