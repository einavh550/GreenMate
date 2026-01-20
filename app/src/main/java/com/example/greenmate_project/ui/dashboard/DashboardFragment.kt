package com.example.greenmate_project.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.greenmate_project.R
import com.example.greenmate_project.model.ActionType
import com.google.android.material.snackbar.Snackbar

/**
 * Dashboard Fragment - Shows today's care tasks, overdue items, and statistics.
 * Entry point for the app's main functionality.
 */
class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var textGreeting: TextView
    private lateinit var recyclerTodayTasks: RecyclerView
    private lateinit var recyclerOverdueTasks: RecyclerView
    private lateinit var textTodayEmpty: TextView
    private lateinit var textOverdueEmpty: TextView
    private lateinit var textTodayHeader: TextView
    private lateinit var textOverdueHeader: TextView
    private lateinit var textAllDone: TextView
    private lateinit var progressLoading: ProgressBar

    // Statistics views
    private lateinit var textStatPlants: TextView
    private lateinit var textStatStreak: TextView
    private lateinit var textStatCompleted: TextView

    private lateinit var todayAdapter: CareTaskAdapter
    private lateinit var overdueAdapter: CareTaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerViews()
        observeViewModel()
        loadData()
    }

    private fun initViews(view: View) {
        textGreeting = view.findViewById(R.id.text_greeting)
        recyclerTodayTasks = view.findViewById(R.id.recycler_today_tasks)
        recyclerOverdueTasks = view.findViewById(R.id.recycler_overdue_tasks)
        textTodayEmpty = view.findViewById(R.id.text_today_empty)
        textOverdueEmpty = view.findViewById(R.id.text_overdue_empty)
        textTodayHeader = view.findViewById(R.id.text_today_header)
        textOverdueHeader = view.findViewById(R.id.text_overdue_header)
        textAllDone = view.findViewById(R.id.text_all_done)
        progressLoading = view.findViewById(R.id.progress_loading)

        // Statistics views
        textStatPlants = view.findViewById(R.id.text_stat_plants)
        textStatStreak = view.findViewById(R.id.text_stat_streak)
        textStatCompleted = view.findViewById(R.id.text_stat_completed)
    }

    private fun setupRecyclerViews() {
        // Setup today tasks adapter
        todayAdapter = CareTaskAdapter { task ->
            viewModel.completeTask(task)
            showTaskCompletedMessage(task.actionType)
        }
        recyclerTodayTasks.adapter = todayAdapter

        // Setup overdue tasks adapter
        overdueAdapter = CareTaskAdapter { task ->
            viewModel.completeTask(task)
            showTaskCompletedMessage(task.actionType)
        }
        recyclerOverdueTasks.adapter = overdueAdapter
    }

    private fun observeViewModel() {
        viewModel.todayTasks.observe(viewLifecycleOwner) { tasks ->
            todayAdapter.submitList(tasks)
            updateTodayVisibility(tasks.isEmpty())
        }

        viewModel.overdueTasks.observe(viewLifecycleOwner) { tasks ->
            overdueAdapter.submitList(tasks)
            updateOverdueVisibility(tasks.isEmpty())
        }

        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            updateStatistics(stats)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
                viewModel.clearError()
            }
        }
    }

    private fun updateStatistics(stats: CareStats) {
        textStatPlants.text = stats.totalPlants.toString()
        textStatStreak.text = stats.careStreak.toString()
        textStatCompleted.text = stats.tasksCompletedThisWeek.toString()
    }

    private fun loadData() {
        viewModel.loadTasks()
    }

    private fun showLoading(show: Boolean) {
        progressLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateTodayVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            textTodayEmpty.visibility = View.VISIBLE
            recyclerTodayTasks.visibility = View.GONE
        } else {
            textTodayEmpty.visibility = View.GONE
            recyclerTodayTasks.visibility = View.VISIBLE
        }
        updateAllDoneVisibility()
    }

    private fun updateOverdueVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            textOverdueEmpty.visibility = View.VISIBLE
            recyclerOverdueTasks.visibility = View.GONE
        } else {
            textOverdueEmpty.visibility = View.GONE
            recyclerOverdueTasks.visibility = View.VISIBLE
        }
        updateAllDoneVisibility()
    }

    private fun updateAllDoneVisibility() {
        val todayEmpty = viewModel.todayTasks.value?.isEmpty() ?: true
        val overdueEmpty = viewModel.overdueTasks.value?.isEmpty() ?: true

        if (todayEmpty && overdueEmpty) {
            // Show all done message, hide section headers and empty states
            textAllDone.visibility = View.VISIBLE
            textTodayHeader.visibility = View.GONE
            textTodayEmpty.visibility = View.GONE
            recyclerTodayTasks.visibility = View.GONE
            textOverdueHeader.visibility = View.GONE
            textOverdueEmpty.visibility = View.GONE
            recyclerOverdueTasks.visibility = View.GONE
        } else {
            // Show sections, hide all done message
            textAllDone.visibility = View.GONE
            textTodayHeader.visibility = View.VISIBLE
            textOverdueHeader.visibility = View.VISIBLE
        }
    }

    private fun showTaskCompletedMessage(actionType: ActionType) {
        val message = when (actionType) {
            ActionType.WATER -> getString(R.string.msg_plant_watered)
            ActionType.FERTILIZE -> getString(R.string.msg_plant_fertilized)
        }
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showError(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        viewModel.loadTasks()
    }

    companion object {
        fun newInstance() = DashboardFragment()
    }
}
