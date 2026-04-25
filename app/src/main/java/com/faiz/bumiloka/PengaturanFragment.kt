package com.faiz.bumiloka

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.faiz.bumiloka.model.Wilayah
import com.faiz.bumiloka.network.ApiService
import com.faiz.bumiloka.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class PengaturanFragment : Fragment() {

    private lateinit var api: ApiService
    private var provinsiList: List<Wilayah> = listOf()
    private var kabupatenList: List<Wilayah> = listOf()
    private var kecamatanList: List<Wilayah> = listOf()

    private var isEditMode = false // Status mode Edit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pengaturan, container, false)
        api = RetrofitClient.instance

        // Inisialisasi View
        val spProv = view.findViewById<Spinner>(R.id.spProvinsi)
        val spKab = view.findViewById<Spinner>(R.id.spKabupaten)
        val spKec = view.findViewById<Spinner>(R.id.spKecamatan)
        val spSek = view.findViewById<Spinner>(R.id.spSekolah)
        val spAgama = view.findViewById<Spinner>(R.id.spAgama)
        val etUmur = view.findViewById<EditText>(R.id.etUmur)
        val etNama = view.findViewById<EditText>(R.id.etNama)
        val etNis = view.findViewById<EditText>(R.id.etNis)
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpan)

        // 1. Setup Dropdown Agama
        val listAgama = listOf("Islam", "Kristen", "Katolik", "Hindu", "Budha", "Khonghucu")
        val adapterAgama = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listAgama)
        adapterAgama.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spAgama.adapter = adapterAgama

        // --- LOAD DATA AWAL ---
        loadUserData(view)
        toggleInput(view, false) // Kunci input saat pertama masuk
        btnSimpan.text = "Ubah Profil"

        // --- LISTENERS WILAYAH ---
        spProv.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                if (isEditMode && provinsiList.isNotEmpty()) {
                    loadKabupaten(provinsiList[pos].id)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spKab.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                if (isEditMode && kabupatenList.isNotEmpty()) {
                    loadKecamatan(kabupatenList[pos].id)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spKec.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                if (isEditMode && kecamatanList.isNotEmpty()) {
                    loadSekolah(kecamatanList[pos].id)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // --- TOMBOL TOGGLE (UBAH/SIMPAN) ---
        btnSimpan.setOnClickListener {
            if (!isEditMode) {
                isEditMode = true
                btnSimpan.text = "Simpan Perubahan"
                toggleInput(view, true)
                loadProvinsiApi(spProv)
            } else {
                saveDataToFirebase(view, btnSimpan)
            }
        }

        return view
    }

    private fun toggleInput(view: View, enabled: Boolean) {
        try {
            view.findViewById<EditText>(R.id.etNama)?.isEnabled = enabled
            view.findViewById<EditText>(R.id.etNis)?.isEnabled = enabled
            view.findViewById<EditText>(R.id.etUmur)?.isEnabled = enabled
            view.findViewById<Spinner>(R.id.spProvinsi)?.isEnabled = enabled
            view.findViewById<Spinner>(R.id.spKabupaten)?.isEnabled = enabled
            view.findViewById<Spinner>(R.id.spKecamatan)?.isEnabled = enabled
            view.findViewById<Spinner>(R.id.spSekolah)?.isEnabled = enabled
            view.findViewById<Spinner>(R.id.spAgama)?.isEnabled = enabled
            view.findViewById<RadioButton>(R.id.rbLaki)?.isEnabled = enabled
            view.findViewById<RadioButton>(R.id.rbPerempuan)?.isEnabled = enabled
        } catch (e: Exception) {
            Log.e("FIX", "Error toggleInput: ${e.message}")
        }
    }

    private fun loadProvinsiApi(spProv: Spinner) {
        lifecycleScope.launch {
            try {
                provinsiList = api.getProvinsi()
                val names = provinsiList.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
                spProv.adapter = adapter
            } catch (e: Exception) {
                Log.e("API", "Gagal load provinsi: ${e.message}")
            }
        }
    }

    private fun loadUserData(view: View) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(user.uid)

        db.get().addOnSuccessListener { snapshot ->
            if (!isAdded || view == null) return@addOnSuccessListener

            if (snapshot.exists()) {
                val etNama = view.findViewById<EditText>(R.id.etNama)
                val etNis = view.findViewById<EditText>(R.id.etNis)
                val etUmur = view.findViewById<EditText>(R.id.etUmur)

                val namaFb = snapshot.child("name").value.toString()
                etNama?.setText(if (namaFb == "null" || namaFb.isEmpty()) user.displayName else namaFb)
                etNis?.setText(snapshot.child("nis").value.toString())
                etUmur?.setText(snapshot.child("umur").value.toString())

                setupStaticSpinner(view.findViewById(R.id.spProvinsi), snapshot.child("provinsi").value.toString())
                setupStaticSpinner(view.findViewById(R.id.spKabupaten), snapshot.child("kabupaten").value.toString())
                setupStaticSpinner(view.findViewById(R.id.spKecamatan), snapshot.child("kecamatan").value.toString())
                setupStaticSpinner(view.findViewById(R.id.spSekolah), snapshot.child("sekolah").value.toString())

                val gender = snapshot.child("jenisKelamin").value.toString()
                if (gender == "Laki-laki") view.findViewById<RadioButton>(R.id.rbLaki)?.isChecked = true
                else if (gender == "Perempuan") view.findViewById<RadioButton>(R.id.rbPerempuan)?.isChecked = true

                setSpinnerValue(view.findViewById(R.id.spAgama), snapshot.child("agama").value.toString())
            } else {
                view.findViewById<EditText>(R.id.etNama)?.setText(user.displayName)
            }
        }
    }

    private fun setupStaticSpinner(spinner: Spinner, value: String) {
        if (value != "null" && value.isNotEmpty()) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf(value))
            spinner.adapter = adapter
        }
    }

    private fun setSpinnerValue(spinner: Spinner, value: String) {
        val adapter = spinner.adapter ?: return
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == value) {
                spinner.setSelection(i)
                break
            }
        }
    }

    private fun saveDataToFirebase(view: View, button: Button) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid // Mengambil UID unik dari akun yang sedang login

        if (userId == null) {
            Toast.makeText(context, "User tidak terdeteksi, silakan login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseDatabase.getInstance().reference

        // Ambil Data dari View & Trim
        val nama = view.findViewById<EditText>(R.id.etNama)?.text.toString().trim()
        val nis = view.findViewById<EditText>(R.id.etNis)?.text.toString().trim()
        val umur = view.findViewById<EditText>(R.id.etUmur)?.text.toString().trim()
        val prov = view.findViewById<Spinner>(R.id.spProvinsi)?.selectedItem?.toString().orEmpty()
        val kab = view.findViewById<Spinner>(R.id.spKabupaten)?.selectedItem?.toString().orEmpty()
        val kec = view.findViewById<Spinner>(R.id.spKecamatan)?.selectedItem?.toString().orEmpty()
        val sek = view.findViewById<Spinner>(R.id.spSekolah)?.selectedItem?.toString().orEmpty()
        val agama = view.findViewById<Spinner>(R.id.spAgama)?.selectedItem?.toString().orEmpty()

        val rgGender = view.findViewById<RadioGroup>(R.id.rgGender)
        val gender = if (rgGender != null && rgGender.checkedRadioButtonId != -1) {
            view.findViewById<RadioButton>(rgGender.checkedRadioButtonId).text.toString()
        } else ""

        // --- VALIDASI 1: SEMUA WAJIB DIISI ---
        if (nama.isEmpty() || nis.isEmpty() || umur.isEmpty() || gender.isEmpty() ||
            prov.isEmpty() || kab.isEmpty() || kec.isEmpty() || sek.isEmpty() || agama.isEmpty()) {

            Toast.makeText(context, "Semua data wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        // --- VALIDASI 2: NIS WAJIB 10 DIGIT ---
        if (nis.length != 10) {
            val etNis = view.findViewById<EditText>(R.id.etNis)
            etNis?.error = "NIS harus tepat 10 digit"
            Toast.makeText(context, "NIS tidak valid (harus 10 digit)", Toast.LENGTH_SHORT).show()
            return
        }

        // --- PROSES SIMPAN KE FIREBASE ---
        val userData = mapOf(
            "name" to nama,
            "nis" to nis,
            "umur" to umur,
            "provinsi" to prov,
            "kabupaten" to kab,
            "kecamatan" to kec,
            "sekolah" to sek,
            "agama" to agama,
            "jenisKelamin" to gender,
            "isProfileComplete" to true // Boolean murni agar HomeFragment mengenalinya
        )

        // Menuju folder: users -> [UID_USER_LOGIN]
        db.child("users").child(userId).setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(context, "Profil Berhasil Diperbarui!", Toast.LENGTH_SHORT).show()

                // Kembalikan ke mode 'Lihat' (Kunci input)
                isEditMode = false
                button.text = "Ubah Profil"
                toggleInput(view, false)
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE", "Gagal simpan: ${e.message}")
                Toast.makeText(context, "Gagal simpan: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadKabupaten(provId: String) {
        val spKab = view?.findViewById<Spinner>(R.id.spKabupaten) ?: return
        lifecycleScope.launch {
            try {
                kabupatenList = api.getKabupaten(provId)
                val names = kabupatenList.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
                spKab.adapter = adapter
            } catch (e: Exception) {
                Log.e("API", "Error Kab: ${e.message}")
            }
        }
    }

    private fun loadKecamatan(kabId: String) {
        val spKec = view?.findViewById<Spinner>(R.id.spKecamatan) ?: return
        lifecycleScope.launch {
            try {
                kecamatanList = api.getKecamatan(kabId)
                val names = kecamatanList.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
                spKec.adapter = adapter
            } catch (e: Exception) {
                Log.e("API", "Error Kec: ${e.message}")
            }
        }
    }

    private fun loadSekolah(idKecamatan: String) {
        val spSek = view?.findViewById<Spinner>(R.id.spSekolah) ?: return
        val db = FirebaseDatabase.getInstance().reference.child("sekolah").child(idKecamatan.trim())

        db.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                if (!isAdded) return
                val list = mutableListOf<String>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) list.add(child.value.toString())
                } else {
                    list.add("Lainnya / Tidak terdaftar")
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, list)
                spSek.adapter = adapter
            }
            override fun onCancelled(p0: com.google.firebase.database.DatabaseError) {}
        })
    }
}