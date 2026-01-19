package com.example.greenmate_project.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.greenmate_project.R
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * Settings Fragment - App configuration options.
 * Handles theme selection and notification preferences.
 */
class SettingsFragment : Fragment() {

    private lateinit var radioGroupTheme: RadioGroup
    private lateinit var switchNotifications: SwitchMaterial

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadSettings()
        setupListeners()
    }

    private fun initViews(view: View) {
        radioGroupTheme = view.findViewById(R.id.radio_group_theme)
        switchNotifications = view.findViewById(R.id.switch_notifications)
    }

    private fun loadSettings() {
        // Load current theme setting
        when (AppCompatDelegate.getDefaultNightMode()) {
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

        // TODO: Load notification setting from SharedPreferences in Milestone 9
        switchNotifications.isChecked = true
    }

    private fun setupListeners() {
        radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radio_theme_light -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.radio_theme_dark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
            // TODO: Save theme preference to SharedPreferences
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Save notification preference and schedule/cancel worker in Milestone 9
        }
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
