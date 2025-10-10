package com.example.aspectratiocalculator

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PresetManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("presets", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveCustomPreset(name: String, width: Float, height: Float) {
        val presets = getCustomPresets().toMutableMap()
        presets[name] = Pair(width, height)
        
        val json = gson.toJson(presets)
        sharedPreferences.edit().putString("custom_presets", json).apply()
    }

    fun getCustomPresets(): Map<String, Pair<Float, Float>> {
        val json = sharedPreferences.getString("custom_presets", "{}")
        val type = object : TypeToken<Map<String, Pair<Float, Float>>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    fun deleteCustomPreset(name: String) {
        val presets = getCustomPresets().toMutableMap()
        presets.remove(name)
        
        val json = gson.toJson(presets)
        sharedPreferences.edit().putString("custom_presets", json).apply()
    }
}