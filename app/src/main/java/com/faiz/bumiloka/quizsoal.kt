package com.faiz.bumiloka

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class quizsoal : AppCompatActivity() {
    private var skor = 0
    private lateinit var options: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quizsoal)

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        val btnNext = findViewById<Button>(R.id.btnNext)
        
        options = listOf(
            findViewById(R.id.option1),
            findViewById(R.id.option2),
            findViewById(R.id.option3),
            findViewById(R.id.option4)
        )

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Logic sederhana untuk memilih jawaban
        options[0].setOnClickListener { 
            selectOption(0, 100) 
        }
        options[1].setOnClickListener { 
            selectOption(1, 75) 
        }
        options[2].setOnClickListener { 
            selectOption(2, 50) 
        }
        options[3].setOnClickListener { 
            selectOption(3, 25) 
        }

        btnNext.setOnClickListener {
            pindahKeHasil()
        }
    }

    private fun selectOption(index: Int, points: Int) {
        resetOptions()
        skor = points
        // Memberi highlight pada pilihan yang dipilih
        options[index].setBackgroundResource(android.R.color.holo_green_light)
    }

    private fun resetOptions() {
        for (option in options) {
            option.setBackgroundResource(R.drawable.bg_option)
        }
    }

    private fun pindahKeHasil() {
        val destination = when {
            skor >= 100 -> quizmenang1::class.java
            skor >= 50 -> quizmenang2::class.java
            else -> quizselesai::class.java
        }
        
        val intent = Intent(this, destination)
        startActivity(intent)
        finish() // Agar tidak bisa kembali ke soal
    }
}
