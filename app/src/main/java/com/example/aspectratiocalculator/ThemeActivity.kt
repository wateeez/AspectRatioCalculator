package com.example.aspectratiocalculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.aspectratiocalculator.databinding.ActivityThemeBinding

class ThemeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThemeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val prefs = getSharedPreferences(AspectRatioApp.PREFS, MODE_PRIVATE)
        fun savedMode() = prefs.getInt(AspectRatioApp.KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        binding.themeGroup.check(ThemeModes.radioIdFor(savedMode()))
        // Applies as soon as it is picked; recreating the activity restores this state from prefs.
        binding.themeGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = ThemeModes.modeFor(checkedId)
            if (mode != savedMode()) {
                prefs.edit().putInt(AspectRatioApp.KEY_NIGHT_MODE, mode).apply()
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }
    }
}
