package com.example.greenmate_project

import android.app.Application
import com.example.greenmate_project.util.NotificationHelper
import com.example.greenmate_project.util.PreferencesManager
import com.example.greenmate_project.worker.WorkerScheduler
import com.google.firebase.FirebaseApp

/**
 * Application class for GreenMate.
 * Initializes Firebase and applies saved settings when the app starts.
 *
 * Note: This is an academic project using Firebase Free tier (Spark plan).
 */
class GreenMateApp : Application() {

    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize PreferencesManager and apply saved theme
        preferencesManager = PreferencesManager.getInstance(this)
        preferencesManager.applySavedTheme()

        // Create notification channel (required for Android 8.0+)
        NotificationHelper.createNotificationChannel(this)

        // Schedule care reminders if notifications are enabled
        if (preferencesManager.areNotificationsEnabled()) {
            WorkerScheduler.scheduleDailyReminder(
                this,
                preferencesManager.getNotificationTime()
            )
        }

        // Initialize Firebase
        // This is automatically done by the google-services plugin,
        FirebaseApp.initializeApp(this)
    }
}
