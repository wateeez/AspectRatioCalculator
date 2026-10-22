package com.example.aspectratiocalculator

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.aspectratiocalculator.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var calculator: Fragment
    private lateinit var adSize: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Both tabs stay alive and are shown/hidden so entered values survive tab switches.
        if (savedInstanceState == null) {
            calculator = CalculatorFragment()
            adSize = AdSizeFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, calculator, TAG_CALCULATOR)
                .add(R.id.fragmentContainer, adSize, TAG_AD)
                .commitNow()
        } else {
            calculator = supportFragmentManager.findFragmentByTag(TAG_CALCULATOR)!!
            adSize = supportFragmentManager.findFragmentByTag(TAG_AD)!!
        }

        // BottomNavigationView on phones, NavigationRailView on wide screens (layout-sw600dp).
        val nav = binding.root.findViewById<NavigationBarView>(R.id.bottomNav)
        nav.setOnItemSelectedListener { item ->
            showTab(item.itemId == R.id.nav_ad)
            true
        }
        showTab(nav.selectedItemId == R.id.nav_ad)
    }

    private fun showTab(showAd: Boolean) {
        supportActionBar?.setTitle(if (showAd) R.string.ad_title else R.string.app_name)
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .apply {
                if (showAd) hide(calculator).show(adSize) else hide(adSize).show(calculator)
            }
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            showSettings()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private companion object {
        const val TAG_CALCULATOR = "calculator"
        const val TAG_AD = "ad"
    }
}
