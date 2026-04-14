package com.faiz.terraviva

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.faiz.terraviva.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Locale
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Cek Session: Jika tidak ada user yang login, arahkan ke LoginActivity
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        enableEdgeToEdge()
        setContentView(R.layout.activity_home_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)

        // Fungsi untuk memperbarui tampilan nama
        fun updateUserName(user: FirebaseUser?) {
            val rawName = when {
                !user?.displayName.isNullOrBlank() -> user?.displayName
                !user?.email.isNullOrBlank() -> user?.email?.substringBefore("@")
                else -> "Bumi Lover"
            }
            
            // Capitalize: Mengubah huruf pertama setiap kata menjadi huruf besar
            val nameToShow = rawName?.split(" ")?.joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            } ?: ""

            tvGreeting.text = getString(R.string.hello_placeholder, nameToShow)
            
            // Setup listener profil dengan nama terbaru
            ivProfile.setOnClickListener { view ->
                val popupMenu = PopupMenu(this@HomeScreen, view)
                
                // Membuat username menjadi BOLD menggunakan SpannableString
                val spannableName = SpannableString(nameToShow)
                spannableName.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    spannableName.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Menyamakan ID Menu dengan ID di Listener
                popupMenu.menu.add(0, 1, 0, spannableName)
                popupMenu.menu.add(0, 2, 1, "Pengaturan Profil")
                popupMenu.menu.add(0, 3, 2, "Logout")

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> {
                            Toast.makeText(this@HomeScreen, "Logged in as $nameToShow", Toast.LENGTH_SHORT).show()
                            true
                        }
                        2 -> {
                            val intent = Intent(this, ProfilActivity::class.java)
                            startActivity(intent)
                            true
                        }
                        3 -> {
                            // Konfirmasi Logout
                            AlertDialog.Builder(this@HomeScreen)
                                .setTitle("Konfirmasi Logout")
                                .setMessage("Apakah Anda yakin ingin keluar?")
                                .setPositiveButton("Ya") { _, _ ->
                                    // Proses Logout
                                    auth.signOut()
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        Toast.makeText(this@HomeScreen, "Berhasil Logout", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@HomeScreen, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                                .setNegativeButton("Tidak", null)
                                .show()
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
        // Tampilkan nama awal dari cache session
        updateUserName(currentUser)

        // Reload data dari server Firebase untuk memastikan Nama muncul (sinkronisasi)
        currentUser.reload().addOnCompleteListener {
            if (it.isSuccessful) {
                updateUserName(auth.currentUser)
            }
        }
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set default aktif
        bottomNav.selectedItemId = R.id.navigation_home

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {

                R.id.navigation_home -> true

                R.id.navigation_activities -> {
                    val intent = Intent(this, AktivitasActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }

                R.id.navigation_tips -> {
                    val intent = Intent(this, TipsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }

                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfilActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }
}
