package com.faiz.bumiloka

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // KODE db.updateChildren(...) SUDAH DIHAPUS DARI SINI!
        // Jangan pernah mereset database di MainActivity ya!

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // default fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navigation_activities -> {
                    replaceFragment(AktivitasFragment())
                    true
                }
                R.id.navigation_tips -> {
                    // Assuming TipsFragment exists or create it
                    replaceFragment(TipsFragment()) // Placeholder
                    true
                }
                R.id.navigation_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}