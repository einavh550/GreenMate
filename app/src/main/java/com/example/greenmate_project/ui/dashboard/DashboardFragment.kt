package com.example.greenmate_project.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.greenmate_project.R

/**
 * Dashboard Fragment - Shows today's care tasks and overdue items.
 * Entry point for the app's main functionality.
 */
class DashboardFragment : Fragment() {

    private lateinit var textGreeting: TextView
    private lateinit var recyclerTodayTasks: RecyclerView
    private lateinit var recyclerOverdueTasks: RecyclerView
    private lateinit var textTodayEmpty: TextView
    private lateinit var textOverdueEmpty: TextView
    private lateinit var textAllDone: TextView
    private lateinit var progressLoading: ProgressBar

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
        loadData()
    }

    private fun initViews(view: View) {
        textGreeting = view.findViewById(R.id.text_greeting)
        recyclerTodayTasks = view.findViewById(R.id.recycler_today_tasks)
        recyclerOverdueTasks = view.findViewById(R.id.recycler_overdue_tasks)
        textTodayEmpty = view.findViewById(R.id.text_today_empty)
        textOverdueEmpty = view.findViewById(R.id.text_overdue_empty)
        textAllDone = view.findViewById(R.id.text_all_done)
        progressLoading = view.findViewById(R.id.progress_loading)
    }

    private fun setupRecyclerViews() {
        // TODO: Set up adapters in Milestone 5
    }

    private fun loadData() {
        // TODO: Load care tasks from repository in Milestone 5
        // For now, show empty states
        showEmptyState()
    }

    private fun showLoading(show: Boolean) {
        progressLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState() {
        textTodayEmpty.visibility = View.VISIBLE
        textOverdueEmpty.visibility = View.VISIBLE
        recyclerTodayTasks.visibility = View.GONE
        recyclerOverdueTasks.visibility = View.GONE
    }

    private fun showAllDone() {
        textAllDone.visibility = View.VISIBLE
        textTodayEmpty.visibility = View.GONE
        textOverdueEmpty.visibility = View.GONE
        recyclerTodayTasks.visibility = View.GONE
        recyclerOverdueTasks.visibility = View.GONE
    }

    companion object {
        fun newInstance() = DashboardFragment()
    }
}
