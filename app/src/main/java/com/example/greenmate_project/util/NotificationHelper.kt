package com.example.greenmate_project.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.greenmate_project.MainActivity
import com.example.greenmate_project.R

/**
 * Helper class for managing notifications.
 * Handles notification channel creation and displaying care reminders.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "greenmate_care_reminders"
    private const val CHANNEL_NAME = "Care Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for plant watering and fertilizing reminders"

    private const val NOTIFICATION_ID_WATER = 1001
    private const val NOTIFICATION_ID_FERTILIZE = 1002
    private const val NOTIFICATION_ID_OVERDUE = 1003

    /**
     * Creates the notification channel for Android 8.0+.
     * Should be called when the app starts.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a notification for plants that need watering today.
     * @param context Application context
     * @param plantNames List of plant names that need watering
     */
    fun showWaterReminderNotification(context: Context, plantNames: List<String>) {
        if (plantNames.isEmpty()) return
        if (!hasNotificationPermission(context)) return

        val title = context.getString(R.string.notification_water_title)
        val body = if (plantNames.size == 1) {
            context.getString(R.string.notification_water_body, plantNames.first())
        } else {
            "${plantNames.size} plants need watering today"
        }

        showNotification(context, NOTIFICATION_ID_WATER, title, body)
    }

    /**
     * Shows a notification for plants that need fertilizing today.
     * @param context Application context
     * @param plantNames List of plant names that need fertilizing
     */
    fun showFertilizeReminderNotification(context: Context, plantNames: List<String>) {
        if (plantNames.isEmpty()) return
        if (!hasNotificationPermission(context)) return

        val title = context.getString(R.string.notification_fertilize_title)
        val body = if (plantNames.size == 1) {
            context.getString(R.string.notification_fertilize_body, plantNames.first())
        } else {
            "${plantNames.size} plants need fertilizing today"
        }

        showNotification(context, NOTIFICATION_ID_FERTILIZE, title, body)
    }

    /**
     * Shows a notification for overdue plant care.
     * @param context Application context
     * @param overdueCount Number of plants with overdue care
     */
    fun showOverdueNotification(context: Context, overdueCount: Int) {
        if (overdueCount <= 0) return
        if (!hasNotificationPermission(context)) return

        val title = context.getString(R.string.notification_overdue_title)
        val body = context.getString(R.string.notification_overdue_body, overdueCount)

        showNotification(context, NOTIFICATION_ID_OVERDUE, title, body)
    }

    /**
     * Shows a combined care reminder notification.
     * @param context Application context
     * @param waterCount Plants needing water
     * @param fertilizeCount Plants needing fertilizer
     * @param overdueCount Plants with overdue care
     */
    fun showCareReminderNotification(
        context: Context,
        waterCount: Int,
        fertilizeCount: Int,
        overdueCount: Int
    ) {
        if (!hasNotificationPermission(context)) return

        val tasks = mutableListOf<String>()
        if (waterCount > 0) tasks.add("$waterCount to water")
        if (fertilizeCount > 0) tasks.add("$fertilizeCount to fertilize")
        if (overdueCount > 0) tasks.add("$overdueCount overdue")

        if (tasks.isEmpty()) return

        val title = "Plant Care Reminder"
        val body = tasks.joinToString(", ")

        showNotification(context, NOTIFICATION_ID_WATER, title, body)
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        body: String
    ) {
        // Create intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_leaf)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
            e.printStackTrace()
        }
    }

    /**
     * Checks if the app has notification permission.
     * On Android 13+, explicit permission is required.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Cancels all care reminder notifications.
     */
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).apply {
            cancel(NOTIFICATION_ID_WATER)
            cancel(NOTIFICATION_ID_FERTILIZE)
            cancel(NOTIFICATION_ID_OVERDUE)
        }
    }
}
