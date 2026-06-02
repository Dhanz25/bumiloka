package com.faiz.bumiloka

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.faiz.bumiloka.admin.DashboardAdminFragment

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin)

        // TAMPILKAN DASHBOARD ADMIN PERTAMA KALI

        if (savedInstanceState == null) {

            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    DashboardAdminFragment()
                )
                .commit()
        }
    }
}