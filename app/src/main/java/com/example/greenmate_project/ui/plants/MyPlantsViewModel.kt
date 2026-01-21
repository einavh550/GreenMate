package com.example.greenmate_project.ui.plants

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.greenmate_project.data.PlantRepository
import com.example.greenmate_project.data.PlantRepositoryImpl
import com.example.greenmate_project.model.Plant
import com.example.greenmate_project.model.PlantStatus
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

/**
 * ViewModel for the My Plants screen.
 * Manages plant list data, search, filtering (status + location), and sorting.
 */
class MyPlantsViewModel : ViewModel() {

    private val plantRepository: PlantRepository = PlantRepositoryImpl()

    // All plants (unfiltered)
    private var allPlants: List<Plant> = emptyList()

    // Current search query
    private var currentSearchQuery: String = ""

    // Current status filter
    private var currentFilter: PlantFilter = PlantFilter.ALL

    // Current location filter
    private var currentLocationFilter: LocationFilter = LocationFilter.ALL

    // Current sort order
    private var currentSortOrder: SortOrder = SortOrder.NAME_ASC

    // UI state - filtered plants
    private val _filteredPlants = MutableLiveData<List<Plant>>()
    val filteredPlants: LiveData<List<Plant>> = _filteredPlants

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Event for navigation to plant details
    private val _navigateToPlantDetail = MutableLiveData<String?>()
    val navigateToPlantDetail: LiveData<String?> = _navigateToPlantDetail

    // Event for showing delete confirmation
    private val _showDeleteConfirmation = MutableLiveData<Plant?>()
    val showDeleteConfirmation: LiveData<Plant?> = _showDeleteConfirmation

    // Event for showing success message
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // Results count for display
    private val _resultsCount = MutableLiveData<Int>()
    val resultsCount: LiveData<Int> = _resultsCount

    // Whether filters are active (for showing results count)
    private val _hasActiveFilters = MutableLiveData<Boolean>()
    val hasActiveFilters: LiveData<Boolean> = _hasActiveFilters

    // Current sort order for UI
    private val _sortOrder = MutableLiveData<SortOrder>(SortOrder.NAME_ASC)
    val sortOrder: LiveData<SortOrder> = _sortOrder

    // Current location filter for UI
    private val _locationFilter = MutableLiveData<LocationFilter>(LocationFilter.ALL)
    val locationFilter: LiveData<LocationFilter> = _locationFilter

    /**
     * Loads all plants for the current user.
     */
    fun loadPlants() {
        _isLoading.value = true
        _error.value = null

        plantRepository.getAllPlants(
            onSuccess = { plantList ->
                allPlants = plantList
                applyFiltersAndSort()
                _isLoading.value = false
            },
            onError = { e ->
                _error.value = e.message
                _isLoading.value = false
                allPlants = emptyList()
                _filteredPlants.value = emptyList()
            }
        )
    }

    /**
     * Updates the search query and filters results.
     */
    fun setSearchQuery(query: String) {
        currentSearchQuery = query.trim()
        applyFiltersAndSort()
    }

    /**
     * Sets the status filter type.
     */
    fun setFilter(filter: PlantFilter) {
        currentFilter = filter
        applyFiltersAndSort()
    }

    /**
     * Sets the location filter.
     */
    fun setLocationFilter(filter: LocationFilter) {
        currentLocationFilter = filter
        _locationFilter.value = filter
        applyFiltersAndSort()
    }

    /**
     * Cycles through sort orders.
     */
    fun cycleSortOrder() {
        currentSortOrder = when (currentSortOrder) {
            SortOrder.NAME_ASC -> SortOrder.NAME_DESC
            SortOrder.NAME_DESC -> SortOrder.DATE_DESC
            SortOrder.DATE_DESC -> SortOrder.STATUS
            SortOrder.STATUS -> SortOrder.NAME_ASC
        }
        _sortOrder.value = currentSortOrder
        applyFiltersAndSort()
    }

    /**
     * Gets the current status filter.
     */
    fun getCurrentFilter(): PlantFilter = currentFilter

    /**
     * Gets the current location filter.
     */
    fun getCurrentLocationFilter(): LocationFilter = currentLocationFilter

    /**
     * Applies current search, filter (status + location), and sort settings.
     */
    private fun applyFiltersAndSort() {
        var result = allPlants

        // Apply search filter
        if (currentSearchQuery.isNotEmpty()) {
            result = result.filter { plant ->
                plant.name.contains(currentSearchQuery, ignoreCase = true) ||
                plant.location.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        // Apply status filter
        result = when (currentFilter) {
            PlantFilter.ALL -> result
            PlantFilter.NEEDS_ATTENTION -> result.filter { plant ->
                val status = calculatePlantStatus(plant)
                status == PlantStatus.NEEDS_ATTENTION || status == PlantStatus.OVERDUE
            }
            PlantFilter.HEALTHY -> result.filter { plant ->
                calculatePlantStatus(plant) == PlantStatus.HEALTHY
            }
        }

        // Apply location filter
        result = when (currentLocationFilter) {
            LocationFilter.ALL -> result
            LocationFilter.LIVING_ROOM -> result.filter { it.location.equals("Living Room", ignoreCase = true) }
            LocationFilter.BALCONY -> result.filter { it.location.equals("Balcony", ignoreCase = true) }
            LocationFilter.ROOM -> result.filter { it.location.equals("Room", ignoreCase = true) }
            LocationFilter.GARDEN -> result.filter { it.location.equals("Garden", ignoreCase = true) }
        }

        // Apply sorting
        result = when (currentSortOrder) {
            SortOrder.NAME_ASC -> result.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> result.sortedByDescending { it.name.lowercase() }
            SortOrder.DATE_DESC -> result.sortedByDescending { it.createdAt?.seconds ?: 0 }
            SortOrder.STATUS -> result.sortedWith(
                compareBy(
                    { calculatePlantStatus(it).ordinal },
                    { it.name.lowercase() }
                )
            )
        }

        _filteredPlants.value = result
        _resultsCount.value = result.size
        _hasActiveFilters.value = currentSearchQuery.isNotEmpty() ||
                currentFilter != PlantFilter.ALL ||
                currentLocationFilter != LocationFilter.ALL
    }

    /**
     * Calculates the status of a plant based on care schedule.
     */
    private fun calculatePlantStatus(plant: Plant): PlantStatus {
        val now = Timestamp.now().seconds

        val waterDaysRemaining = plant.lastWateredAt?.let { lastWatered ->
            val daysSinceWater = TimeUnit.SECONDS.toDays(now - lastWatered.seconds).toInt()
            plant.waterIntervalDays - daysSinceWater
        } ?: 0

        val fertilizeDaysRemaining = plant.lastFertilizedAt?.let { lastFertilized ->
            val daysSinceFertilize = TimeUnit.SECONDS.toDays(now - lastFertilized.seconds).toInt()
            plant.fertilizeIntervalDays - daysSinceFertilize
        } ?: 0

        val minDays = minOf(waterDaysRemaining, fertilizeDaysRemaining)

        return when {
            minDays < 0 -> PlantStatus.OVERDUE
            minDays == 0 -> PlantStatus.NEEDS_ATTENTION
            else -> PlantStatus.HEALTHY
        }
    }

    /**
     * Called when a plant is clicked to view details.
     */
    fun onPlantClicked(plant: Plant) {
        _navigateToPlantDetail.value = plant.id
    }

    /**
     * Clears the navigation event after handling.
     */
    fun onPlantDetailNavigated() {
        _navigateToPlantDetail.value = null
    }

    /**
     * Shows delete confirmation dialog for a plant.
     */
    fun confirmDeletePlant(plant: Plant) {
        _showDeleteConfirmation.value = plant
    }

    /**
     * Clears the delete confirmation event after handling.
     */
    fun onDeleteConfirmationShown() {
        _showDeleteConfirmation.value = null
    }

    /**
     * Deletes a plant from the user's collection.
     */
    fun deletePlant(plant: Plant) {
        _isLoading.value = true

        plantRepository.deletePlant(
            plantId = plant.id,
            onSuccess = {
                _successMessage.value = "Plant deleted"
                loadPlants() // Refresh the list
            },
            onError = { e ->
                _error.value = e.message
                _isLoading.value = false
            }
        )
    }

    /**
     * Clears the current error message.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clears the success message.
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Status filter options for plants.
     */
    enum class PlantFilter {
        ALL,
        NEEDS_ATTENTION,
        HEALTHY
    }

    /**
     * Location filter options.
     */
    enum class LocationFilter {
        ALL,
        LIVING_ROOM,
        BALCONY,
        ROOM,
        GARDEN
    }

    /**
     * Sort order options.
     */
    enum class SortOrder {
        NAME_ASC,
        NAME_DESC,
        DATE_DESC,
        STATUS
    }
}
