package com.example.greenmate_project.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Manages app preferences using SharedPreferences.
 * Handles theme selection and notification settings.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Gets the saved theme mode.
     * @return One of AppCompatDelegate theme mode constants.
     */
    fun getThemeMode(): Int {
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    /**
     * Saves the theme mode.
     * @param mode One of AppCompatDelegate theme mode constants.
     */
    fun setThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    /**
     * Gets whether notifications are enabled.
     * @return True if notifications are enabled, false otherwise.
     */
    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    /**
     * Saves the notification enabled preference.
     * @param enabled True to enable notifications, false to disable.
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    /**
     * Gets the notification time in minutes from midnight.
     * Default is 9:00 AM (540 minutes).
     * @return Minutes from midnight for notification time.
     */
    fun getNotificationTime(): Int {
        return prefs.getInt(KEY_NOTIFICATION_TIME, DEFAULT_NOTIFICATION_TIME)
    }

    /**
     * Saves the notification time.
     * @param minutesFromMidnight Minutes from midnight for notification time.
     */
    fun setNotificationTime(minutesFromMidnight: Int) {
        prefs.edit().putInt(KEY_NOTIFICATION_TIME, minutesFromMidnight).apply()
    }

    /**
     * Gets the notification time as formatted string (e.g., "9:00 AM").
     * @return Formatted time string.
     */
    fun getFormattedNotificationTime(): String {
        val minutes = getNotificationTime()
        val hours = minutes / 60
        val mins = minutes % 60
        val period = if (hours < 12) "AM" else "PM"
        val displayHours = when {
            hours == 0 -> 12
            hours > 12 -> hours - 12
            else -> hours
        }
        return String.format("%d:%02d %s", displayHours, mins, period)
    }

    /**
     * Applies the saved theme mode to the app.
     * Should be called in Application.onCreate().
     */
    fun applySavedTheme() {
        AppCompatDelegate.setDefaultNightMode(getThemeMode())
    }

    /**
     * Gets whether the user has completed onboarding.
     * @return True if onboarding is completed, false otherwise.
     */
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Saves the onboarding completion status.
     * @param completed True if onboarding is completed.
     */
    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    // ========== Statistics ==========

    /**
     * Gets the current care streak (days without overdue tasks).
     * @return Number of days in the current streak.
     */
    fun getCareStreak(): Int {
        return prefs.getInt(KEY_CARE_STREAK, 0)
    }

    /**
     * Sets the care streak value.
     * @param streak Number of days in the streak.
     */
    fun setCareStreak(streak: Int) {
        prefs.edit().putInt(KEY_CARE_STREAK, streak).apply()
    }

    /**
     * Gets the last streak update date (days since epoch).
     * @return Days since epoch when streak was last updated.
     */
    fun getLastStreakUpdateDay(): Long {
        return prefs.getLong(KEY_LAST_STREAK_UPDATE, 0L)
    }

    /**
     * Sets the last streak update day.
     * @param daysSinceEpoch Days since epoch.
     */
    fun setLastStreakUpdateDay(daysSinceEpoch: Long) {
        prefs.edit().putLong(KEY_LAST_STREAK_UPDATE, daysSinceEpoch).apply()
    }

    /**
     * Gets the count of tasks completed this week.
     * @return Number of tasks completed this week.
     */
    fun getTasksCompletedThisWeek(): Int {
        return prefs.getInt(KEY_TASKS_COMPLETED_WEEK, 0)
    }

    /**
     * Sets the tasks completed this week count.
     * @param count Number of completed tasks.
     */
    fun setTasksCompletedThisWeek(count: Int) {
        prefs.edit().putInt(KEY_TASKS_COMPLETED_WEEK, count).apply()
    }

    /**
     * Increments the tasks completed this week counter.
     */
    fun incrementTasksCompleted() {
        val current = getTasksCompletedThisWeek()
        setTasksCompletedThisWeek(current + 1)
    }

    /**
     * Gets the week number when tasks were last counted.
     * @return Week number (weeks since epoch).
     */
    fun getLastTaskCountWeek(): Long {
        return prefs.getLong(KEY_LAST_TASK_COUNT_WEEK, 0L)
    }

    /**
     * Sets the last task count week.
     * @param weekNumber Week number (weeks since epoch).
     */
    fun setLastTaskCountWeek(weekNumber: Long) {
        prefs.edit().putLong(KEY_LAST_TASK_COUNT_WEEK, weekNumber).apply()
    }

    companion object {
        private const val PREFS_NAME = "greenmate_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_NOTIFICATION_TIME = "notification_time"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val DEFAULT_NOTIFICATION_TIME = 540 // 9:00 AM

        // Statistics keys
        private const val KEY_CARE_STREAK = "care_streak"
        private const val KEY_LAST_STREAK_UPDATE = "last_streak_update"
        private const val KEY_TASKS_COMPLETED_WEEK = "tasks_completed_week"
        private const val KEY_LAST_TASK_COUNT_WEEK = "last_task_count_week"

        @Volatile
        private var instance: PreferencesManager? = null

        /**
         * Gets the singleton instance of PreferencesManager.
         * @param context Application context.
         * @return PreferencesManager instance.
         */
        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
