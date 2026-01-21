package com.example.greenmate_project.util

/**
 * Application-wide constants for GreenMate Smart Gardening app.
 */
object Constants {

    // ==================== FIRESTORE COLLECTIONS ====================

    object Firestore {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_PLANTS = "plants"
        const val COLLECTION_ACTIONS = "actions"
    }

    // ==================== SHARED PREFERENCES ====================

    object Prefs {
        const val PREF_NAME = "greenmate_prefs"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_NOTIFICATION_TIME = "notification_time"

        // Theme modes
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
    }

    // ==================== INTENT EXTRAS ====================

    object Extras {
        const val EXTRA_PLANT_ID = "extra_plant_id"
        const val EXTRA_IS_EDIT_MODE = "extra_is_edit_mode"
    }

    // ==================== DEFAULT VALUES ====================

    object Defaults {
        const val WATER_INTERVAL_DAYS = 3
        const val FERTILIZE_INTERVAL_DAYS = 14
        const val DEFAULT_WATER_INTERVAL = 3
        const val DEFAULT_FERTILIZE_INTERVAL = 14
        const val NOTIFICATION_HOUR = 9
        const val NOTIFICATION_MINUTE = 0
    }

    // ==================== WORKER TAGS ====================

    object Workers {
        const val DAILY_CARE_WORKER = "daily_care_worker"
        const val CARE_CHECK_INTERVAL_HOURS = 24L
    }

    // ==================== NOTIFICATION ====================

    object Notifications {
        const val CHANNEL_ID = "greenmate_care_reminders"
        const val CHANNEL_NAME = "Care Reminders"
        const val NOTIFICATION_ID_BASE = 1000
    }
}
