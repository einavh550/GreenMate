package com.example.greenmate_project.data

import com.example.greenmate_project.model.Plant
import com.example.greenmate_project.service.FirebaseAuthService
import com.example.greenmate_project.service.FirestoreService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query

/**
 * Firestore implementation of PlantRepository.
 * Handles all plant CRUD operations for the current user.
 */
class PlantRepositoryImpl : PlantRepository {

    override fun getAllPlants(
        onSuccess: (List<Plant>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val collection = FirestoreService.currentUserPlantsCollection()
        if (collection == null) {
            onError(IllegalStateException("User not signed in"))
            return
        }

        collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val plants = snapshot.toObjects(Plant::class.java)
                onSuccess(plants)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    override fun getPlant(
        plantId: String,
        onSuccess: (Plant?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuthService.currentUserId
        if (uid == null) {
            onError(IllegalStateException("User not signed in"))
            return
        }

        FirestoreService.plantDocument(uid, plantId)
            .get()
            .addOnSuccessListener { snapshot ->
                val plant = snapshot.toObject(Plant::class.java)
                onSuccess(plant)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    override fun addPlant(
        plant: Plant,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val collection = FirestoreService.currentUserPlantsCollection()
        if (collection == null) {
            onError(IllegalStateException("User not signed in"))
            return
        }

        collection
            .add(plant)
            .addOnSuccessListener { docRef ->
                onSuccess(docRef.id)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    override fun updatePlant(
        plant: Plant,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuthService.currentUserId
        if (uid == null) {
            onError(IllegalStateException("User not signed in"))
            return
        }

        FirestoreService.plantDocument(uid, plant.id)
            .set(plant)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    override fun deletePlant(
        plantId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuthService.currentUserId
        if (uid == null) {
            onError(IllegalStateException("User not signed in"))
            return
        }

        FirestoreService.plantDocument(uid, plantId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    override fun markWatered(
        plantId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuthService.currentUserId
        if (uid == null) {
            onError(IllegalStateException("User not signed in"))
            return
        }

        FirestoreService.plantDocument(uid, plantId)
            .update("lastWateredAt", Timestamp.now())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    override fun markFertilized(
        plantId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuthService.currentUserId
        if (uid == null) {
            onError(IllegalStateException("User not signed in"))
            return
        }

        FirestoreService.plantDocument(uid, plantId)
            .update("lastFertilizedAt", Timestamp.now())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
}
