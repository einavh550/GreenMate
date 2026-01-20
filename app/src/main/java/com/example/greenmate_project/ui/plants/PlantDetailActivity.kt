package com.example.greenmate_project.ui.plants

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.greenmate_project.R
import com.example.greenmate_project.model.ActionType
import com.example.greenmate_project.model.PlantStatus
import com.example.greenmate_project.util.AnimationUtils
import com.example.greenmate_project.util.Constants
import com.example.greenmate_project.util.ImageUtils
import com.example.greenmate_project.widget.CareTasksWidget
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Activity for viewing plant details, care schedule, and history.
 */
class PlantDetailActivity : AppCompatActivity() {

    private val viewModel: PlantDetailViewModel by viewModels()

    private lateinit var toolbar: MaterialToolbar
    private lateinit var imagePlant: ImageView
    private lateinit var textPlantName: TextView
    private lateinit var textLocation: TextView
    private lateinit var badgeStatus: TextView
    private lateinit var textNextWater: TextView
    private lateinit var textNextFertilize: TextView
    private lateinit var btnWater: MaterialButton
    private lateinit var btnFertilize: MaterialButton
    private lateinit var recyclerHistory: RecyclerView
    private lateinit var textHistoryEmpty: TextView
    private lateinit var layoutLoading: FrameLayout

    private lateinit var historyAdapter: CareHistoryAdapter

    private var plantId: String? = null

    // Activity result launcher for edit screen
    private val editPlantLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Refresh plant data after editing
            viewModel.refreshPlant()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_detail)

        plantId = intent.getStringExtra(Constants.Extras.EXTRA_PLANT_ID)
        if (plantId == null) {
            finish()
            return
        }

        initViews()
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.loadPlant(plantId!!)
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        imagePlant = findViewById(R.id.image_plant)
        textPlantName = findViewById(R.id.text_plant_name)
        textLocation = findViewById(R.id.text_location)
        badgeStatus = findViewById(R.id.badge_status)
        textNextWater = findViewById(R.id.text_next_water)
        textNextFertilize = findViewById(R.id.text_next_fertilize)
        btnWater = findViewById(R.id.btn_water)
        btnFertilize = findViewById(R.id.btn_fertilize)
        recyclerHistory = findViewById(R.id.recycler_history)
        textHistoryEmpty = findViewById(R.id.text_history_empty)
        layoutLoading = findViewById(R.id.layout_loading)
    }

    private fun setupToolbar() {
        toolbar.title = getString(R.string.title_plant_details)

        toolbar.setNavigationOnClickListener {
            finishWithAnimation()
        }

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    plantId?.let { id ->
                        val intent = AddEditPlantActivity.editIntent(this, id)
                        editPlantLauncher.launch(intent)
                        AnimationUtils.applyEnterTransition(this)
                    }
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    private fun finishWithAnimation() {
        finish()
        AnimationUtils.applyExitTransition(this)
    }

    @Deprecated("Use finishWithAnimation instead")
    override fun onBackPressed() {
        super.onBackPressed()
        AnimationUtils.applyExitTransition(this)
    }

    private fun setupRecyclerView() {
        historyAdapter = CareHistoryAdapter()
        recyclerHistory.adapter = historyAdapter
    }

    private fun setupListeners() {
        btnWater.setOnClickListener {
            viewModel.waterPlant()
        }

        btnFertilize.setOnClickListener {
            viewModel.fertilizePlant()
        }
    }

    private fun observeViewModel() {
        viewModel.plant.observe(this) { plant ->
            textPlantName.text = plant.name
            textLocation.text = plant.location.ifEmpty { "-" }
            toolbar.title = plant.name

            // Load plant image or show default
            val bitmap = ImageUtils.loadImage(plant.photoUrl)
            if (bitmap != null) {
                imagePlant.setImageBitmap(bitmap)
                imagePlant.colorFilter = null
            } else {
                imagePlant.setImageResource(R.drawable.ic_leaf)
                imagePlant.setColorFilter(ContextCompat.getColor(this, R.color.primary_light))
            }
        }

        viewModel.waterDaysRemaining.observe(this) { days ->
            textNextWater.text = formatDaysRemaining(days)
            textNextWater.setTextColor(getStatusColor(days))
        }

        viewModel.fertilizeDaysRemaining.observe(this) { days ->
            textNextFertilize.text = formatDaysRemaining(days)
            textNextFertilize.setTextColor(getStatusColor(days))
        }

        viewModel.plantStatus.observe(this) { status ->
            updateStatusBadge(status)
        }

        viewModel.careHistory.observe(this) { history ->
            historyAdapter.submitList(history)
            textHistoryEmpty.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
            recyclerHistory.visibility = if (history.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            layoutLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                showError(it)
                viewModel.clearError()
            }
        }

        viewModel.actionSuccess.observe(this) { actionType ->
            actionType?.let {
                val message = when (it) {
                    ActionType.WATER -> getString(R.string.msg_plant_watered)
                    ActionType.FERTILIZE -> getString(R.string.msg_plant_fertilized)
                }
                showSuccess(message)
                CareTasksWidget.updateAllWidgets(this)
                viewModel.clearActionSuccess()
            }
        }

        viewModel.deleteSuccess.observe(this) { success ->
            if (success) {
                showSuccess(getString(R.string.msg_plant_deleted))
                CareTasksWidget.updateAllWidgets(this)
                setResult(RESULT_OK)
                finishWithAnimation()
            }
        }
    }

    private fun formatDaysRemaining(days: Int): String {
        return when {
            days < -1 -> getString(R.string.status_days_overdue, -days)
            days == -1 -> getString(R.string.status_one_day_overdue)
            days == 0 -> getString(R.string.status_due_today)
            days == 1 -> getString(R.string.status_due_tomorrow)
            else -> getString(R.string.status_due_in_days, days)
        }
    }

    private fun getStatusColor(days: Int): Int {
        return ContextCompat.getColor(this, when {
            days < 0 -> R.color.score_red
            days <= 1 -> R.color.score_yellow
            else -> R.color.text_secondary
        })
    }

    private fun updateStatusBadge(status: PlantStatus) {
        when (status) {
            PlantStatus.HEALTHY -> {
                badgeStatus.text = getString(R.string.plant_status_ok)
                badgeStatus.backgroundTintList = ContextCompat.getColorStateList(this, R.color.score_green)
            }
            PlantStatus.NEEDS_ATTENTION -> {
                badgeStatus.text = getString(R.string.plant_status_needs_attention)
                badgeStatus.backgroundTintList = ContextCompat.getColorStateList(this, R.color.score_yellow)
            }
            PlantStatus.OVERDUE -> {
                badgeStatus.text = getString(R.string.plant_status_overdue)
                badgeStatus.backgroundTintList = ContextCompat.getColorStateList(this, R.color.score_red)
            }
        }
    }

    private fun showDeleteConfirmation() {
        val plantName = viewModel.plant.value?.name ?: ""
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_plant_title)
            .setMessage(getString(R.string.dialog_delete_plant_message, plantName))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deletePlant()
            }
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        /**
         * Creates an intent to view plant details.
         */
        fun newIntent(context: Context, plantId: String): Intent {
            return Intent(context, PlantDetailActivity::class.java).apply {
                putExtra(Constants.Extras.EXTRA_PLANT_ID, plantId)
            }
        }
    }
}
