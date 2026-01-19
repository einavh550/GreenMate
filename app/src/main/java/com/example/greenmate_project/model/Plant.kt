package com.example.greenmate_project.model

import com.example.greenmate_project.util.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Represents a plant profile in the user's collection.
 *
 * Firestore path: users/{uid}/plants/{plantId}
 */
data class Plant(
    /** Firestore document ID */
    @DocumentId
    val id: String = "",

    /** User-given name for the plant (e.g., "My Cactus") */
    val name: String = "",

    /** Location where plant is kept (e.g., "Living Room") */
    val location: String = "",

    /** Optional photo URL (for future camera feature) */
    val photoUrl: String? = null,

    /** How often to water, in days */
    val waterIntervalDays: Int = Constants.Defaults.WATER_INTERVAL_DAYS,

    /** How often to fertilize, in days */
    val fertilizeIntervalDays: Int = Constants.Defaults.FERTILIZE_INTERVAL_DAYS,

    /** When the plant was last watered */
    val lastWateredAt: Timestamp? = null,

    /** When the plant was last fertilized */
    val lastFertilizedAt: Timestamp? = null,

    /** When the plant profile was created */
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    /** No-argument constructor required by Firestore */
    constructor() : this(id = "")
}
