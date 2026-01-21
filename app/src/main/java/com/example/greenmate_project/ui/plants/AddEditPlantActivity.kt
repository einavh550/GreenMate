package com.example.greenmate_project.ui.plants

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import com.example.greenmate_project.R
import com.example.greenmate_project.util.AnimationUtils
import com.example.greenmate_project.util.Constants
import com.example.greenmate_project.util.ImageUtils
import com.example.greenmate_project.util.PreferencesManager
import com.example.greenmate_project.widget.CareTasksWidget
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File

/**
 * Activity for adding a new plant or editing an existing one.
 */
class AddEditPlantActivity : AppCompatActivity() {

    private val viewModel: AddEditPlantViewModel by viewModels()

    private lateinit var toolbar: MaterialToolbar
    private lateinit var cardPhoto: MaterialCardView
    private lateinit var imagePlant: ImageView
    private lateinit var inputLayoutName: TextInputLayout
    private lateinit var inputLayoutLocation: TextInputLayout
    private lateinit var inputName: TextInputEditText
    private lateinit var inputLocation: AutoCompleteTextView
    private lateinit var sliderWaterInterval: Slider
    private lateinit var sliderFertilizeInterval: Slider
    private lateinit var textWaterIntervalValue: TextView
    private lateinit var textFertilizeIntervalValue: TextView
    private lateinit var layoutLoading: FrameLayout

    private var plantId: String? = null
    private var isEditMode: Boolean = false

    // Temporary file for camera capture
    private var tempCameraUri: Uri? = null

    // Gallery picker
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleImageSelected(it) }
    }

    // Camera capture
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { handleImageSelected(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_plant)

        // Get intent extras
        plantId = intent.getStringExtra(Constants.Extras.EXTRA_PLANT_ID)
        isEditMode = intent.getBooleanExtra(Constants.Extras.EXTRA_IS_EDIT_MODE, false)

        initViews()
        setupToolbar()
        setupListeners()
        observeViewModel()

        // Load plant data if editing, otherwise set defaults from preferences
        if (isEditMode && plantId != null) {
            viewModel.loadPlant(plantId!!)
        } else {
            // Set default intervals from user preferences
            val preferencesManager = PreferencesManager.getInstance(this)
            viewModel.setWaterInterval(preferencesManager.getDefaultWaterInterval())
            viewModel.setFertilizeInterval(preferencesManager.getDefaultFertilizeInterval())
        }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        cardPhoto = findViewById(R.id.card_photo)
        imagePlant = findViewById(R.id.image_plant)
        inputLayoutName = findViewById(R.id.input_layout_name)
        inputLayoutLocation = findViewById(R.id.input_layout_location)
        inputName = findViewById(R.id.input_name)
        inputLocation = findViewById(R.id.input_location)
        sliderWaterInterval = findViewById(R.id.slider_water_interval)
        sliderFertilizeInterval = findViewById(R.id.slider_fertilize_interval)
        textWaterIntervalValue = findViewById(R.id.text_water_interval_value)
        textFertilizeIntervalValue = findViewById(R.id.text_fertilize_interval_value)
        layoutLoading = findViewById(R.id.layout_loading)

        // Setup location dropdown with fixed values from arrays.xml
        setupLocationDropdown()
    }

    private fun setupLocationDropdown() {
        val locations = resources.getStringArray(R.array.plant_locations)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locations)
        inputLocation.setAdapter(adapter)
    }

    private fun setupToolbar() {
        toolbar.title = if (isEditMode) {
            getString(R.string.title_edit_plant)
        } else {
            getString(R.string.title_add_plant)
        }

        toolbar.setNavigationOnClickListener {
            finishWithAnimation()
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

    private fun setupListeners() {
        // Photo card click
        cardPhoto.setOnClickListener {
            showImagePickerDialog()
        }

        // Name input
        inputName.doAfterTextChanged { text ->
            viewModel.setPlantName(text?.toString() ?: "")
        }

        // Location dropdown selection
        inputLocation.setOnItemClickListener { _, _, position, _ ->
            val locations = resources.getStringArray(R.array.plant_locations)
            if (position < locations.size) {
                viewModel.setLocation(locations[position])
            }
        }

        // Water interval slider
        sliderWaterInterval.addOnChangeListener { _, value, _ ->
            viewModel.setWaterInterval(value.toInt())
        }

        // Fertilize interval slider
        sliderFertilizeInterval.addOnChangeListener { _, value, _ ->
            viewModel.setFertilizeInterval(value.toInt())
        }

        // Save button
        findViewById<View>(R.id.btn_save).setOnClickListener {
            viewModel.savePlant(this)
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf(
            getString(R.string.option_take_photo),
            getString(R.string.option_choose_gallery)
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_select_image)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> launchGallery()
                }
            }
            .show()
    }

    private fun launchCamera() {
        val photoFile = File(cacheDir, "temp_plant_photo.jpg")
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    private fun launchGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun handleImageSelected(uri: Uri) {
        // Save the image locally and update the view model
        val savedPath = ImageUtils.saveImageFromUri(this, uri, viewModel.photoPath.value)
        savedPath?.let { path ->
            viewModel.setPhotoPath(path)
        }
    }

    private fun observeViewModel() {
        viewModel.plantName.observe(this) { name ->
            if (inputName.text?.toString() != name) {
                inputName.setText(name)
            }
        }

        viewModel.location.observe(this) { location ->
            if (inputLocation.text?.toString() != location) {
                inputLocation.setText(location, false)
            }
        }

        viewModel.waterInterval.observe(this) { days ->
            sliderWaterInterval.value = days.toFloat()
            textWaterIntervalValue.text = formatIntervalText(days)
        }

        viewModel.fertilizeInterval.observe(this) { days ->
            sliderFertilizeInterval.value = days.toFloat()
            textFertilizeIntervalValue.text = formatIntervalText(days)
        }

        viewModel.photoPath.observe(this) { path ->
            if (path.isNullOrEmpty()) {
                // Show default icon
                imagePlant.setImageResource(R.drawable.ic_leaf)
                imagePlant.setPadding(
                    resources.getDimensionPixelSize(R.dimen.spacing_xl),
                    resources.getDimensionPixelSize(R.dimen.spacing_xl),
                    resources.getDimensionPixelSize(R.dimen.spacing_xl),
                    resources.getDimensionPixelSize(R.dimen.spacing_xl)
                )
                imagePlant.colorFilter = null
            } else {
                // Load and display the image
                val bitmap = ImageUtils.loadImage(path)
                if (bitmap != null) {
                    imagePlant.setImageBitmap(bitmap)
                    imagePlant.setPadding(0, 0, 0, 0)
                    imagePlant.colorFilter = null
                }
            }
        }

        viewModel.nameError.observe(this) { error ->
            inputLayoutName.error = error
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

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                showSuccess()
                CareTasksWidget.updateAllWidgets(this)
                setResult(RESULT_OK)
                finishWithAnimation()
            }
        }
    }

    private fun formatIntervalText(days: Int): String {
        return if (days == 1) {
            getString(R.string.interval_every_day)
        } else {
            getString(R.string.interval_every_n_days, days)
        }
    }

    private fun showError(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccess() {
        val message = getString(R.string.msg_plant_saved)
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        /**
         * Creates an intent to add a new plant.
         */
        fun newIntent(context: Context): Intent {
            return Intent(context, AddEditPlantActivity::class.java)
        }

        /**
         * Creates an intent to edit an existing plant.
         */
        fun editIntent(context: Context, plantId: String): Intent {
            return Intent(context, AddEditPlantActivity::class.java).apply {
                putExtra(Constants.Extras.EXTRA_PLANT_ID, plantId)
                putExtra(Constants.Extras.EXTRA_IS_EDIT_MODE, true)
            }
        }
    }
}
