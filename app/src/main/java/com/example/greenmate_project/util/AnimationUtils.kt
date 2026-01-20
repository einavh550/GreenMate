package com.example.greenmate_project.util

import android.app.Activity
import com.example.greenmate_project.R

/**
 * Utility object for activity transition animations.
 */
object AnimationUtils {

    /**
     * Apply enter animation (slide in from right).
     * Call this after startActivity().
     */
    fun applyEnterTransition(activity: Activity) {
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    /**
     * Apply exit animation (slide out to right).
     * Call this after finish().
     */
    fun applyExitTransition(activity: Activity) {
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
