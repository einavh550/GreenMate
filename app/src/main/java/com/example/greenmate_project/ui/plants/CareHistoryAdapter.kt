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
import com.example.greenmate_project.model.ActionType
import com.example.greenmate_project.model.CareAction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying care history in the Plant Details screen.
 */
class CareHistoryAdapter : ListAdapter<CareAction, CareHistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_care_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val indicator: View = itemView.findViewById(R.id.indicator)
        private val iconAction: ImageView = itemView.findViewById(R.id.icon_action)
        private val textAction: TextView = itemView.findViewById(R.id.text_action)
        private val textDate: TextView = itemView.findViewById(R.id.text_date)

        fun bind(action: CareAction) {
            val context = itemView.context

            when (action.type) {
                ActionType.WATER -> {
                    iconAction.setImageResource(R.drawable.ic_water)
                    iconAction.setColorFilter(ContextCompat.getColor(context, R.color.primary_light))
                    indicator.backgroundTintList = ContextCompat.getColorStateList(context, R.color.primary_light)
                    textAction.text = context.getString(R.string.action_watered)
                }
                ActionType.FERTILIZE -> {
                    iconAction.setImageResource(R.drawable.ic_fertilizer)
                    iconAction.setColorFilter(ContextCompat.getColor(context, R.color.earth_light))
                    indicator.backgroundTintList = ContextCompat.getColorStateList(context, R.color.earth_light)
                    textAction.text = context.getString(R.string.action_fertilized)
                }
            }

            // Format date
            action.performedAt?.let { timestamp ->
                textDate.text = formatDate(timestamp.toDate())
            } ?: run {
                textDate.text = ""
            }
        }

        private fun formatDate(date: Date): String {
            val now = Calendar.getInstance()
            val actionDate = Calendar.getInstance().apply { time = date }

            return when {
                isSameDay(now, actionDate) -> {
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    "Today, ${timeFormat.format(date)}"
                }
                isYesterday(now, actionDate) -> {
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    "Yesterday, ${timeFormat.format(date)}"
                }
                isSameYear(now, actionDate) -> {
                    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                    dateFormat.format(date)
                }
                else -> {
                    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    dateFormat.format(date)
                }
            }
        }

        private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }

        private fun isYesterday(today: Calendar, date: Calendar): Boolean {
            val yesterday = Calendar.getInstance().apply {
                time = today.time
                add(Calendar.DAY_OF_YEAR, -1)
            }
            return isSameDay(yesterday, date)
        }

        private fun isSameYear(cal1: Calendar, cal2: Calendar): Boolean {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
        }
    }

    private class HistoryDiffCallback : DiffUtil.ItemCallback<CareAction>() {
        override fun areItemsTheSame(oldItem: CareAction, newItem: CareAction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CareAction, newItem: CareAction): Boolean {
            return oldItem == newItem
        }
    }
}
