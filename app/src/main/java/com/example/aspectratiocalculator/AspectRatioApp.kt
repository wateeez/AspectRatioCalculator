package com.example.aspectratiocalculator

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors

class AspectRatioApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Restore the user's explicit light/dark choice; otherwise follow the system.
        val mode = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(mode)
        // Material You: wallpaper-based colors on Android 12+, static scheme from themes.xml otherwise.
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    companion object {
        const val PREFS = "settings"
        const val KEY_NIGHT_MODE = "night_mode"
    }
}
