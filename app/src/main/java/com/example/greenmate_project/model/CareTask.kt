package com.example.greenmate_project.model

/**
 * Represents a pending care task shown on the dashboard.
 * This is a runtime object (not stored in Firestore) computed from Plant data.
 */
data class CareTask(
    /** The plant that needs care */
    val plant: Plant,

    /** Type of care needed */
    val actionType: ActionType,

    /** Days until care is due (negative = overdue) */
    val daysUntilDue: Int,

    /** Current status based on schedule */
    val status: PlantStatus
) {
    /** True if this task is overdue */
    val isOverdue: Boolean
        get() = daysUntilDue < 0

    /** True if this task is due today */
    val isDueToday: Boolean
        get() = daysUntilDue == 0

    /** Human-readable description of when task is due */
    val dueDescription: String
        get() = when {
            daysUntilDue < -1 -> "${-daysUntilDue} days overdue"
            daysUntilDue == -1 -> "1 day overdue"
            daysUntilDue == 0 -> "Due today"
            daysUntilDue == 1 -> "Due tomorrow"
            else -> "Due in $daysUntilDue days"
        }
}
