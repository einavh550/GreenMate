package com.example.greenmate_project

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * Application class for GreenMate.
 * Initializes Firebase when the app starts.
 *
 * Note: This is an academic project using Firebase Free tier (Spark plan).
 */
class GreenMateApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        // This is automatically done by the google-services plugin,
        // but we call it explicitly for clarity
        FirebaseApp.initializeApp(this)
    }
}
