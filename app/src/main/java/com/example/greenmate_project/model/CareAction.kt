package com.example.greenmate_project.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Represents a single care action (watering or fertilizing) performed on a plant.
 *
 * Firestore path: users/{uid}/plants/{plantId}/actions/{actionId}
 */
data class CareAction(
    /** Firestore document ID */
    @DocumentId
    val id: String = "",

    /** Type of action performed */
    val type: ActionType = ActionType.WATER,

    /** Optional note about the action (e.g., "Used liquid fertilizer") */
    val note: String? = null,

    /** When the action was performed */
    @ServerTimestamp
    val performedAt: Timestamp? = null
)
