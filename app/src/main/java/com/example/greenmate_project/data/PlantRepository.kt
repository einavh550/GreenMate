package com.example.greenmate_project.data

import com.example.greenmate_project.model.Plant

/**
 * Repository interface for Plant CRUD operations.
 * Implementations handle Firestore communication.
 */
interface PlantRepository {

    /**
     * Gets all plants for the current user.
     * @param onSuccess Called with list of plants on success
     * @param onError Called with exception on failure
     */
    fun getAllPlants(
        onSuccess: (List<Plant>) -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Gets a single plant by ID.
     * @param plantId The plant document ID
     * @param onSuccess Called with plant on success (null if not found)
     * @param onError Called with exception on failure
     */
    fun getPlant(
        plantId: String,
        onSuccess: (Plant?) -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Adds a new plant to the user's collection.
     * @param plant The plant to add (ID will be auto-generated)
     * @param onSuccess Called with the new plant ID on success
     * @param onError Called with exception on failure
     */
    fun addPlant(
        plant: Plant,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Updates an existing plant.
     * @param plant The plant with updated fields
     * @param onSuccess Called on success
     * @param onError Called with exception on failure
     */
    fun updatePlant(
        plant: Plant,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Deletes a plant and all its care history.
     * @param plantId The plant document ID to delete
     * @param onSuccess Called on success
     * @param onError Called with exception on failure
     */
    fun deletePlant(
        plantId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Updates the last watered timestamp for a plant.
     * @param plantId The plant document ID
     * @param onSuccess Called on success
     * @param onError Called with exception on failure
     */
    fun markWatered(
        plantId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Updates the last fertilized timestamp for a plant.
     * @param plantId The plant document ID
     * @param onSuccess Called on success
     * @param onError Called with exception on failure
     */
    fun markFertilized(
        plantId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )
}
