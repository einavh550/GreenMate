package com.example.greenmate_project.ui.plants

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.greenmate_project.data.PlantRepository
import com.example.greenmate_project.data.PlantRepositoryImpl
import com.example.greenmate_project.model.Plant
import com.example.greenmate_project.util.Constants
import com.google.firebase.Timestamp

/**
 * ViewModel for the Add/Edit Plant screen.
 * Handles plant creation and editing logic.
 */
class AddEditPlantViewModel : ViewModel() {

    private val plantRepository: PlantRepository = PlantRepositoryImpl()

    // Current plant being edited (null for new plant)
    private var currentPlant: Plant? = null
    private var isEditMode: Boolean = false

    // Form fields
    private val _plantName = MutableLiveData<String>("")
    val plantName: LiveData<String> = _plantName

    private val _location = MutableLiveData<String>("")
    val location: LiveData<String> = _location

    private val _waterInterval = MutableLiveData<Int>(Constants.Defaults.WATER_INTERVAL_DAYS)
    val waterInterval: LiveData<Int> = _waterInterval

    private val _fertilizeInterval = MutableLiveData<Int>(Constants.Defaults.FERTILIZE_INTERVAL_DAYS)
    val fertilizeInterval: LiveData<Int> = _fertilizeInterval

    private val _photoPath = MutableLiveData<String?>()
    val photoPath: LiveData<String?> = _photoPath

    // UI state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _nameError = MutableLiveData<String?>()
    val nameError: LiveData<String?> = _nameError

    private val _locationError = MutableLiveData<String?>()
    val locationError: LiveData<String?> = _locationError

    /**
     * Loads an existing plant for editing.
     */
    fun loadPlant(plantId: String) {
        isEditMode = true
        _isLoading.value = true

        plantRepository.getPlant(
            plantId = plantId,
            onSuccess = { plant ->
                plant?.let {
                    currentPlant = it
                    _plantName.value = it.name
                    _location.value = it.location
                    _waterInterval.value = it.waterIntervalDays
                    _fertilizeInterval.value = it.fertilizeIntervalDays
                    _photoPath.value = it.photoUrl
                }
                _isLoading.value = false
            },
            onError = { e ->
                _error.value = e.message
                _isLoading.value = false
            }
        )
    }

    /**
     * Updates the plant name.
     */
    fun setPlantName(name: String) {
        _plantName.value = name
        // Clear error when user starts typing
        if (name.isNotBlank()) {
            _nameError.value = null
        }
    }

    /**
     * Updates the location.
     */
    fun setLocation(location: String) {
        _location.value = location
        // Clear error when user selects a location
        if (location.isNotBlank()) {
            _locationError.value = null
        }
    }

    /**
     * Updates the watering interval.
     */
    fun setWaterInterval(days: Int) {
        _waterInterval.value = days
    }

    /**
     * Updates the fertilizing interval.
     */
    fun setFertilizeInterval(days: Int) {
        _fertilizeInterval.value = days
    }

    /**
     * Updates the photo path.
     */
    fun setPhotoPath(path: String?) {
        _photoPath.value = path
    }

    /**
     * Validates and saves the plant.
     */
    fun savePlant() {
        // Validate name
        val name = _plantName.value?.trim() ?: ""
        if (name.isBlank()) {
            _nameError.value = "Plant name is required"
            return
        }

        // Validate location
        val location = _location.value?.trim() ?: ""
        if (location.isBlank()) {
            _locationError.value = "Please select a location"
            return
        }

        _isLoading.value = true
        _nameError.value = null
        _locationError.value = null

        val plant = if (isEditMode && currentPlant != null) {
            // Update existing plant
            currentPlant!!.copy(
                name = name,
                location = _location.value?.trim() ?: "",
                photoUrl = _photoPath.value,
                waterIntervalDays = _waterInterval.value ?: Constants.Defaults.WATER_INTERVAL_DAYS,
                fertilizeIntervalDays = _fertilizeInterval.value ?: Constants.Defaults.FERTILIZE_INTERVAL_DAYS
            )
        } else {
            // Create new plant
            Plant(
                name = name,
                location = _location.value?.trim() ?: "",
                photoUrl = _photoPath.value,
                waterIntervalDays = _waterInterval.value ?: Constants.Defaults.WATER_INTERVAL_DAYS,
                fertilizeIntervalDays = _fertilizeInterval.value ?: Constants.Defaults.FERTILIZE_INTERVAL_DAYS,
                lastWateredAt = Timestamp.now(),
                lastFertilizedAt = Timestamp.now()
            )
        }

        if (isEditMode) {
            plantRepository.updatePlant(
                plant = plant,
                onSuccess = {
                    _isLoading.value = false
                    _saveSuccess.value = true
                },
                onError = { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
            )
        } else {
            plantRepository.addPlant(
                plant = plant,
                onSuccess = { _ ->
                    _isLoading.value = false
                    _saveSuccess.value = true
                },
                onError = { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
            )
        }
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _error.value = null
    }
}
