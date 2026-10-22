package com.example.aspectratiocalculator

import android.animation.ValueAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.example.aspectratiocalculator.databinding.DialogSavePresetBinding
import com.example.aspectratiocalculator.databinding.FragmentCalculatorBinding
import com.google.android.material.R as MaterialR
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.motion.MotionUtils
import com.google.android.material.snackbar.Snackbar
import kotlin.math.min

class CalculatorFragment : Fragment() {
    private var _binding: FragmentCalculatorBinding? = null
    private val binding get() = _binding!!
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
    private var decimalAnimator: ValueAnimator? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        presetManager = PresetManager(requireContext())

        setupInputListeners()
        setupCustomPresetSupport()

        binding.previewBox.background = Motion.previewBackground(requireContext())
        binding.previewBox.setTextColor(Motion.previewTextColor(requireContext()))

        binding.calculateButton.setOnClickListener {
            calculateAspectRatio()
        }

        if (savedInstanceState == null) playEntrance()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) playEntrance()
    }

    private fun playEntrance() {
        Motion.enterStaggered(
            listOf(
                binding.widthInputLayout, binding.heightInputLayout, binding.presetsLabel,
                binding.presetsScroll, binding.calculateButton, binding.resultCard, binding.previewContainer
            )
        )
    }

    override fun onDestroyView() {
        decimalAnimator?.cancel()
        _binding = null
        super.onDestroyView()
    }

    private fun showMessage(@StringRes resId: Int) {
        Snackbar.make(binding.root, resId, Snackbar.LENGTH_SHORT).show()
    }

    private fun setupCustomPresetSupport() {
        binding.savePresetButton.setOnClickListener { showSavePresetDialog() }
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
            showMessage(R.string.err_enter_dimensions)
            return
        }

        val dialogBinding = DialogSavePresetBinding.inflate(LayoutInflater.from(requireContext()))

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.save_preset_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = dialogBinding.presetNameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    presetManager.saveCustomPreset(name, width, height)
                    updateCustomPresetChips()
                }
            }
            .setNegativeButton(R.string.cancel, null)
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
        val chip = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_preset_chip, binding.presetsGroup, false) as Chip
        chip.apply {
            text = name
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
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_preset_title)
            .setMessage(getString(R.string.delete_preset_message, presetName))
            .setPositiveButton(R.string.delete) { _, _ ->
                presetManager.deleteCustomPreset(presetName)
                updateCustomPresetChips()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setupInputListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Clear chip selection when user types
                binding.presetsGroup.clearCheck()
                binding.widthInputLayout.error = null
                binding.heightInputLayout.error = null
            }
        }

        binding.widthInput.addTextChangedListener(textWatcher)
        binding.heightInput.addTextChangedListener(textWatcher)
    }

    private fun calculateAspectRatio() {
        val widthStr = binding.widthInput.text.toString()
        val heightStr = binding.heightInput.text.toString()

        if (widthStr.isEmpty()) binding.widthInputLayout.fail(getString(R.string.err_required))
        if (heightStr.isEmpty()) binding.heightInputLayout.fail(getString(R.string.err_required))
        if (widthStr.isEmpty() || heightStr.isEmpty()) return

        val width = widthStr.toFloatOrNull()
        val height = heightStr.toFloatOrNull()

        if (width == null) binding.widthInputLayout.fail(getString(R.string.err_invalid_numbers))
        if (height == null) binding.heightInputLayout.fail(getString(R.string.err_invalid_numbers))
        if (width == null || height == null) return

        if (width <= 0) binding.widthInputLayout.fail(getString(R.string.err_positive))
        if (height <= 0) binding.heightInputLayout.fail(getString(R.string.err_positive))
        if (width <= 0 || height <= 0) return

        try {
            val gcd = findGCD(width.toInt(), height.toInt())
            val simplifiedWidth = width / gcd
            val simplifiedHeight = height / gcd

            val ratio = "${simplifiedWidth.toInt()}:${simplifiedHeight.toInt()}"
            val decimal = width / height

            // Bounds change for the preview, count-up for the decimal, pop for the card.
            TransitionManager.beginDelayedTransition(binding.contentRoot, previewTransition())
            binding.resultRatio.text = ratio
            decimalAnimator?.cancel()
            decimalAnimator = Motion.countUp(requireContext()) { p ->
                binding.resultDecimal.text = getString(R.string.decimal_format, decimal * p)
            }
            Motion.pop(binding.resultCard)
            binding.previewBox.text = ratio
            updatePreview(width, height)
        } catch (e: Exception) {
            showMessage(R.string.err_calculating)
        }
    }

    private fun previewTransition(): TransitionSet {
        val duration = MotionUtils.resolveThemeDuration(requireContext(), MaterialR.attr.motionDurationLong2, 400)
        val interpolator = MotionUtils.resolveThemeInterpolator(
            requireContext(), MaterialR.attr.motionEasingEmphasizedInterpolator, PathInterpolator(0.2f, 0f, 0f, 1f)
        )
        return TransitionSet().apply {
            addTransition(ChangeBounds().addTarget(binding.previewBox))
            setDuration(duration.toLong())
            setInterpolator(interpolator)
        }
    }

    private fun updatePreview(width: Float, height: Float) {
        val container = binding.previewContainer
        val padding = (container.width * 0.1).toInt() // 10% padding
        val availableWidth = container.width - padding * 2
        val availableHeight = container.height - padding * 2
        if (availableWidth <= 0 || availableHeight <= 0) return

        val scale = min(availableWidth / width, availableHeight / height)
        val params = binding.previewBox.layoutParams
        params.width = (width * scale).toInt()
        params.height = (height * scale).toInt()
        binding.previewBox.layoutParams = params
    }

    private fun findGCD(a: Int, b: Int): Int {
        return if (b == 0) a else findGCD(b, a % b)
    }
}
