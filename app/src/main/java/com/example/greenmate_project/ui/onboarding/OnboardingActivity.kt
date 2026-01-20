package com.example.greenmate_project.ui.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.greenmate_project.MainActivity
import com.example.greenmate_project.R
import com.example.greenmate_project.util.PreferencesManager
import com.google.android.material.button.MaterialButton

/**
 * Onboarding activity shown on first app launch.
 * Introduces the app features and requests necessary permissions.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnSkip: TextView
    private lateinit var btnNext: MaterialButton
    private lateinit var indicator1: View
    private lateinit var indicator2: View
    private lateinit var indicator3: View

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var onboardingAdapter: OnboardingAdapter

    private val pages = listOf(
        OnboardingPage(
            imageRes = R.drawable.ic_onboarding_plants,
            titleRes = R.string.onboarding_title_1,
            descriptionRes = R.string.onboarding_desc_1
        ),
        OnboardingPage(
            imageRes = R.drawable.ic_onboarding_reminders,
            titleRes = R.string.onboarding_title_2,
            descriptionRes = R.string.onboarding_desc_2
        ),
        OnboardingPage(
            imageRes = R.drawable.ic_onboarding_organize,
            titleRes = R.string.onboarding_title_3,
            descriptionRes = R.string.onboarding_desc_3
        )
    )

    // Permission request launcher for notifications (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Continue regardless of permission result
        finishOnboarding()
    }

    // Permission request launcher for camera
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // After camera permission, request notification permission
        requestNotificationPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        preferencesManager = PreferencesManager.getInstance(this)

        initViews()
        setupViewPager()
        setupListeners()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewpager_onboarding)
        btnSkip = findViewById(R.id.btn_skip)
        btnNext = findViewById(R.id.btn_next)
        indicator1 = findViewById(R.id.indicator_1)
        indicator2 = findViewById(R.id.indicator_2)
        indicator3 = findViewById(R.id.indicator_3)
    }

    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter(pages)
        viewPager.adapter = onboardingAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
                updateButtonText(position)
            }
        })
    }

    private fun setupListeners() {
        btnSkip.setOnClickListener {
            requestPermissionsAndFinish()
        }

        btnNext.setOnClickListener {
            if (viewPager.currentItem < pages.size - 1) {
                viewPager.currentItem += 1
            } else {
                requestPermissionsAndFinish()
            }
        }
    }

    private fun updateIndicators(position: Int) {
        val indicators = listOf(indicator1, indicator2, indicator3)
        indicators.forEachIndexed { index, view ->
            view.setBackgroundResource(
                if (index == position) R.drawable.indicator_dot_selected
                else R.drawable.indicator_dot_unselected
            )
        }
    }

    private fun updateButtonText(position: Int) {
        if (position == pages.size - 1) {
            btnNext.text = getString(R.string.onboarding_get_started)
            btnSkip.visibility = View.INVISIBLE
        } else {
            btnNext.text = getString(R.string.onboarding_next)
            btnSkip.visibility = View.VISIBLE
        }
    }

    private fun requestPermissionsAndFinish() {
        // Request camera permission first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            // Camera already granted, check notifications
            requestNotificationPermission()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                finishOnboarding()
            }
        } else {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        // Mark onboarding as completed
        preferencesManager.setOnboardingCompleted(true)

        // Navigate to main activity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
