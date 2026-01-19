package com.example.greenmate_project.data

import com.example.greenmate_project.model.CareAction
import com.example.greenmate_project.service.FirebaseAuthService
import com.example.greenmate_project.service.FirestoreService
import com.google.firebase.firestore.Query

/**
 * Firestore implementation of ActionRepository.
 * Handles care history operations for plants.
 */
class ActionRepositoryImpl : ActionRepository {

    override fun getActionsForPlant(
        plantId: String,
        onSuccess: (List<CareAction>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuthService.currentUserId
        if (uid == null) {
            onError(IllegalStateException("User not signed in"))
            return
        }

        FirestoreService.actionsCollection(uid, plantId)
            .orderBy("performedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val actions = snapshot.toObjects(CareAction::class.java)
                onSuccess(actions)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    override fun getRecentActions(
        plantId: String,
        limit: Int,
        onSuccess: (List<CareAction>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuthService.currentUserId
        if (uid == null) {
            onError(IllegalStateException("User not signed in"))
            return
        }

        FirestoreService.actionsCollection(uid, plantId)
            .orderBy("performedAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .addOnSuccessListener { snapshot ->
                val actions = snapshot.toObjects(CareAction::class.java)
                onSuccess(actions)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    override fun addAction(
        plantId: String,
        action: CareAction,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuthService.currentUserId
        if (uid == null) {
            onError(IllegalStateException("User not signed in"))
            return
        }

        FirestoreService.actionsCollection(uid, plantId)
            .add(action)
            .addOnSuccessListener { docRef ->
                onSuccess(docRef.id)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    override fun deleteAction(
        plantId: String,
        actionId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuthService.currentUserId
        if (uid == null) {
            onError(IllegalStateException("User not signed in"))
            return
        }

        FirestoreService.actionDocument(uid, plantId, actionId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
}
