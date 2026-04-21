package com.faiz.bumiloka

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class ProfilActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profil)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val btnPengaturan = findViewById<LinearLayout>(R.id.btnPengaturan)

        currentUser?.let { user ->
            val rawName = when {
                !user.displayName.isNullOrBlank() -> user.displayName
                !user.email.isNullOrBlank() -> user.email?.substringBefore("@")
                else -> "Bumi Lover"
            }

            val nameToShow = rawName?.split(" ")?.joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            } ?: ""

            tvProfileName.text = nameToShow
        }

        btnPengaturan.setOnClickListener {
            val intent = Intent(this, PengaturanActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)

        // set aktif ke aktivitas
        bottomNav.selectedItemId = R.id.navigation_profile

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

                R.id.navigation_tips -> {
                    startActivity(Intent(this, TipsActivity::class.java))
                    true
                }

                R.id.navigation_profile -> true

                else -> false
            }
        }
    }
}