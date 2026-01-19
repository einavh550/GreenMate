package com.example.greenmate_project.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Service class for Firebase Authentication.
 * Uses Anonymous Authentication for simplicity (academic project).
 *
 * Anonymous auth is free and doesn't require user to create an account.
 * Each device gets a unique user ID that persists until app data is cleared.
 */
object FirebaseAuthService {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    /**
     * Returns the current authenticated user, or null if not signed in.
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Returns the current user's UID, or null if not signed in.
     */
    val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Checks if a user is currently signed in.
     */
    val isSignedIn: Boolean
        get() = auth.currentUser != null

    /**
     * Signs in anonymously. Creates a new anonymous account if not already signed in.
     * This is the simplest auth method - no email/password required.
     *
     * @param onSuccess Called when sign-in succeeds with the user ID
     * @param onError Called when sign-in fails with the exception
     */
    fun signInAnonymously(
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // If already signed in, return existing user ID
        currentUserId?.let { uid ->
            onSuccess(uid)
            return
        }

        // Sign in anonymously
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    onSuccess(uid)
                } else {
                    onError(Exception("Sign-in succeeded but user ID is null"))
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    /**
     * Signs out the current user.
     * Note: For anonymous users, signing out will lose access to their data
     * since a new anonymous account will be created on next sign-in.
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Ensures a user is signed in before performing an action.
     * If not signed in, signs in anonymously first.
     *
     * @param onReady Called when user is signed in with the user ID
     * @param onError Called if sign-in fails
     */
    fun ensureSignedIn(
        onReady: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (isSignedIn) {
            currentUserId?.let { onReady(it) }
                ?: onError(Exception("User is signed in but UID is null"))
        } else {
            signInAnonymously(onReady, onError)
        }
    }
}
