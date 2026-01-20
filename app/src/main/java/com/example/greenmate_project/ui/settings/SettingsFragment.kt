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
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

/**
 * Settings Fragment - App configuration options.
 * Handles theme selection and notification preferences.
 */
class SettingsFragment : Fragment() {

    private lateinit var radioGroupTheme: RadioGroup
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var layoutNotificationTime: LinearLayout
    private lateinit var textNotificationTime: TextView
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
    }

    private fun setupListeners() {
        radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radio_theme_light -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.radio_theme_dark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            preferencesManager.setThemeMode(mode)
            AppCompatDelegate.setDefaultNightMode(mode)
        }

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

        layoutNotificationTime.setOnClickListener {
            showTimePicker()
        }
    }

    private fun updateNotificationTimeVisibility(notificationsEnabled: Boolean) {
        layoutNotificationTime.visibility = if (notificationsEnabled) View.VISIBLE else View.GONE
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

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
