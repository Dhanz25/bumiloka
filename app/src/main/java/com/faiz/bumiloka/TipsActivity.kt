package com.faiz.bumiloka

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class TipsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tips)
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)

        // set aktif ke aktivitas
        bottomNav.selectedItemId = R.id.navigation_tips

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {

                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeScreen::class.java))
                    true
                }

                R.id.navigation_activities -> {
                    startActivity(Intent(this, AktivitasActivity::class.java))
                    true
                }

                R.id.navigation_tips -> true

                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfilActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }
}