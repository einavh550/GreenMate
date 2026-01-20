package com.example.greenmate_project.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenmate_project.R
import com.example.greenmate_project.model.ActionType
import com.example.greenmate_project.model.CareTask
import com.example.greenmate_project.model.PlantStatus
import com.google.android.material.button.MaterialButton

/**
 * Adapter for displaying care tasks in the Dashboard RecyclerViews.
 * Handles both "Today's Tasks" and "Overdue Tasks" lists.
 */
class CareTaskAdapter(
    private val onTaskComplete: (CareTask) -> Unit
) : ListAdapter<CareTask, CareTaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_care_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconActionType: ImageView = itemView.findViewById(R.id.icon_action_type)
        private val textPlantName: TextView = itemView.findViewById(R.id.text_plant_name)
        private val textTaskDescription: TextView = itemView.findViewById(R.id.text_task_description)
        private val btnAction: MaterialButton = itemView.findViewById(R.id.btn_action)

        fun bind(task: CareTask) {
            val context = itemView.context

            // Set plant name
            textPlantName.text = task.plant.name

            // Set action type icon and description
            when (task.actionType) {
                ActionType.WATER -> {
                    iconActionType.setImageResource(R.drawable.ic_water)
                    iconActionType.setColorFilter(
                        ContextCompat.getColor(context, R.color.primary_light)
                    )
                    textTaskDescription.text = task.dueDescription
                    btnAction.text = context.getString(R.string.action_water)
                }
                ActionType.FERTILIZE -> {
                    iconActionType.setImageResource(R.drawable.ic_fertilizer)
                    iconActionType.setColorFilter(
                        ContextCompat.getColor(context, R.color.earth_light)
                    )
                    textTaskDescription.text = task.dueDescription
                    btnAction.text = context.getString(R.string.action_fertilize)
                }
            }

            // Set description color based on status
            val statusColor = when (task.status) {
                PlantStatus.OVERDUE -> R.color.score_red
                PlantStatus.NEEDS_ATTENTION -> R.color.score_yellow
                PlantStatus.HEALTHY -> R.color.text_secondary
            }
            textTaskDescription.setTextColor(ContextCompat.getColor(context, statusColor))

            // Handle action button click
            btnAction.setOnClickListener {
                onTaskComplete(task)
            }
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<CareTask>() {
        override fun areItemsTheSame(oldItem: CareTask, newItem: CareTask): Boolean {
            // Unique by plant ID + action type
            return oldItem.plant.id == newItem.plant.id &&
                   oldItem.actionType == newItem.actionType
        }

        override fun areContentsTheSame(oldItem: CareTask, newItem: CareTask): Boolean {
            return oldItem == newItem
        }
    }
}
