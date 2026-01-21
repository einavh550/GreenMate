package com.example.greenmate_project.ui.dashboard

import android.content.res.ColorStateList
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
import com.example.greenmate_project.util.ImageUtils
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
        private val imagePlantThumbnail: ImageView = itemView.findViewById(R.id.image_plant_thumbnail)
        private val iconActionType: ImageView = itemView.findViewById(R.id.icon_action_type)
        private val textPlantName: TextView = itemView.findViewById(R.id.text_plant_name)
        private val textTaskDescription: TextView = itemView.findViewById(R.id.text_task_description)
        private val btnAction: MaterialButton = itemView.findViewById(R.id.btn_action)

        fun bind(task: CareTask) {
            val context = itemView.context

            // Set plant name
            textPlantName.text = task.plant.name

            // Load plant thumbnail image
            val bitmap = ImageUtils.loadImage(task.plant.photoUrl)
            if (bitmap != null) {
                imagePlantThumbnail.setImageBitmap(bitmap)
                imagePlantThumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
                imagePlantThumbnail.setPadding(0, 0, 0, 0)
                imagePlantThumbnail.colorFilter = null
            } else {
                // Show placeholder icon
                imagePlantThumbnail.setImageResource(R.drawable.ic_leaf)
                imagePlantThumbnail.scaleType = ImageView.ScaleType.CENTER
                val padding = context.resources.getDimensionPixelSize(R.dimen.spacing_sm)
                imagePlantThumbnail.setPadding(padding, padding, padding, padding)
                imagePlantThumbnail.setColorFilter(ContextCompat.getColor(context, R.color.primary))
            }

            // Set action type icon, description, and button style
            when (task.actionType) {
                ActionType.WATER -> {
                    iconActionType.setImageResource(R.drawable.ic_water)
                    iconActionType.setColorFilter(
                        ContextCompat.getColor(context, R.color.button_water_background)
                    )
                    textTaskDescription.text = task.dueDescription
                    btnAction.text = context.getString(R.string.action_water)
                    // Green Water button
                    btnAction.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.button_water_background)
                    )
                    btnAction.setTextColor(ContextCompat.getColor(context, R.color.button_water_text))
                }
                ActionType.FERTILIZE -> {
                    iconActionType.setImageResource(R.drawable.ic_fertilizer)
                    iconActionType.setColorFilter(
                        ContextCompat.getColor(context, R.color.button_fertilize_background)
                    )
                    textTaskDescription.text = task.dueDescription
                    btnAction.text = context.getString(R.string.action_fertilize)
                    // Brown Fertilize button
                    btnAction.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.button_fertilize_background)
                    )
                    btnAction.setTextColor(ContextCompat.getColor(context, R.color.button_fertilize_text))
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
