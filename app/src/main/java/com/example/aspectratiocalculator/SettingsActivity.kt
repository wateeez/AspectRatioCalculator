package com.example.aspectratiocalculator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.aspectratiocalculator.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.themeRow.setOnClickListener { startActivity(Intent(this, ThemeActivity::class.java)) }
        binding.pageSetupRow.setOnClickListener { startActivity(Intent(this, PageSetupActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        val mode = getSharedPreferences(AspectRatioApp.PREFS, MODE_PRIVATE)
            .getInt(AspectRatioApp.KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        binding.themeSummary.setText(ThemeModes.labelFor(mode))
        binding.pageSetupSummary.text = getString(R.string.page_summary, AdSettings.columns(this), AdSettings.columnWidth(this))
    }
}
