package com.example.greenmate_project.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Utility class for scheduling and managing the care reminder worker.
 */
object WorkerScheduler {

    /**
     * Schedules the daily care reminder notification.
     * The worker runs once per day, checking plants and sending notifications.
     *
     * @param context Application context
     * @param notificationTimeMinutes Minutes from midnight for the notification time
     */
    fun scheduleDailyReminder(context: Context, notificationTimeMinutes: Int = 540) {
        // Calculate initial delay to the notification time
        val initialDelay = calculateInitialDelay(notificationTimeMinutes)

        // Create constraints - require network since we fetch from Firestore
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create periodic work request (runs every 24 hours)
        val workRequest = PeriodicWorkRequestBuilder<CareReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        // Schedule the work, replacing any existing work with the same name
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CareReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Cancels the scheduled care reminder notifications.
     *
     * @param context Application context
     */
    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(CareReminderWorker.WORK_NAME)
    }

    /**
     * Calculates the delay in milliseconds until the target notification time.
     * If the time has already passed today, schedules for tomorrow.
     *
     * @param targetMinutes Minutes from midnight (e.g., 540 = 9:00 AM)
     * @return Delay in milliseconds
     */
    private fun calculateInitialDelay(targetMinutes: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetMinutes / 60)
            set(Calendar.MINUTE, targetMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If target time has already passed today, schedule for tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }

    /**
     * Checks if the care reminder is currently scheduled.
     *
     * @param context Application context
     * @param callback Called with true if scheduled, false otherwise
     */
    fun isReminderScheduled(context: Context, callback: (Boolean) -> Unit) {
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(CareReminderWorker.WORK_NAME)
            .observeForever { workInfos ->
                val isScheduled = workInfos?.any { !it.state.isFinished } ?: false
                callback(isScheduled)
            }
    }
}
