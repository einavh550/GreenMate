package com.example.greenmate_project.data

import com.example.greenmate_project.model.CareAction

/**
 * Repository interface for CareAction (care history) operations.
 * Implementations handle Firestore communication.
 */
interface ActionRepository {

    /**
     * Gets all care actions for a specific plant.
     * @param plantId The plant document ID
     * @param onSuccess Called with list of actions on success
     * @param onError Called with exception on failure
     */
    fun getActionsForPlant(
        plantId: String,
        onSuccess: (List<CareAction>) -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Gets care actions for a plant performed after a given timestamp.
     * @param plantId The plant document ID
     * @param sinceTimestamp Only return actions performed after this time
     * @param onSuccess Called with list of actions on success
     * @param onError Called with exception on failure
     */
    fun getRecentActions(
        plantId: String,
        sinceTimestamp: com.google.firebase.Timestamp,
        onSuccess: (List<CareAction>) -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Adds a new care action to a plant's history.
     * @param plantId The plant document ID
     * @param action The action to add (ID will be auto-generated)
     * @param onSuccess Called with the new action ID on success
     * @param onError Called with exception on failure
     */
    fun addAction(
        plantId: String,
        action: CareAction,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Deletes a care action from history.
     * @param plantId The plant document ID
     * @param actionId The action document ID to delete
     * @param onSuccess Called on success
     * @param onError Called with exception on failure
     */
    fun deleteAction(
        plantId: String,
        actionId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Deletes all care actions for a plant (used when deleting a plant).
     * @param plantId The plant document ID
     * @param onSuccess Called on success
     * @param onError Called with exception on failure
     */
    fun deleteAllActionsForPlant(
        plantId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )
}
