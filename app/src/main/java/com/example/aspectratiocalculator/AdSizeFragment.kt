package com.example.aspectratiocalculator

import android.animation.ValueAnimator
import android.os.Bundle
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.example.aspectratiocalculator.databinding.FragmentAdSizeBinding
import kotlin.math.min

class AdSizeFragment : Fragment() {
    private var _binding: FragmentAdSizeBinding? = null
    private val binding get() = _binding!!
    private var numberAnimator: ValueAnimator? = null
    // Page setup is edited in the toolbar settings dialog; recalculate when it changes.
    private val settingsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        if (_binding != null) {
            updateSettingsSummary()
            convert(silent = true)
        }
    }

    private val ratios = mapOf(
        R.id.ratioCustom to null,
        R.id.ratio11 to (1 to 1),
        R.id.ratio21 to (2 to 1),
        R.id.ratio12 to (1 to 2),
        R.id.ratio43 to (4 to 3),
        R.id.ratio34 to (3 to 4)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdSizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateSettingsSummary()
        AdSettings.prefs(requireContext()).registerOnSharedPreferenceChangeListener(settingsListener)

        // Picking a ratio recalculates right away when the inputs are already valid.
        binding.ratioGroup.setOnCheckedStateChangeListener { _, _ -> convert(silent = true) }
        binding.convertButton.setOnClickListener { convert(silent = false) }

        binding.adPreviewBox.background = Motion.previewBackground(requireContext())
        binding.adPreviewBox.setTextColor(Motion.previewTextColor(requireContext()))

        val clearErrors = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.ccInputLayout.error = null
            }
        }
        binding.ccInput.addTextChangedListener(clearErrors)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) return
        Motion.enterStaggered(
            listOf(
                binding.ccInputLayout, binding.ratioScroll,
                binding.convertButton, binding.adResultCard, binding.adPreviewContainer
            )
        )
    }

    override fun onDestroyView() {
        AdSettings.prefs(requireContext()).unregisterOnSharedPreferenceChangeListener(settingsListener)
        numberAnimator?.cancel()
        _binding = null
        super.onDestroyView()
    }

    private fun updateSettingsSummary() {
        binding.settingsSummary.text =
            getString(R.string.page_summary, AdSettings.columns(requireContext()), AdSettings.columnWidth(requireContext()))
    }

    private fun selectedRatio(): Pair<Int, Int>? = ratios[binding.ratioGroup.checkedChipId]

    private fun convert(silent: Boolean) {
        val cc = binding.ccInput.text.toString().toDoubleOrNull()
        val columnWidth = AdSettings.columnWidth(requireContext()).toDoubleOrNull()
        val columns = AdSettings.columns(requireContext()).toDoubleOrNull()
        val ratio = selectedRatio()

        if (cc == null || cc <= 0) {
            if (!silent) binding.ccInputLayout.fail(getString(if (cc == null) R.string.err_required else R.string.err_positive))
            return
        }
        if (columnWidth == null || columnWidth <= 0 || columns == null || columns <= 0) return

        val size = AdSizeCalculator.calculate(cc, columnWidth, columns, ratio)

        TransitionManager.beginDelayedTransition(binding.adPreviewContainer.parent as android.view.ViewGroup, previewTransition())
        numberAnimator?.cancel()
        numberAnimator = Motion.countUp(requireContext()) { p ->
            binding.adResultText.text = getString(
                R.string.ad_result_format,
                size.widthCm * p, size.heightCm * p, size.widthInch * p, size.heightInch * p,
                size.columns * p, size.columnCentimeters * p, size.ratio * p
            )
        }
        Motion.pop(binding.adResultCard)
        binding.adPreviewBox.text = getString(R.string.ad_preview_label, size.widthCm, size.heightCm)
        updatePreview(size.widthCm, size.heightCm)
    }

    private fun previewTransition(): TransitionSet = TransitionSet().apply {
        addTransition(ChangeBounds().addTarget(binding.adPreviewBox))
        duration = 400
    }

    private fun updatePreview(width: Double, height: Double) {
        val container = binding.adPreviewContainer
        val padding = (container.width * 0.1).toInt()
        val availableWidth = container.width - padding * 2
        val availableHeight = container.height - padding * 2
        if (availableWidth <= 0 || availableHeight <= 0) return

        val scale = min(availableWidth / width, availableHeight / height)
        val params = binding.adPreviewBox.layoutParams
        params.width = (width * scale).toInt()
        params.height = (height * scale).toInt()
        binding.adPreviewBox.layoutParams = params
    }
}
