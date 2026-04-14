package com.faiz.terraviva

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
    }
}