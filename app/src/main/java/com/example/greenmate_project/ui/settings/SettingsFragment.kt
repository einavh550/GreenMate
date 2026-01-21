package com.example.greenmate_project.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.greenmate_project.R
import com.example.greenmate_project.util.PreferencesManager
import com.example.greenmate_project.worker.WorkerScheduler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

/**
 * Settings Fragment - App configuration options.
 * Handles theme selection, notification preferences, default plant intervals,
 * and maintenance options.
 */
class SettingsFragment : Fragment() {

    private lateinit var radioGroupTheme: RadioGroup
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var layoutNotificationTime: LinearLayout
    private lateinit var textNotificationTime: TextView
    private lateinit var sliderDefaultWater: Slider
    private lateinit var sliderDefaultFertilize: Slider
    private lateinit var textDefaultWaterValue: TextView
    private lateinit var textDefaultFertilizeValue: TextView
    private lateinit var layoutResetPreferences: LinearLayout
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferencesManager = PreferencesManager.getInstance(requireContext())
        initViews(view)
        loadSettings()
        setupListeners()
    }

    private fun initViews(view: View) {
        radioGroupTheme = view.findViewById(R.id.radio_group_theme)
        switchNotifications = view.findViewById(R.id.switch_notifications)
        layoutNotificationTime = view.findViewById(R.id.layout_notification_time)
        textNotificationTime = view.findViewById(R.id.text_notification_time)
        sliderDefaultWater = view.findViewById(R.id.slider_default_water)
        sliderDefaultFertilize = view.findViewById(R.id.slider_default_fertilize)
        textDefaultWaterValue = view.findViewById(R.id.text_default_water_value)
        textDefaultFertilizeValue = view.findViewById(R.id.text_default_fertilize_value)
        layoutResetPreferences = view.findViewById(R.id.layout_reset_preferences)
    }

    private fun loadSettings() {
        // Load saved theme setting
        when (preferencesManager.getThemeMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> {
                radioGroupTheme.check(R.id.radio_theme_light)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                radioGroupTheme.check(R.id.radio_theme_dark)
            }
            else -> {
                radioGroupTheme.check(R.id.radio_theme_system)
            }
        }

        // Load notification settings
        val notificationsEnabled = preferencesManager.areNotificationsEnabled()
        switchNotifications.isChecked = notificationsEnabled
        textNotificationTime.text = preferencesManager.getFormattedNotificationTime()
        updateNotificationTimeVisibility(notificationsEnabled)

        // Load default interval settings
        val defaultWater = preferencesManager.getDefaultWaterInterval()
        val defaultFertilize = preferencesManager.getDefaultFertilizeInterval()
        sliderDefaultWater.value = defaultWater.toFloat()
        sliderDefaultFertilize.value = defaultFertilize.toFloat()
        updateWaterIntervalText(defaultWater)
        updateFertilizeIntervalText(defaultFertilize)
    }

    private fun setupListeners() {
        // Theme selection
        radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radio_theme_light -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.radio_theme_dark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            preferencesManager.setThemeMode(mode)
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        // Notifications toggle
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setNotificationsEnabled(isChecked)
            updateNotificationTimeVisibility(isChecked)
            if (isChecked) {
                WorkerScheduler.scheduleDailyReminder(
                    requireContext(),
                    preferencesManager.getNotificationTime()
                )
            } else {
                WorkerScheduler.cancelDailyReminder(requireContext())
            }
        }

        // Notification time picker
        layoutNotificationTime.setOnClickListener {
            showTimePicker()
        }

        // Default water interval slider
        sliderDefaultWater.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val days = value.toInt()
                preferencesManager.setDefaultWaterInterval(days)
                updateWaterIntervalText(days)
            }
        }

        // Default fertilize interval slider
        sliderDefaultFertilize.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val days = value.toInt()
                preferencesManager.setDefaultFertilizeInterval(days)
                updateFertilizeIntervalText(days)
            }
        }

        // Reset preferences
        layoutResetPreferences.setOnClickListener {
            showResetConfirmationDialog()
        }
    }

    private fun updateNotificationTimeVisibility(notificationsEnabled: Boolean) {
        layoutNotificationTime.visibility = if (notificationsEnabled) View.VISIBLE else View.GONE
    }

    private fun updateWaterIntervalText(days: Int) {
        textDefaultWaterValue.text = formatIntervalText(days)
    }

    private fun updateFertilizeIntervalText(days: Int) {
        textDefaultFertilizeValue.text = formatIntervalText(days)
    }

    private fun formatIntervalText(days: Int): String {
        return if (days == 1) {
            getString(R.string.interval_every_day)
        } else {
            getString(R.string.interval_every_n_days, days)
        }
    }

    private fun showTimePicker() {
        val currentMinutes = preferencesManager.getNotificationTime()
        val currentHour = currentMinutes / 60
        val currentMinute = currentMinutes % 60

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(currentHour)
            .setMinute(currentMinute)
            .setTitleText(R.string.settings_notification_time)
            .build()

        picker.addOnPositiveButtonClickListener {
            val newMinutes = picker.hour * 60 + picker.minute
            preferencesManager.setNotificationTime(newMinutes)
            textNotificationTime.text = preferencesManager.getFormattedNotificationTime()

            // Reschedule with new time if notifications are enabled
            if (preferencesManager.areNotificationsEnabled()) {
                WorkerScheduler.scheduleDailyReminder(requireContext(), newMinutes)
            }
        }

        picker.show(parentFragmentManager, "time_picker")
    }

    private fun showResetConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_reset_confirm_title)
            .setMessage(R.string.settings_reset_confirm_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_ok) { _, _ ->
                resetPreferences()
            }
            .show()
    }

    private fun resetPreferences() {
        preferencesManager.resetLocalPreferences()

        // Apply the default theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Reload UI to reflect reset values
        loadSettings()

        // Reschedule notifications with default time
        if (preferencesManager.areNotificationsEnabled()) {
            WorkerScheduler.scheduleDailyReminder(
                requireContext(),
                preferencesManager.getNotificationTime()
            )
        }

        // Show success message
        view?.let {
            Snackbar.make(it, R.string.settings_reset_success, Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
