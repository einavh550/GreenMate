package com.example.greenmate_project.service

import com.example.greenmate_project.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference

/**
 * Service class for Firestore database operations.
 * Provides easy access to collections and documents for the gardening app.
 *
 * Firestore Structure:
 *   users/{uid}                           - User settings
 *   users/{uid}/plants/{plantId}          - Plant profiles
 *   users/{uid}/plants/{plantId}/actions/{actionId} - Care history
 */
object FirestoreService {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // ==================== USER COLLECTION ====================

    /**
     * Returns reference to the users collection.
     */
    fun usersCollection(): CollectionReference {
        return db.collection(Constants.Firestore.COLLECTION_USERS)
    }

    /**
     * Returns reference to a specific user's document.
     */
    fun userDocument(uid: String): DocumentReference {
        return usersCollection().document(uid)
    }

    /**
     * Returns reference to current user's document.
     * Returns null if no user is signed in.
     */
    fun currentUserDocument(): DocumentReference? {
        val uid = FirebaseAuthService.currentUserId ?: return null
        return userDocument(uid)
    }

    // ==================== PLANTS COLLECTION ====================

    /**
     * Returns reference to a user's plants subcollection.
     */
    fun plantsCollection(uid: String): CollectionReference {
        return userDocument(uid).collection(Constants.Firestore.COLLECTION_PLANTS)
    }

    /**
     * Returns reference to current user's plants collection.
     * Returns null if no user is signed in.
     */
    fun currentUserPlantsCollection(): CollectionReference? {
        val uid = FirebaseAuthService.currentUserId ?: return null
        return plantsCollection(uid)
    }

    /**
     * Returns reference to a specific plant document.
     */
    fun plantDocument(uid: String, plantId: String): DocumentReference {
        return plantsCollection(uid).document(plantId)
    }

    // ==================== ACTIONS COLLECTION ====================

    /**
     * Returns reference to a plant's actions (care history) subcollection.
     */
    fun actionsCollection(uid: String, plantId: String): CollectionReference {
        return plantDocument(uid, plantId).collection(Constants.Firestore.COLLECTION_ACTIONS)
    }

    /**
     * Returns reference to a specific action document.
     */
    fun actionDocument(uid: String, plantId: String, actionId: String): DocumentReference {
        return actionsCollection(uid, plantId).document(actionId)
    }

    // ==================== HELPER METHODS ====================

    /**
     * Generates a new document ID for the given collection.
     */
    fun generateId(collection: CollectionReference): String {
        return collection.document().id
    }
}
