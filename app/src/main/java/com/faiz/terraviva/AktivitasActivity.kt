package com.faiz.terraviva

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AktivitasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_aktivitas)
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)

        // set aktif ke aktivitas
        bottomNav.selectedItemId = R.id.navigation_activities

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {

                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeScreen::class.java))
                    true
                }

                R.id.navigation_activities -> true

                R.id.navigation_tips -> {
                    startActivity(Intent(this, TipsActivity::class.java))
                    true
                }

                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfilActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }
}