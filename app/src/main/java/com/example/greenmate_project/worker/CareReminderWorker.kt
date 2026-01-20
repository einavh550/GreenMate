package com.example.greenmate_project.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.greenmate_project.data.PlantRepositoryImpl
import com.example.greenmate_project.model.Plant
import com.example.greenmate_project.util.NotificationHelper
import com.example.greenmate_project.util.PreferencesManager
import com.google.firebase.Timestamp
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * WorkManager worker that checks plant care schedules and sends notifications.
 * Runs daily at the user's configured notification time.
 */
class CareReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val plantRepository = PlantRepositoryImpl()

    override suspend fun doWork(): Result {
        // Check if notifications are enabled
        val preferencesManager = PreferencesManager.getInstance(applicationContext)
        if (!preferencesManager.areNotificationsEnabled()) {
            return Result.success()
        }

        // Check notification permission
        if (!NotificationHelper.hasNotificationPermission(applicationContext)) {
            return Result.success()
        }

        return try {
            val plants = getPlantsSuspend()
            checkPlantsAndNotify(plants)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    /**
     * Converts the callback-based getAllPlants to a suspend function.
     */
    private suspend fun getPlantsSuspend(): List<Plant> {
        return suspendCancellableCoroutine { continuation ->
            plantRepository.getAllPlants(
                onSuccess = { plants ->
                    continuation.resume(plants)
                },
                onError = { exception ->
                    continuation.resumeWithException(exception)
                }
            )
        }
    }

    /**
     * Analyzes plants and sends appropriate notifications.
     */
    private fun checkPlantsAndNotify(plants: List<Plant>) {
        if (plants.isEmpty()) return

        val now = Timestamp.now().seconds

        var waterTodayCount = 0
        var fertilizeTodayCount = 0
        var overdueCount = 0

        val plantsNeedingWater = mutableListOf<String>()
        val plantsNeedingFertilizer = mutableListOf<String>()

        for (plant in plants) {
            // Calculate days until water needed
            val waterDaysRemaining = plant.lastWateredAt?.let { lastWatered ->
                val daysSinceWater = TimeUnit.SECONDS.toDays(now - lastWatered.seconds).toInt()
                plant.waterIntervalDays - daysSinceWater
            } ?: 0

            // Calculate days until fertilize needed
            val fertilizeDaysRemaining = plant.lastFertilizedAt?.let { lastFertilized ->
                val daysSinceFertilize = TimeUnit.SECONDS.toDays(now - lastFertilized.seconds).toInt()
                plant.fertilizeIntervalDays - daysSinceFertilize
            } ?: 0

            // Count plants needing care today
            if (waterDaysRemaining <= 0) {
                plantsNeedingWater.add(plant.name)
                if (waterDaysRemaining < 0) {
                    overdueCount++
                } else {
                    waterTodayCount++
                }
            }

            if (fertilizeDaysRemaining <= 0) {
                plantsNeedingFertilizer.add(plant.name)
                if (fertilizeDaysRemaining < 0) {
                    // Only count as overdue once per plant
                    if (waterDaysRemaining >= 0) {
                        overdueCount++
                    }
                } else {
                    fertilizeTodayCount++
                }
            }
        }

        // Send combined notification if there's anything to report
        val totalTasks = waterTodayCount + fertilizeTodayCount + overdueCount
        if (totalTasks > 0) {
            NotificationHelper.showCareReminderNotification(
                applicationContext,
                plantsNeedingWater.size,
                plantsNeedingFertilizer.size,
                overdueCount
            )
        }
    }

    companion object {
        const val WORK_NAME = "care_reminder_work"
    }
}
