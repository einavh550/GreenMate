package com.example.greenmate_project

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.greenmate_project.service.FirebaseAuthService
import com.example.greenmate_project.ui.dashboard.DashboardFragment
import com.example.greenmate_project.ui.plants.MyPlantsFragment
import com.example.greenmate_project.ui.settings.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Main Activity - hosts the bottom navigation and fragments.
 * Single-activity architecture entry point.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupWindowInsets()
        initViews()
        setupBottomNavigation()
        ensureUserSignedIn()

        // Restore selected tab or show dashboard by default
        if (savedInstanceState != null) {
            val selectedTabId = savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.nav_dashboard)
            bottomNavigation.selectedItemId = selectedTabId
        } else {
            replaceFragment(DashboardFragment.newInstance())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_TAB, bottomNavigation.selectedItemId)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_dashboard -> DashboardFragment.newInstance()
                R.id.nav_my_plants -> MyPlantsFragment.newInstance()
                R.id.nav_settings -> SettingsFragment.newInstance()
                else -> return@setOnItemSelectedListener false
            }
            replaceFragment(fragment)
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun ensureUserSignedIn() {
        FirebaseAuthService.ensureSignedIn(
            onReady = { uid ->
                Log.d(TAG, "User signed in: $uid")
            },
            onError = { e ->
                Log.e(TAG, "Auth error: ${e.message}")
            }
        )
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val KEY_SELECTED_TAB = "selected_tab"
    }
}
