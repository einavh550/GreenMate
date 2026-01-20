package com.example.greenmate_project.ui.plants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.greenmate_project.R
import com.example.greenmate_project.util.AnimationUtils
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

/**
 * My Plants Fragment - Shows list of user's plants.
 * Supports search, filtering, and sorting.
 */
class MyPlantsFragment : Fragment() {

    private val viewModel: MyPlantsViewModel by viewModels()

    private lateinit var inputSearch: TextInputEditText
    private lateinit var chipSort: Chip
    private lateinit var chipAll: Chip
    private lateinit var chipNeedsAttention: Chip
    private lateinit var chipHealthy: Chip
    private lateinit var textResultsCount: TextView
    private lateinit var recyclerPlants: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var imageEmpty: ImageView
    private lateinit var textEmptyTitle: TextView
    private lateinit var textEmptyMessage: TextView
    private lateinit var fabAddPlant: FloatingActionButton
    private lateinit var progressLoading: ProgressBar

    private lateinit var plantAdapter: PlantAdapter

    // Activity result launcher for add plant screen
    private val addPlantLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Refresh plant list after adding
            viewModel.loadPlants()
        }
    }

    // Activity result launcher for plant detail screen
    private val plantDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Refresh plant list after potential changes
            viewModel.loadPlants()
        }
    }

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
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        loadPlants()
    }

    private fun initViews(view: View) {
        inputSearch = view.findViewById(R.id.input_search)
        chipSort = view.findViewById(R.id.chip_sort)
        chipAll = view.findViewById(R.id.chip_all)
        chipNeedsAttention = view.findViewById(R.id.chip_needs_attention)
        chipHealthy = view.findViewById(R.id.chip_healthy)
        textResultsCount = view.findViewById(R.id.text_results_count)
        recyclerPlants = view.findViewById(R.id.recycler_plants)
        layoutEmpty = view.findViewById(R.id.layout_empty)
        imageEmpty = view.findViewById(R.id.image_empty)
        textEmptyTitle = view.findViewById(R.id.text_empty_title)
        textEmptyMessage = view.findViewById(R.id.text_empty_message)
        fabAddPlant = view.findViewById(R.id.fab_add_plant)
        progressLoading = view.findViewById(R.id.progress_loading)
    }

    private fun setupRecyclerView() {
        plantAdapter = PlantAdapter { plant ->
            viewModel.onPlantClicked(plant)
        }
        recyclerPlants.adapter = plantAdapter
    }

    private fun setupListeners() {
        // Search input
        inputSearch.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }

        // Sort chip - cycles through sort options
        chipSort.setOnClickListener {
            viewModel.cycleSortOrder()
        }

        // Filter chips
        chipAll.setOnClickListener {
            setFilterChipSelection(MyPlantsViewModel.PlantFilter.ALL)
            viewModel.setFilter(MyPlantsViewModel.PlantFilter.ALL)
        }

        chipNeedsAttention.setOnClickListener {
            setFilterChipSelection(MyPlantsViewModel.PlantFilter.NEEDS_ATTENTION)
            viewModel.setFilter(MyPlantsViewModel.PlantFilter.NEEDS_ATTENTION)
        }

        chipHealthy.setOnClickListener {
            setFilterChipSelection(MyPlantsViewModel.PlantFilter.HEALTHY)
            viewModel.setFilter(MyPlantsViewModel.PlantFilter.HEALTHY)
        }

        // FAB to add plant
        fabAddPlant.setOnClickListener {
            val intent = AddEditPlantActivity.newIntent(requireContext())
            addPlantLauncher.launch(intent)
            activity?.let { AnimationUtils.applyEnterTransition(it) }
        }
    }

    private fun setFilterChipSelection(filter: MyPlantsViewModel.PlantFilter) {
        chipAll.isChecked = filter == MyPlantsViewModel.PlantFilter.ALL
        chipNeedsAttention.isChecked = filter == MyPlantsViewModel.PlantFilter.NEEDS_ATTENTION
        chipHealthy.isChecked = filter == MyPlantsViewModel.PlantFilter.HEALTHY
    }

    private fun observeViewModel() {
        viewModel.filteredPlants.observe(viewLifecycleOwner) { plants ->
            plantAdapter.submitList(plants)
            updateEmptyState(plants.isEmpty())
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

        viewModel.navigateToPlantDetail.observe(viewLifecycleOwner) { plantId ->
            plantId?.let {
                val intent = PlantDetailActivity.newIntent(requireContext(), it)
                plantDetailLauncher.launch(intent)
                activity?.let { act -> AnimationUtils.applyEnterTransition(act) }
                viewModel.onPlantDetailNavigated()
            }
        }

        viewModel.showDeleteConfirmation.observe(viewLifecycleOwner) { plant ->
            plant?.let {
                showDeleteConfirmationDialog(it)
                viewModel.onDeleteConfirmationShown()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                showMessage(it)
                viewModel.clearSuccessMessage()
            }
        }

        viewModel.resultsCount.observe(viewLifecycleOwner) { count ->
            textResultsCount.text = getString(R.string.search_results_count, count)
        }

        viewModel.hasActiveFilters.observe(viewLifecycleOwner) { hasFilters ->
            textResultsCount.visibility = if (hasFilters) View.VISIBLE else View.GONE
        }

        viewModel.sortOrder.observe(viewLifecycleOwner) { sortOrder ->
            updateSortChipText(sortOrder)
        }
    }

    private fun updateSortChipText(sortOrder: MyPlantsViewModel.SortOrder) {
        chipSort.text = when (sortOrder) {
            MyPlantsViewModel.SortOrder.NAME_ASC -> getString(R.string.filter_sort_name)
            MyPlantsViewModel.SortOrder.NAME_DESC -> "Z-A"
            MyPlantsViewModel.SortOrder.DATE_DESC -> getString(R.string.filter_sort_date)
            MyPlantsViewModel.SortOrder.STATUS -> getString(R.string.filter_sort_status)
        }
    }

    private fun loadPlants() {
        viewModel.loadPlants()
    }

    private fun showLoading(show: Boolean) {
        progressLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            layoutEmpty.visibility = View.VISIBLE
            recyclerPlants.visibility = View.GONE

            // Check if it's due to filters/search or no plants at all
            val hasActiveFilters = viewModel.hasActiveFilters.value ?: false
            if (hasActiveFilters) {
                imageEmpty.setImageResource(R.drawable.ic_search)
                textEmptyTitle.text = getString(R.string.search_no_results)
                textEmptyMessage.text = getString(R.string.search_no_results_filter)
            } else {
                imageEmpty.setImageResource(R.drawable.ic_leaf)
                textEmptyTitle.text = getString(R.string.plants_empty_title)
                textEmptyMessage.text = getString(R.string.plants_empty_message)
            }
        } else {
            layoutEmpty.visibility = View.GONE
            recyclerPlants.visibility = View.VISIBLE
        }
    }

    private fun showDeleteConfirmationDialog(plant: com.example.greenmate_project.model.Plant) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_plant_title)
            .setMessage(getString(R.string.dialog_delete_plant_message, plant.name))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deletePlant(plant)
            }
            .show()
    }

    private fun showMessage(message: String) {
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
        viewModel.loadPlants()
    }

    companion object {
        fun newInstance() = MyPlantsFragment()
    }
}
