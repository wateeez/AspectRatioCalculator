package com.example.aspectratiocalculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.aspectratiocalculator.databinding.ActivityPageSetupBinding
import kotlin.math.max
import kotlin.math.roundToInt

class PageSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPageSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPageSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val savedColumns = AdSettings.columns(this).toDoubleOrNull()?.roundToInt() ?: 1
        binding.columnsSlider.valueTo = max(12, savedColumns).toFloat()
        if (savedInstanceState == null) {
            binding.columnsSlider.value = savedColumns.coerceIn(1, binding.columnsSlider.valueTo.toInt()).toFloat()
            binding.columnWidthInput.setText(AdSettings.columnWidth(this))
        }

        binding.columnsSlider.addOnChangeListener { _, _, _ -> updateSummary() }
        binding.columnWidthInput.doAfterTextChanged {
            binding.columnWidthInputLayout.error = null
            updateSummary()
        }
        updateSummary()

        binding.savePageSetupButton.setOnClickListener {
            val width = binding.columnWidthInput.text.toString()
            val number = width.toDoubleOrNull()
            binding.columnWidthInputLayout.error = when {
                number == null -> getString(R.string.err_required)
                number <= 0 -> getString(R.string.err_positive)
                else -> null
            }
            if (binding.columnWidthInputLayout.error != null) return@setOnClickListener

            AdSettings.prefs(this).edit()
                .putString(AdSettings.KEY_COLUMNS, columns().toString())
                .putString(AdSettings.KEY_COLUMN_WIDTH, width)
                .apply()
            finish()
        }
    }

    private fun columns() = binding.columnsSlider.value.roundToInt()

    private fun updateSummary() {
        binding.columnsValue.text = columns().toString()
        val width = binding.columnWidthInput.text.toString().ifBlank { "—" }
        binding.pageSummary.text = getString(R.string.page_summary, columns().toString(), width)
    }
}
