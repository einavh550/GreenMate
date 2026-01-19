package com.example.greenmate_project.ui.plants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.greenmate_project.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * My Plants Fragment - Shows list of user's plants.
 * Allows adding new plants and viewing plant details.
 */
class MyPlantsFragment : Fragment() {

    private lateinit var recyclerPlants: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var fabAddPlant: FloatingActionButton
    private lateinit var progressLoading: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_plants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        setupRecyclerView()
        loadPlants()
    }

    private fun initViews(view: View) {
        recyclerPlants = view.findViewById(R.id.recycler_plants)
        layoutEmpty = view.findViewById(R.id.layout_empty)
        fabAddPlant = view.findViewById(R.id.fab_add_plant)
        progressLoading = view.findViewById(R.id.progress_loading)
    }

    private fun setupListeners() {
        fabAddPlant.setOnClickListener {
            // TODO: Navigate to Add Plant screen in Milestone 6
        }
    }

    private fun setupRecyclerView() {
        // TODO: Set up adapter in Milestone 5
    }

    private fun loadPlants() {
        // TODO: Load plants from repository in Milestone 5
        // For now, show empty state
        showEmptyState()
    }

    private fun showLoading(show: Boolean) {
        progressLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState() {
        layoutEmpty.visibility = View.VISIBLE
        recyclerPlants.visibility = View.GONE
    }

    private fun showPlantsList() {
        layoutEmpty.visibility = View.GONE
        recyclerPlants.visibility = View.VISIBLE
    }

    companion object {
        fun newInstance() = MyPlantsFragment()
    }
}
