package com.example.aspectratiocalculator

import androidx.appcompat.app.AppCompatDelegate

/** Maps the theme radio buttons and their summary text to night modes. */
object ThemeModes {
    private val modes = mapOf(
        R.id.themeSystem to AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        R.id.themeLight to AppCompatDelegate.MODE_NIGHT_NO,
        R.id.themeDark to AppCompatDelegate.MODE_NIGHT_YES,
    )

    fun modeFor(radioId: Int): Int = modes.getValue(radioId)

    fun radioIdFor(mode: Int): Int = modes.entries.firstOrNull { it.value == mode }?.key ?: R.id.themeSystem

    fun labelFor(mode: Int): Int = when (mode) {
        AppCompatDelegate.MODE_NIGHT_NO -> R.string.theme_light
        AppCompatDelegate.MODE_NIGHT_YES -> R.string.theme_dark
        else -> R.string.theme_system
    }
}
