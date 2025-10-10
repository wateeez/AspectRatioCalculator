package com.example.aspectratiocalculator

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.InputType
import android.view.ViewGroup.LayoutParams
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.aspectratiocalculator.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val defaultPresets = mapOf(
        "16:9" to Pair(16f, 9f),
        "4:3" to Pair(4f, 3f),
        "21:9" to Pair(21f, 9f),
        "1:1" to Pair(1f, 1f),
        "3:2" to Pair(3f, 2f),
        "2.39:1" to Pair(2.39f, 1f),  // Anamorphic Widescreen
        "5:4" to Pair(5f, 4f),        // Common in photography
        "9:16" to Pair(9f, 16f),      // Portrait mode
        "1.85:1" to Pair(1.85f, 1f),  // Standard Widescreen
        "8:5" to Pair(8f, 5f)         // Common in photography
    )

    private lateinit var presetManager: PresetManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presetManager = PresetManager(this)
        
        setupThemeSwitch()
        setupInputListeners()
        setupPresetChips()
        setupCustomPresetSupport()
        
        binding.calculateButton.setOnClickListener {
            calculateAspectRatio()
        }
    }

    private fun setupThemeSwitch() {
        // Set initial state based on current theme
        binding.themeSwitch.isChecked = resources.configuration.uiMode and 
            android.content.res.Configuration.UI_MODE_NIGHT_MASK == 
            android.content.res.Configuration.UI_MODE_NIGHT_YES

        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupCustomPresetSupport() {
        binding.calculateButton.setOnLongClickListener {
            showSavePresetDialog()
            true
        }

        // Add custom presets to the chip group
        updateCustomPresetChips()
    }

    private fun showSavePresetDialog() {
        val width = binding.widthInput.text.toString().toFloatOrNull()
        val height = binding.heightInput.text.toString().toFloatOrNull()

        if (width == null || height == null) {
            Toast.makeText(this, "Please enter valid dimensions first", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(this).apply {
            hint = "Preset Name"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        AlertDialog.Builder(this)
            .setTitle("Save Custom Preset")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString()
                if (name.isNotEmpty()) {
                    presetManager.saveCustomPreset(name, width, height)
                    updateCustomPresetChips()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateCustomPresetChips() {
        // Remove existing custom preset chips
        binding.presetsGroup.removeAllViews()

        // Add default presets
        defaultPresets.forEach { (ratio, values) ->
            addPresetChip(ratio, values.first, values.second)
        }

        // Add custom presets
        presetManager.getCustomPresets().forEach { (name, values) ->
            addPresetChip(name, values.first, values.second, true)
        }
    }

    private fun addPresetChip(name: String, width: Float, height: Float, isCustom: Boolean = false) {
        val chip = Chip(this).apply {
            text = name
            isCheckable = true
            setOnClickListener {
                binding.widthInput.setText(width.toString())
                binding.heightInput.setText(height.toString())
                calculateAspectRatio()
            }
            if (isCustom) {
                setOnLongClickListener {
                    showDeletePresetDialog(name)
                    true
                }
            }
        }
        binding.presetsGroup.addView(chip)
    }

    private fun showDeletePresetDialog(presetName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Custom Preset")
            .setMessage("Do you want to delete the preset '$presetName'?")
            .setPositiveButton("Delete") { _, _ ->
                presetManager.deleteCustomPreset(presetName)
                updateCustomPresetChips()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupInputListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Clear chip selection when user types
                binding.presetsGroup.clearCheck()
            }
        }

        binding.widthInput.addTextChangedListener(textWatcher)
        binding.heightInput.addTextChangedListener(textWatcher)
    }

    private fun setupPresetChips() {
        presets.forEach { (ratio, values) ->
            binding.presetsGroup.findViewById<Chip>(
                resources.getIdentifier(
                    "preset${ratio.replace(":", "")}",
                    "id",
                    packageName
                )
            )?.setOnClickListener {
                binding.widthInput.setText(values.first.toString())
                binding.heightInput.setText(values.second.toString())
                calculateAspectRatio()
            }
        }
    }

    private fun calculateAspectRatio() {
        val widthStr = binding.widthInput.text.toString()
        val heightStr = binding.heightInput.text.toString()

        if (widthStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please enter both width and height", Toast.LENGTH_SHORT).show()
            return
        }

        val width = widthStr.toFloatOrNull()
        val height = heightStr.toFloatOrNull()

        if (width == null || height == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (width <= 0 || height <= 0) {
            Toast.makeText(this, "Values must be greater than 0", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val gcd = findGCD(width.toInt(), height.toInt())
            val simplifiedWidth = width / gcd
            val simplifiedHeight = height / gcd

            val ratio = "${simplifiedWidth.toInt()}:${simplifiedHeight.toInt()}"
            val decimal = String.format("%.3f", width / height)
            binding.resultText.text = "Aspect Ratio: $ratio\nDecimal: $decimal"

            updatePreview(width, height)
        } catch (e: Exception) {
            Toast.makeText(this, "Error calculating aspect ratio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePreview(width: Float, height: Float) {
        val container = binding.previewContainer
        container.post {
            try {
                val containerWidth = container.width
                val containerHeight = container.height
                
                // Add padding to container size
                val padding = (containerWidth * 0.1).toInt() // 10% padding
                val availableWidth = containerWidth - (padding * 2)
                val availableHeight = containerHeight - (padding * 2)

                val scale = min(availableWidth / width, availableHeight / height)
                val scaledWidth = (width * scale).toInt()
                val scaledHeight = (height * scale).toInt()

                val previewBox = binding.previewBox
                val params = previewBox.layoutParams
                params.width = scaledWidth
                params.height = scaledHeight
                previewBox.layoutParams = params

                // Add a subtle animation
                previewBox.alpha = 0f
                previewBox.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()
            } catch (e: Exception) {
                Toast.makeText(this, "Error updating preview", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun findGCD(a: Int, b: Int): Int {
        return if (b == 0) a else findGCD(b, a % b)
    }
}