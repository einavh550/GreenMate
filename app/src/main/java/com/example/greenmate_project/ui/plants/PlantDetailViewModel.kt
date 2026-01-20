package com.example.greenmate_project.ui.plants

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.greenmate_project.data.ActionRepository
import com.example.greenmate_project.data.ActionRepositoryImpl
import com.example.greenmate_project.data.PlantRepository
import com.example.greenmate_project.data.PlantRepositoryImpl
import com.example.greenmate_project.model.ActionType
import com.example.greenmate_project.model.CareAction
import com.example.greenmate_project.model.Plant
import com.example.greenmate_project.model.PlantStatus
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

/**
 * ViewModel for the Plant Detail screen.
 * Manages plant data, care actions, and history.
 */
class PlantDetailViewModel : ViewModel() {

    private val plantRepository: PlantRepository = PlantRepositoryImpl()
    private val actionRepository: ActionRepository = ActionRepositoryImpl()

    // Plant data
    private val _plant = MutableLiveData<Plant>()
    val plant: LiveData<Plant> = _plant

    // Calculated care info
    private val _waterDaysRemaining = MutableLiveData<Int>()
    val waterDaysRemaining: LiveData<Int> = _waterDaysRemaining

    private val _fertilizeDaysRemaining = MutableLiveData<Int>()
    val fertilizeDaysRemaining: LiveData<Int> = _fertilizeDaysRemaining

    private val _plantStatus = MutableLiveData<PlantStatus>()
    val plantStatus: LiveData<PlantStatus> = _plantStatus

    // Care history
    private val _careHistory = MutableLiveData<List<CareAction>>()
    val careHistory: LiveData<List<CareAction>> = _careHistory

    // UI state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _actionSuccess = MutableLiveData<ActionType?>()
    val actionSuccess: LiveData<ActionType?> = _actionSuccess

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> = _deleteSuccess

    private var currentPlantId: String? = null

    /**
     * Loads plant data and care history.
     */
    fun loadPlant(plantId: String) {
        currentPlantId = plantId
        _isLoading.value = true

        plantRepository.getPlant(
            plantId = plantId,
            onSuccess = { plant ->
                plant?.let {
                    _plant.value = it
                    calculateCareSchedule(it)
                    loadCareHistory(plantId)
                } ?: run {
                    _error.value = "Plant not found"
                    _isLoading.value = false
                }
            },
            onError = { e ->
                _error.value = e.message
                _isLoading.value = false
            }
        )
    }

    /**
     * Reloads plant data (called after returning from edit screen).
     */
    fun refreshPlant() {
        currentPlantId?.let { loadPlant(it) }
    }

    /**
     * Waters the plant and records the action.
     */
    fun waterPlant() {
        val plantId = currentPlantId ?: return
        _isLoading.value = true

        plantRepository.markWatered(
            plantId = plantId,
            onSuccess = {
                // Record the action in history
                val action = CareAction(
                    type = ActionType.WATER,
                    performedAt = Timestamp.now()
                )
                actionRepository.addAction(
                    plantId = plantId,
                    action = action,
                    onSuccess = {
                        _actionSuccess.value = ActionType.WATER
                        refreshPlant()
                    },
                    onError = { e ->
                        // Plant was watered but history failed - still refresh
                        _actionSuccess.value = ActionType.WATER
                        refreshPlant()
                    }
                )
            },
            onError = { e ->
                _error.value = e.message
                _isLoading.value = false
            }
        )
    }

    /**
     * Fertilizes the plant and records the action.
     */
    fun fertilizePlant() {
        val plantId = currentPlantId ?: return
        _isLoading.value = true

        plantRepository.markFertilized(
            plantId = plantId,
            onSuccess = {
                // Record the action in history
                val action = CareAction(
                    type = ActionType.FERTILIZE,
                    performedAt = Timestamp.now()
                )
                actionRepository.addAction(
                    plantId = plantId,
                    action = action,
                    onSuccess = {
                        _actionSuccess.value = ActionType.FERTILIZE
                        refreshPlant()
                    },
                    onError = { e ->
                        // Plant was fertilized but history failed - still refresh
                        _actionSuccess.value = ActionType.FERTILIZE
                        refreshPlant()
                    }
                )
            },
            onError = { e ->
                _error.value = e.message
                _isLoading.value = false
            }
        )
    }

    /**
     * Deletes the plant.
     */
    fun deletePlant() {
        val plantId = currentPlantId ?: return
        _isLoading.value = true

        plantRepository.deletePlant(
            plantId = plantId,
            onSuccess = {
                _deleteSuccess.value = true
            },
            onError = { e ->
                _error.value = e.message
                _isLoading.value = false
            }
        )
    }

    /**
     * Clears the action success event.
     */
    fun clearActionSuccess() {
        _actionSuccess.value = null
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _error.value = null
    }

    private fun calculateCareSchedule(plant: Plant) {
        val now = Timestamp.now().seconds

        // Calculate days until water needed
        val waterDays = plant.lastWateredAt?.let { lastWatered ->
            val daysSinceWater = TimeUnit.SECONDS.toDays(now - lastWatered.seconds).toInt()
            plant.waterIntervalDays - daysSinceWater
        } ?: 0

        // Calculate days until fertilize needed
        val fertilizeDays = plant.lastFertilizedAt?.let { lastFertilized ->
            val daysSinceFertilize = TimeUnit.SECONDS.toDays(now - lastFertilized.seconds).toInt()
            plant.fertilizeIntervalDays - daysSinceFertilize
        } ?: 0

        _waterDaysRemaining.value = waterDays
        _fertilizeDaysRemaining.value = fertilizeDays

        // Determine overall status
        val minDays = minOf(waterDays, fertilizeDays)
        _plantStatus.value = when {
            minDays < 0 -> PlantStatus.OVERDUE
            minDays == 0 -> PlantStatus.NEEDS_ATTENTION
            else -> PlantStatus.HEALTHY
        }
    }

    private fun loadCareHistory(plantId: String) {
        actionRepository.getRecentActions(
            plantId = plantId,
            limit = 10,
            onSuccess = { actions ->
                _careHistory.value = actions
                _isLoading.value = false
            },
            onError = { e ->
                // History load failed but plant loaded - don't show error
                _careHistory.value = emptyList()
                _isLoading.value = false
            }
        )
    }
}
