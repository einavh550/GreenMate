package com.example.greenmate_project.model

/**
 * Health/care status of a plant based on care schedule adherence.
 */
enum class PlantStatus {
    /** Plant is well-cared for, all tasks up to date */
    HEALTHY,

    /** Plant needs attention soon (within 1 day) */
    NEEDS_ATTENTION,

    /** Plant is overdue for care */
    OVERDUE
}
