package com.example.greenmate_project.ui.plants

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
import com.example.greenmate_project.model.Plant
import com.example.greenmate_project.model.PlantStatus
import com.example.greenmate_project.util.ImageUtils
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

/**
 * Adapter for displaying plants in the My Plants RecyclerView.
 */
class PlantAdapter(
    private val onPlantClick: (Plant) -> Unit
) : ListAdapter<Plant, PlantAdapter.PlantViewHolder>(PlantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagePlant: ImageView = itemView.findViewById(R.id.image_plant)
        private val textPlantName: TextView = itemView.findViewById(R.id.text_plant_name)
        private val textLocation: TextView = itemView.findViewById(R.id.text_location)
        private val textNextCare: TextView = itemView.findViewById(R.id.text_next_care)
        private val textStatus: TextView = itemView.findViewById(R.id.text_status)

        fun bind(plant: Plant) {
            val context = itemView.context

            // Set plant name
            textPlantName.text = plant.name

            // Set location
            textLocation.text = plant.location.ifEmpty {
                context.getString(R.string.hint_location)
            }

            // Calculate and display next care info
            val (nextCareText, status) = calculateNextCare(plant)
            textNextCare.text = nextCareText

            // Set status badge
            when (status) {
                PlantStatus.HEALTHY -> {
                    textStatus.text = context.getString(R.string.plant_status_ok)
                    textStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.score_green)
                }
                PlantStatus.NEEDS_ATTENTION -> {
                    textStatus.text = context.getString(R.string.plant_status_needs_attention)
                    textStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.score_yellow)
                }
                PlantStatus.OVERDUE -> {
                    textStatus.text = context.getString(R.string.plant_status_overdue)
                    textStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.score_red)
                }
            }

            // Set status text color for contrast
            textStatus.setTextColor(ContextCompat.getColor(context, R.color.white))

            // Set plant image or default icon
            val bitmap = ImageUtils.loadImage(plant.photoUrl)
            if (bitmap != null) {
                imagePlant.setImageBitmap(bitmap)
                imagePlant.colorFilter = null
                imagePlant.scaleType = ImageView.ScaleType.CENTER_CROP
            } else {
                imagePlant.setImageResource(R.drawable.ic_leaf)
                imagePlant.setColorFilter(ContextCompat.getColor(context, R.color.primary))
                imagePlant.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }

            // Handle item click
            itemView.setOnClickListener {
                onPlantClick(plant)
            }
        }

        private fun calculateNextCare(plant: Plant): Pair<String, PlantStatus> {
            val context = itemView.context
            val now = Timestamp.now().seconds

            // Calculate days until water needed
            val waterDaysRemaining = plant.lastWateredAt?.let { lastWatered ->
                val daysSinceWater = TimeUnit.SECONDS.toDays(now - lastWatered.seconds).toInt()
                plant.waterIntervalDays - daysSinceWater
            } ?: 0 // If never watered, needs water now

            // Calculate days until fertilize needed
            val fertilizeDaysRemaining = plant.lastFertilizedAt?.let { lastFertilized ->
                val daysSinceFertilize = TimeUnit.SECONDS.toDays(now - lastFertilized.seconds).toInt()
                plant.fertilizeIntervalDays - daysSinceFertilize
            } ?: 0 // If never fertilized, needs fertilizer now

            // Determine the most urgent care need
            val (nextCareText, daysRemaining) = if (waterDaysRemaining <= fertilizeDaysRemaining) {
                when {
                    waterDaysRemaining < 0 -> context.getString(R.string.status_days_overdue, -waterDaysRemaining) + " (water)" to waterDaysRemaining
                    waterDaysRemaining == 0 -> context.getString(R.string.status_due_today) + " (water)" to waterDaysRemaining
                    else -> context.getString(R.string.status_due_in_days, waterDaysRemaining) + " (water)" to waterDaysRemaining
                }
            } else {
                when {
                    fertilizeDaysRemaining < 0 -> context.getString(R.string.status_days_overdue, -fertilizeDaysRemaining) + " (fertilize)" to fertilizeDaysRemaining
                    fertilizeDaysRemaining == 0 -> context.getString(R.string.status_due_today) + " (fertilize)" to fertilizeDaysRemaining
                    else -> context.getString(R.string.status_due_in_days, fertilizeDaysRemaining) + " (fertilize)" to fertilizeDaysRemaining
                }
            }

            // Determine overall status based on most urgent need
            val minDays = minOf(waterDaysRemaining, fertilizeDaysRemaining)
            val status = when {
                minDays < 0 -> PlantStatus.OVERDUE
                minDays == 0 -> PlantStatus.NEEDS_ATTENTION
                else -> PlantStatus.HEALTHY
            }

            return nextCareText to status
        }
    }

    private class PlantDiffCallback : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean {
            return oldItem == newItem
        }
    }
}
