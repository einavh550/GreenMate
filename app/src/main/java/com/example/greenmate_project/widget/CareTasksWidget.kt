package com.example.greenmate_project.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.greenmate_project.MainActivity
import com.example.greenmate_project.R
import com.example.greenmate_project.data.PlantRepositoryImpl
import com.example.greenmate_project.model.ActionType
import com.example.greenmate_project.model.Plant
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

/**
 * Home screen widget that displays today's plant care tasks.
 * Shows up to 4 tasks with an indicator for more.
 */
class CareTasksWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget instance
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // First widget added - could initialize resources here
    }

    override fun onDisabled(context: Context) {
        // Last widget removed - could clean up resources here
    }

    companion object {
        private const val ACTION_REFRESH = "com.example.greenmate_project.WIDGET_REFRESH"

        /**
         * Updates all widget instances.
         * Call this when plant data changes.
         */
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, CareTasksWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = widgetManager.getAppWidgetIds(
                ComponentName(context, CareTasksWidget::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            context.sendBroadcast(intent)
        }

        /**
         * Updates a single widget instance.
         */
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Create RemoteViews
            val views = RemoteViews(context.packageName, R.layout.widget_care_tasks)

            // Set click listener to open app
            val openAppIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            // Load tasks and update widget
            loadTasksAndUpdateWidget(context, appWidgetManager, appWidgetId, views)
        }

        private fun loadTasksAndUpdateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            views: RemoteViews
        ) {
            val plantRepository = PlantRepositoryImpl()

            plantRepository.getAllPlants(
                onSuccess = { plants ->
                    val tasks = calculateTodaysTasks(plants)
                    updateWidgetWithTasks(context, appWidgetManager, appWidgetId, views, tasks)
                },
                onError = { _ ->
                    // Show empty state on error
                    updateWidgetWithTasks(context, appWidgetManager, appWidgetId, views, emptyList())
                }
            )
        }

        private fun calculateTodaysTasks(plants: List<Plant>): List<CareTask> {
            val tasks = mutableListOf<CareTask>()
            val now = Timestamp.now().seconds

            for (plant in plants) {
                // Check water needed
                val waterDaysRemaining = plant.lastWateredAt?.let { lastWatered ->
                    val daysSinceWater = TimeUnit.SECONDS.toDays(now - lastWatered.seconds).toInt()
                    plant.waterIntervalDays - daysSinceWater
                } ?: 0

                if (waterDaysRemaining <= 0) {
                    tasks.add(CareTask(
                        plantName = plant.name,
                        actionType = ActionType.WATER,
                        isOverdue = waterDaysRemaining < 0
                    ))
                }

                // Check fertilize needed
                val fertilizeDaysRemaining = plant.lastFertilizedAt?.let { lastFertilized ->
                    val daysSinceFertilize = TimeUnit.SECONDS.toDays(now - lastFertilized.seconds).toInt()
                    plant.fertilizeIntervalDays - daysSinceFertilize
                } ?: 0

                if (fertilizeDaysRemaining <= 0) {
                    tasks.add(CareTask(
                        plantName = plant.name,
                        actionType = ActionType.FERTILIZE,
                        isOverdue = fertilizeDaysRemaining < 0
                    ))
                }
            }

            // Sort: overdue first, then by plant name
            return tasks.sortedWith(compareBy({ !it.isOverdue }, { it.plantName }))
        }

        private fun updateWidgetWithTasks(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            views: RemoteViews,
            tasks: List<CareTask>
        ) {
            // Update task count
            val taskCountText = when (tasks.size) {
                0 -> context.getString(R.string.widget_no_tasks)
                1 -> context.getString(R.string.widget_one_task)
                else -> context.getString(R.string.widget_task_count, tasks.size)
            }
            views.setTextViewText(R.id.text_task_count, taskCountText)

            // Task layout IDs and text IDs
            val taskLayouts = listOf(
                R.id.layout_task_1,
                R.id.layout_task_2,
                R.id.layout_task_3,
                R.id.layout_task_4
            )
            val taskTexts = listOf(
                R.id.text_task_1,
                R.id.text_task_2,
                R.id.text_task_3,
                R.id.text_task_4
            )
            val taskIcons = listOf(
                R.id.icon_task_1,
                R.id.icon_task_2,
                R.id.icon_task_3,
                R.id.icon_task_4
            )

            // Show empty state if no tasks
            if (tasks.isEmpty()) {
                views.setViewVisibility(R.id.text_empty, View.VISIBLE)
                taskLayouts.forEach { views.setViewVisibility(it, View.GONE) }
                views.setViewVisibility(R.id.text_more_tasks, View.GONE)
            } else {
                views.setViewVisibility(R.id.text_empty, View.GONE)

                // Show up to 4 tasks
                val displayTasks = tasks.take(4)
                for (i in taskLayouts.indices) {
                    if (i < displayTasks.size) {
                        val task = displayTasks[i]
                        views.setViewVisibility(taskLayouts[i], View.VISIBLE)

                        // Set task text
                        val actionText = when (task.actionType) {
                            ActionType.WATER -> context.getString(R.string.action_water)
                            ActionType.FERTILIZE -> context.getString(R.string.action_fertilize)
                        }
                        views.setTextViewText(taskTexts[i], "$actionText ${task.plantName}")

                        // Set icon based on action type
                        val iconRes = when (task.actionType) {
                            ActionType.WATER -> R.drawable.ic_water
                            ActionType.FERTILIZE -> R.drawable.ic_fertilizer
                        }
                        views.setImageViewResource(taskIcons[i], iconRes)
                    } else {
                        views.setViewVisibility(taskLayouts[i], View.GONE)
                    }
                }

                // Show "more tasks" indicator if needed
                if (tasks.size > 4) {
                    val moreCount = tasks.size - 4
                    views.setTextViewText(
                        R.id.text_more_tasks,
                        context.getString(R.string.widget_more_tasks, moreCount)
                    )
                    views.setViewVisibility(R.id.text_more_tasks, View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.text_more_tasks, View.GONE)
                }
            }

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    /**
     * Data class representing a care task for the widget.
     */
    data class CareTask(
        val plantName: String,
        val actionType: ActionType,
        val isOverdue: Boolean
    )
}
