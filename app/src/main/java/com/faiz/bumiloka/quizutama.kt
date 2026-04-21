package com.faiz.bumiloka

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class quizutama : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quizutama)

        // Tombol Kerjakan (Materi 2 & 3 sebagai contoh tombol yang aktif di layout)
        val btnKerjakan2 = findViewById<Button>(R.id.btn_kerjakan_materi2)
        btnKerjakan2.setOnClickListener {
            val intent = Intent(this, quizsoal::class.java)
            startActivity(intent)
        }

        val btnKerjakan3 = findViewById<Button>(R.id.btn_kerjakan_materi3)
        btnKerjakan3.setOnClickListener {
            val intent = Intent(this, quizsoal::class.java)
            startActivity(intent)
        }

        // Logika Tab navigasi
        val btnTabBelum = findViewById<TextView>(R.id.tab_belum)
        btnTabBelum.setOnClickListener {
            startActivity(Intent(this, quizbelum::class.java))
        }
        
        val btnTabSemua = findViewById<TextView>(R.id.tab_all)
        btnTabSemua.setOnClickListener {
            // Tetap di sini atau refresh logic
        }
    }
}
