package com.example.aspectratiocalculator

import android.content.Context
import android.content.SharedPreferences

/** Newspaper page setup, edited from the toolbar settings dialog. */
object AdSettings {
    const val PREFS = "ad_settings"
    const val KEY_COLUMNS = "columns"
    const val KEY_COLUMN_WIDTH = "column_width"

    fun prefs(context: Context): SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun columns(context: Context): String =
        prefs(context).getString(KEY_COLUMNS, null) ?: context.getString(R.string.default_columns)

    fun columnWidth(context: Context): String =
        prefs(context).getString(KEY_COLUMN_WIDTH, null) ?: context.getString(R.string.default_column_width)
}
