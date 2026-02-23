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
 * Supports both list and grid layouts.
 */
class PlantAdapter(
    private val onPlantClick: (Plant) -> Unit
) : ListAdapter<Plant, PlantAdapter.PlantViewHolder>(PlantDiffCallback()) {

    private var isGridLayout: Boolean = true

    /**
     * Switches between grid and list layout modes.
     * Requires full rebind since ViewHolder layout changes.
     */
    @Suppress("unused", "NotifyDataSetChanged")
    fun setGridLayout(isGrid: Boolean) {
        if (isGridLayout != isGrid) {
            isGridLayout = isGrid
            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGridLayout) VIEW_TYPE_GRID else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val layoutRes = if (viewType == VIEW_TYPE_GRID) {
            R.layout.item_plant_grid
        } else {
            R.layout.item_plant
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return PlantViewHolder(view, viewType == VIEW_TYPE_GRID)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlantViewHolder(
        itemView: View,
        private val isGrid: Boolean
    ) : RecyclerView.ViewHolder(itemView) {

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
            val locationText = plant.location.ifEmpty {
                context.getString(R.string.hint_location)
            }
            textLocation.text = locationText

            // Calculate and display next care info
            val (nextCareText, status) = calculateNextCare(plant)
            textNextCare.text = nextCareText

            // Set status badge with premium styling based on plant status
            when (status) {
                PlantStatus.HEALTHY -> {
                    textStatus.text = context.getString(R.string.plant_status_ok)
                    textStatus.setBackgroundResource(R.drawable.bg_badge_healthy)
                    textStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_healthy))
                }
                PlantStatus.NEEDS_ATTENTION -> {
                    textStatus.text = context.getString(R.string.plant_status_needs_attention)
                    textStatus.setBackgroundResource(R.drawable.bg_badge_attention)
                    textStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_attention))
                }
                PlantStatus.OVERDUE -> {
                    textStatus.text = context.getString(R.string.plant_status_overdue)
                    textStatus.setBackgroundResource(R.drawable.bg_badge_overdue)
                    textStatus.setTextColor(ContextCompat.getColor(context, R.color.score_red))
                }
            }

            // Set next care text color based on status
            val careTextColor = when (status) {
                PlantStatus.OVERDUE -> ContextCompat.getColor(context, R.color.score_red)
                PlantStatus.NEEDS_ATTENTION -> ContextCompat.getColor(context, R.color.score_yellow)
                else -> ContextCompat.getColor(context, R.color.score_green)
            }
            textNextCare.setTextColor(careTextColor)

            // Handle grid image sizing for 1:1 aspect ratio
            if (isGrid) {
                setupGridImage(plant)
            } else {
                setupListImage(plant)
            }

            // Handle item click
            itemView.setOnClickListener {
                onPlantClick(plant)
            }
        }

        private fun setupGridImage(plant: Plant) {
            val context = itemView.context

            // Load plant image or show placeholder
            // Using ConstraintLayout dimensionRatio for 1:1 square aspect ratio
            val bitmap = ImageUtils.loadImage(plant.photoUrl)
            if (bitmap != null) {
                imagePlant.setImageBitmap(bitmap)
                imagePlant.scaleType = ImageView.ScaleType.CENTER_CROP
                imagePlant.setPadding(0, 0, 0, 0)
                imagePlant.colorFilter = null
            } else {
                imagePlant.setImageResource(R.drawable.ic_leaf)
                imagePlant.scaleType = ImageView.ScaleType.CENTER
                val padding = context.resources.getDimensionPixelSize(R.dimen.spacing_lg)
                imagePlant.setPadding(padding, padding, padding, padding)
                imagePlant.setColorFilter(ContextCompat.getColor(context, R.color.primary))
            }
        }

        private fun setupListImage(plant: Plant) {
            val context = itemView.context
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
            val (nextCareText, _) = if (waterDaysRemaining <= fertilizeDaysRemaining) {
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

    companion object {
        private const val VIEW_TYPE_LIST = 0
        private const val VIEW_TYPE_GRID = 1
    }
}
