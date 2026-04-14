package com.faiz.terraviva

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PengaturanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pengaturan)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val spProv = findViewById<Spinner>(R.id.spProvinsi)
        val spKab = findViewById<Spinner>(R.id.spKabupaten)
        val spKec = findViewById<Spinner>(R.id.spKecamatan)
        val spSek = findViewById<Spinner>(R.id.spSekolah)

        // DATA DUMMY
        val provinsi = listOf("Jawa Tengah", "Jawa Barat")

        val kabupaten = mapOf(
            "Jawa Tengah" to listOf("Cilacap", "Banyumas"),
            "Jawa Barat" to listOf("Bandung", "Bogor")
        )

        val kecamatan = mapOf(
            "Cilacap" to listOf("Adipala", "Maos"),
            "Banyumas" to listOf("Purwokerto", "Ajibarang"),
            "Bandung" to listOf("Coblong", "Lembang"),
            "Bogor" to listOf("Ciawi", "Cisarua")
        )

        val sekolah = mapOf(
            "Adipala" to listOf("SMK 1 Adipala", "SMA 1 Adipala"),
            "Maos" to listOf("SMK Maos"),
            "Purwokerto" to listOf("SMA 1 Purwokerto", "SMA 2 Purwokerto"),
            "Ajibarang" to listOf("SMA 1 Ajibarang"),
            "Coblong" to listOf("SMA 1 Bandung"),
            "Lembang" to listOf("SMA Lembang"),
            "Ciawi" to listOf("SMA Ciawi"),
            "Cisarua" to listOf("SMA Cisarua")
        )

        // SET PROVINSI
        spProv.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, provinsi)

        spProv.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedProv = provinsi[pos]
                val kabList = kabupaten[selectedProv] ?: emptyList()

                spKab.adapter = ArrayAdapter(this@PengaturanActivity,
                    android.R.layout.simple_spinner_dropdown_item, kabList)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spKab.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedKab = spKab.selectedItem?.toString() ?: ""
                val kecList = kecamatan[selectedKab] ?: emptyList()

                spKec.adapter = ArrayAdapter(this@PengaturanActivity,
                    android.R.layout.simple_spinner_dropdown_item, kecList)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spKec.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedKec = spKec.selectedItem?.toString() ?: ""
                val sekList = sekolah[selectedKec] ?: emptyList()

                spSek.adapter = ArrayAdapter(this@PengaturanActivity,
                    android.R.layout.simple_spinner_dropdown_item, sekList)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
