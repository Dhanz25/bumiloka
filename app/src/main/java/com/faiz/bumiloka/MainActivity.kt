package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    
    private lateinit var navView: BottomNavigationView
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ Paksa aplikasi menggunakan Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navView = findViewById(R.id.bottom_navigation)
        
        // Sinkronisasi lencana dari Cloud saat aplikasi dibuka
        if (FirebaseAuth.getInstance().currentUser != null) {
            BadgeHelper.syncBadges(this)
        }
        
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> loadFragment(HomeFragment())
                R.id.navigation_activities -> loadFragment(AktivitasFragment())
                R.id.navigation_tips -> loadFragment(TipsFragment())
                R.id.navigation_profile -> loadFragment(ProfileFragment())
            }
            true
        }

        // Handle double back press to exit
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Jika ada fragment di backstack (misal sedang buka detail), biarkan sistem menghandle (pop backstack)
                if (supportFragmentManager.backStackEntryCount > 0) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                    return
                }

                // Jika di halaman utama (backstack kosong)
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Ketuk sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        })
    }

    fun setBottomNavigationVisibility(isVisible: Boolean) {
        if (::navView.isInitialized) {
            navView.visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
