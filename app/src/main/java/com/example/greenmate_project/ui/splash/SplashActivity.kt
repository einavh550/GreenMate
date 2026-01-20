package com.example.greenmate_project.ui.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.greenmate_project.MainActivity
import com.example.greenmate_project.R
import com.example.greenmate_project.service.FirebaseAuthService
import com.example.greenmate_project.ui.onboarding.OnboardingActivity
import com.example.greenmate_project.util.PreferencesManager

/**
 * Splash screen activity with animated logo and app branding.
 * Handles Firebase authentication before navigating to MainActivity.
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var imageLogo: ImageView
    private lateinit var textAppName: TextView
    private lateinit var textTagline: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make fullscreen with no status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContentView(R.layout.activity_splash)

        initViews()
        startAnimations()
        initializeApp()
    }

    private fun initViews() {
        imageLogo = findViewById(R.id.image_logo)
        textAppName = findViewById(R.id.text_app_name)
        textTagline = findViewById(R.id.text_tagline)

        // Set initial state for animations
        imageLogo.alpha = 0f
        imageLogo.scaleX = 0.5f
        imageLogo.scaleY = 0.5f
        textAppName.alpha = 0f
        textAppName.translationY = 30f
        textTagline.alpha = 0f
        textTagline.translationY = 20f
    }

    private fun startAnimations() {
        // Logo animation - scale up and fade in with bounce
        val logoScaleX = ObjectAnimator.ofFloat(imageLogo, View.SCALE_X, 0.5f, 1f).apply {
            duration = 800
            interpolator = OvershootInterpolator(1.5f)
        }
        val logoScaleY = ObjectAnimator.ofFloat(imageLogo, View.SCALE_Y, 0.5f, 1f).apply {
            duration = 800
            interpolator = OvershootInterpolator(1.5f)
        }
        val logoAlpha = ObjectAnimator.ofFloat(imageLogo, View.ALPHA, 0f, 1f).apply {
            duration = 600
        }

        // App name animation - fade in and slide up
        val nameAlpha = ObjectAnimator.ofFloat(textAppName, View.ALPHA, 0f, 1f).apply {
            duration = 500
            startDelay = 400
        }
        val nameTranslate = ObjectAnimator.ofFloat(textAppName, View.TRANSLATION_Y, 30f, 0f).apply {
            duration = 500
            startDelay = 400
        }

        // Tagline animation - fade in and slide up
        val taglineAlpha = ObjectAnimator.ofFloat(textTagline, View.ALPHA, 0f, 1f).apply {
            duration = 500
            startDelay = 600
        }
        val taglineTranslate = ObjectAnimator.ofFloat(textTagline, View.TRANSLATION_Y, 20f, 0f).apply {
            duration = 500
            startDelay = 600
        }

        // Play all animations together
        AnimatorSet().apply {
            playTogether(
                logoScaleX, logoScaleY, logoAlpha,
                nameAlpha, nameTranslate,
                taglineAlpha, taglineTranslate
            )
            start()
        }
    }

    private fun initializeApp() {
        // Ensure user is signed in (anonymous auth)
        FirebaseAuthService.ensureSignedIn(
            onReady = {
                // Navigate to main after delay for animation to complete
                navigateToMainAfterDelay()
            },
            onError = {
                // Still navigate even if auth fails (offline mode)
                navigateToMainAfterDelay()
            }
        )
    }

    private fun navigateToMainAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            val preferencesManager = PreferencesManager.getInstance(this)

            val destination = if (preferencesManager.isOnboardingCompleted()) {
                // User has completed onboarding, go to main
                Intent(this, MainActivity::class.java)
            } else {
                // First time user, show onboarding
                Intent(this, OnboardingActivity::class.java)
            }

            startActivity(destination)
            finish()
            // Smooth transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, SPLASH_DURATION)
    }

    companion object {
        private const val SPLASH_DURATION = 2500L // 2.5 seconds
    }
}
