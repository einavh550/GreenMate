package com.example.greenmate_project.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.greenmate_project.data.PlantRepository
import com.example.greenmate_project.data.PlantRepositoryImpl
import com.example.greenmate_project.model.ActionType
import com.example.greenmate_project.model.CareTask
import com.example.greenmate_project.model.Plant
import com.example.greenmate_project.model.PlantStatus
import com.example.greenmate_project.util.PreferencesManager
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

/**
 * Data class holding care statistics.
 */
data class CareStats(
    val totalPlants: Int = 0,
    val careStreak: Int = 0,
    val tasksCompletedThisWeek: Int = 0
)

/**
 * ViewModel for the Dashboard screen.
 * Manages care task data and statistics calculations.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val plantRepository: PlantRepository = PlantRepositoryImpl()
    private val preferencesManager = PreferencesManager.getInstance(application)

    // UI state
    private val _todayTasks = MutableLiveData<List<CareTask>>()
    val todayTasks: LiveData<List<CareTask>> = _todayTasks

    private val _overdueTasks = MutableLiveData<List<CareTask>>()
    val overdueTasks: LiveData<List<CareTask>> = _overdueTasks

    private val _stats = MutableLiveData<CareStats>()
    val stats: LiveData<CareStats> = _stats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Loads plants and calculates care tasks and statistics.
     */
    fun loadTasks() {
        _isLoading.value = true
        _error.value = null

        plantRepository.getAllPlants(
            onSuccess = { plants ->
                val allTasks = calculateCareTasks(plants)

                // Separate into today and overdue
                val today = allTasks.filter { it.isDueToday }
                val overdue = allTasks.filter { it.isOverdue }

                _todayTasks.value = today
                _overdueTasks.value = overdue

                // Calculate and update statistics
                updateStatistics(plants, overdue.isEmpty())

                _isLoading.value = false
            },
            onError = { e ->
                _error.value = e.message
                _isLoading.value = false
                _todayTasks.value = emptyList()
                _overdueTasks.value = emptyList()
                _stats.value = CareStats()
            }
        )
    }

    /**
     * Updates care statistics based on current plant data.
     */
    private fun updateStatistics(plants: List<Plant>, noOverdueTasks: Boolean) {
        val currentDaySinceEpoch = System.currentTimeMillis() / TimeUnit.DAYS.toMillis(1)
        val currentWeekSinceEpoch = System.currentTimeMillis() / TimeUnit.DAYS.toMillis(7)

        // Update care streak
        val lastStreakDay = preferencesManager.getLastStreakUpdateDay()
        var currentStreak = preferencesManager.getCareStreak()

        if (currentDaySinceEpoch > lastStreakDay) {
            // New day - update streak
            if (noOverdueTasks && plants.isNotEmpty()) {
                // No overdue tasks and has plants - increment or maintain streak
                if (currentDaySinceEpoch == lastStreakDay + 1) {
                    // Consecutive day
                    currentStreak++
                } else if (lastStreakDay == 0L) {
                    // First time tracking
                    currentStreak = 1
                }
                // If more than 1 day gap but no overdue, maintain current streak
            } else if (!noOverdueTasks) {
                // Has overdue tasks - reset streak
                currentStreak = 0
            }

            preferencesManager.setCareStreak(currentStreak)
            preferencesManager.setLastStreakUpdateDay(currentDaySinceEpoch)
        }

        // Check if we need to reset weekly task count
        val lastCountWeek = preferencesManager.getLastTaskCountWeek()
        if (currentWeekSinceEpoch > lastCountWeek) {
            preferencesManager.setTasksCompletedThisWeek(0)
            preferencesManager.setLastTaskCountWeek(currentWeekSinceEpoch)
        }

        _stats.value = CareStats(
            totalPlants = plants.size,
            careStreak = currentStreak,
            tasksCompletedThisWeek = preferencesManager.getTasksCompletedThisWeek()
        )
    }

    /**
     * Marks a care task as completed (waters or fertilizes the plant).
     */
    fun completeTask(task: CareTask) {
        _isLoading.value = true

        val onSuccess: () -> Unit = {
            // Increment completed tasks counter
            preferencesManager.incrementTasksCompleted()
            // Reload tasks to reflect the change
            loadTasks()
        }

        val onError: (Exception) -> Unit = { e ->
            _error.value = e.message
            _isLoading.value = false
        }

        when (task.actionType) {
            ActionType.WATER -> {
                plantRepository.markWatered(task.plant.id, onSuccess, onError)
            }
            ActionType.FERTILIZE -> {
                plantRepository.markFertilized(task.plant.id, onSuccess, onError)
            }
        }
    }

    /**
     * Clears the current error message.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Calculates care tasks from a list of plants.
     * Each plant can generate up to 2 tasks (water and fertilize).
     */
    private fun calculateCareTasks(plants: List<Plant>): List<CareTask> {
        val tasks = mutableListOf<CareTask>()
        val now = Timestamp.now().seconds

        for (plant in plants) {
            // Calculate water task
            val waterTask = calculateWaterTask(plant, now)
            if (waterTask != null && waterTask.daysUntilDue <= 1) {
                tasks.add(waterTask)
            }

            // Calculate fertilize task
            val fertilizeTask = calculateFertilizeTask(plant, now)
            if (fertilizeTask != null && fertilizeTask.daysUntilDue <= 1) {
                tasks.add(fertilizeTask)
            }
        }

        // Sort by urgency (most overdue first, then by plant name)
        return tasks.sortedWith(
            compareBy<CareTask> { it.daysUntilDue }
                .thenBy { it.plant.name }
        )
    }

    private fun calculateWaterTask(plant: Plant, nowSeconds: Long): CareTask? {
        val daysUntilDue = plant.lastWateredAt?.let { lastWatered ->
            val daysSinceWater = TimeUnit.SECONDS.toDays(nowSeconds - lastWatered.seconds).toInt()
            plant.waterIntervalDays - daysSinceWater
        } ?: 0 // If never watered, needs water now

        val status = when {
            daysUntilDue < 0 -> PlantStatus.OVERDUE
            daysUntilDue == 0 -> PlantStatus.NEEDS_ATTENTION
            else -> PlantStatus.HEALTHY
        }

        return CareTask(
            plant = plant,
            actionType = ActionType.WATER,
            daysUntilDue = daysUntilDue,
            status = status
        )
    }

    private fun calculateFertilizeTask(plant: Plant, nowSeconds: Long): CareTask? {
        val daysUntilDue = plant.lastFertilizedAt?.let { lastFertilized ->
            val daysSinceFertilize = TimeUnit.SECONDS.toDays(nowSeconds - lastFertilized.seconds).toInt()
            plant.fertilizeIntervalDays - daysSinceFertilize
        } ?: 0 // If never fertilized, needs fertilizer now

        val status = when {
            daysUntilDue < 0 -> PlantStatus.OVERDUE
            daysUntilDue == 0 -> PlantStatus.NEEDS_ATTENTION
            else -> PlantStatus.HEALTHY
        }

        return CareTask(
            plant = plant,
            actionType = ActionType.FERTILIZE,
            daysUntilDue = daysUntilDue,
            status = status
        )
    }
}
