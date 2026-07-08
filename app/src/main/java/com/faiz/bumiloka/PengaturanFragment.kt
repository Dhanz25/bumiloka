package com.faiz.bumiloka

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.faiz.bumiloka.model.Wilayah
import com.faiz.bumiloka.network.ApiService
import com.faiz.bumiloka.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch

class PengaturanFragment : Fragment() {

    private lateinit var api: ApiService
    private var provinsiList: List<Wilayah> = listOf()
    private var kabupatenList: List<Wilayah> = listOf()
    private var kecamatanList: List<Wilayah> = listOf()

    private var isEditMode = false 
    private var selectedProv = ""
    private var selectedKab = ""
    private var selectedKec = ""
    private var selectedSek = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pengaturan, container, false)
        api = RetrofitClient.instance
        
        initViews(view)
        loadUserData(view)
        toggleInput(view, false) 
        
        return view
    }

    private fun initViews(view: View) {
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpan)
        val btnBatal = view.findViewById<Button>(R.id.btnBatal)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val formOverlay = view.findViewById<View>(R.id.formOverlay)
        val spProv = view.findViewById<Spinner>(R.id.spProvinsi)
        val spKab = view.findViewById<Spinner>(R.id.spKabupaten)
        val spKec = view.findViewById<Spinner>(R.id.spKecamatan)
        val spSek = view.findViewById<Spinner>(R.id.spSekolah)
        val spAgama = view.findViewById<Spinner>(R.id.spAgama)

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        formOverlay.setOnClickListener { showEditPromptDialog() }

        val listAgama = listOf("Islam", "Kristen", "Katolik", "Hindu", "Budha", "Khonghucu")
        val adapterAgama = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listAgama)
        adapterAgama.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spAgama.adapter = adapterAgama

        // Listener Spinner Berantai
        spProv.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                if (isEditMode && provinsiList.isNotEmpty()) {
                    loadKabupaten(provinsiList[position].id)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spKab.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                if (isEditMode && kabupatenList.isNotEmpty()) {
                    loadKecamatan(kabupatenList[position].id)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spKec.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                if (isEditMode && kecamatanList.isNotEmpty()) {
                    loadSekolah(kecamatanList[position].name)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnSimpan.setOnClickListener {
            if (!isEditMode) enableEditMode(view, btnSimpan, btnBatal, spProv)
            else saveDataToFirebase(view, btnSimpan, btnBatal)
        }

        btnBatal.setOnClickListener {
            isEditMode = false
            btnSimpan.text = "Ubah Profil"
            btnBatal.visibility = View.GONE
            toggleInput(view, false)
            loadUserData(view)
        }
    }

    private fun loadUserData(view: View) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(user.uid)

        db.get().addOnSuccessListener { snapshot ->
            if (!isAdded || !snapshot.exists()) return@addOnSuccessListener
            
            try {
                val etNama = view.findViewById<EditText>(R.id.etNama)
                val etNis = view.findViewById<EditText>(R.id.etNis)
                val etUmur = view.findViewById<EditText>(R.id.etUmur)

                etNama?.setText(snapshot.child("name").value?.toString() ?: user.displayName)
                etNis?.setText(snapshot.child("nis").value?.toString() ?: "")
                etUmur?.setText(snapshot.child("umur").value?.toString() ?: "")

                selectedProv = snapshot.child("provinsi").value?.toString() ?: ""
                selectedKab = snapshot.child("kabupaten").value?.toString() ?: ""
                selectedKec = snapshot.child("kecamatan").value?.toString() ?: ""
                selectedSek = snapshot.child("sekolah").value?.toString() ?: ""

                setupStaticSpinner(view.findViewById(R.id.spProvinsi), selectedProv)
                setupStaticSpinner(view.findViewById(R.id.spKabupaten), selectedKab)
                setupStaticSpinner(view.findViewById(R.id.spKecamatan), selectedKec)
                setupStaticSpinner(view.findViewById(R.id.spSekolah), selectedSek)

                val gender = snapshot.child("jenisKelamin").value?.toString() ?: ""
                if (gender == "Laki-laki") view.findViewById<RadioButton>(R.id.rbLaki)?.isChecked = true
                else if (gender == "Perempuan") view.findViewById<RadioButton>(R.id.rbPerempuan)?.isChecked = true

                setSpinnerValue(view.findViewById(R.id.spAgama), snapshot.child("agama").value?.toString() ?: "")
            } catch (e: Exception) { Log.e("FIX", "Error load data: ${e.message}") }
        }
    }

    private fun toggleInput(view: View, enabled: Boolean) {
        if (!isAdded) return
        view.findViewById<View>(R.id.formOverlay)?.visibility = if (enabled) View.GONE else View.VISIBLE
        val components = listOf(R.id.etNama, R.id.etNis, R.id.etUmur, R.id.spProvinsi, R.id.spKabupaten, R.id.spKecamatan, R.id.spSekolah, R.id.spAgama, R.id.rbLaki, R.id.rbPerempuan)
        components.forEach { view.findViewById<View>(it)?.isEnabled = enabled }
    }

    private fun loadProvinsiApi(spProv: Spinner) {
        lifecycleScope.launch {
            try {
                provinsiList = api.getProvinsi()
                if (!isAdded) return@launch
                val names = provinsiList.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
                spProv.adapter = adapter
                
                val index = names.indexOf(selectedProv)
                if (index >= 0) {
                    spProv.setSelection(index)
                    loadKabupaten(provinsiList[index].id)
                }
            } catch (e: Exception) { Log.e("API", "Error Prov: ${e.message}") }
        }
    }

    private fun loadKabupaten(provId: String) {
        val spKab = view?.findViewById<Spinner>(R.id.spKabupaten) ?: return
        lifecycleScope.launch {
            try {
                kabupatenList = api.getKabupaten(provId)
                if (!isAdded) return@launch
                val names = kabupatenList.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
                spKab.adapter = adapter
                
                val index = names.indexOf(selectedKab)
                if (index >= 0) {
                    spKab.setSelection(index)
                    loadKecamatan(kabupatenList[index].id)
                }
            } catch (e: Exception) { Log.e("API", "Error Kab: ${e.message}") }
        }
    }

    private fun loadKecamatan(kabId: String) {
        val spKec = view?.findViewById<Spinner>(R.id.spKecamatan) ?: return
        lifecycleScope.launch {
            try {
                kecamatanList = api.getKecamatan(kabId)
                if (!isAdded) return@launch
                val names = kecamatanList.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
                spKec.adapter = adapter
                
                val index = names.indexOf(selectedKec)
                if (index >= 0) {
                    spKec.setSelection(index)
                    loadSekolah(kecamatanList[index].name)
                }
            } catch (e: Exception) { Log.e("API", "Error Kec: ${e.message}") }
        }
    }

    private fun loadSekolah(namaKecamatan: String) {
        val spSek = view?.findViewById<Spinner>(R.id.spSekolah) ?: return
        val mapKecamatanToId = mapOf(
            "LUMBIR" to "330201", "WANGON" to "330202", "JATILAWANG" to "330203",
            "RAWALO" to "330204", "KEBASEN" to "330205", "KEMRANJEN" to "330206",
            "SUMPIUH" to "330207", "TAMBAK" to "330208", "SOMAGEDE" to "330209",
            "KALIBAGOR" to "330210", "BANYUMAS" to "330211", "PATIKRAJA" to "330212",
            "PURWOJATI" to "330213", "AJIBARANG" to "330214", "GUMELAR" to "330215",
            "PEKUNCEN" to "330216", "CILONGOK" to "330217", "KARANGLEWAS" to "330218",
            "KEDUNGBANTENG" to "330219", "KEDUNG BANTENG" to "330219",
            "BATURADEN" to "330220", "BATURRADEN" to "330220",
            "SUMBANG" to "330221", "KEMBARAN" to "330222", "SOKARAJA" to "330223",
            "PURWOKERTO SELATAN" to "330224", "PURWOKERTO BARAT" to "330225",
            "PURWOKERTO TIMUR" to "330226", "PURWOKERTO UTARA" to "330227"
        )

        val idFirebase = mapKecamatanToId[namaKecamatan.uppercase()] ?: namaKecamatan
        val db = FirebaseDatabase.getInstance().reference.child("sekolah").child(idFirebase)

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                val list = mutableListOf<String>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) { list.add(child.value.toString()) }
                } else {
                    list.add("Lainnya / Tidak terdaftar")
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, list)
                spSek.adapter = adapter
                val index = list.indexOf(selectedSek)
                if (index >= 0) spSek.setSelection(index)
            }
            override fun onCancelled(error: DatabaseError) { Log.e("FIREBASE", error.message) }
        })
    }

    private fun setupStaticSpinner(spinner: Spinner?, value: String) {
        if (spinner != null && isAdded && value.isNotEmpty()) {
            spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf(value))
        }
    }

    private fun setSpinnerValue(spinner: Spinner?, value: String) {
        val adapter = spinner?.adapter ?: return
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == value) {
                spinner.setSelection(i)
                break
            }
        }
    }

    private fun enableEditMode(view: View, btnSimpan: Button, btnBatal: Button, spProv: Spinner) {
        isEditMode = true
        btnSimpan.text = "Simpan Perubahan"
        btnBatal.visibility = View.VISIBLE
        toggleInput(view, true)
        loadProvinsiApi(spProv)
    }

    private fun showEditPromptDialog() {
        if (!isAdded) return
        AlertDialog.Builder(requireContext()).setTitle("Mode Baca").setMessage("Klik 'Ubah Profil' untuk mengedit.").setPositiveButton("OK", null).show()
    }

    private fun saveDataToFirebase(view: View, btnSimpan: Button, btnBatal: Button) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference

        val nama = view.findViewById<EditText>(R.id.etNama)?.text.toString().trim()
        val nis = view.findViewById<EditText>(R.id.etNis)?.text.toString().trim()
        val umur = view.findViewById<EditText>(R.id.etUmur)?.text.toString().trim()
        
        val prov = view.findViewById<Spinner>(R.id.spProvinsi)?.selectedItem?.toString() ?: ""
        val kab = view.findViewById<Spinner>(R.id.spKabupaten)?.selectedItem?.toString() ?: ""
        val kec = view.findViewById<Spinner>(R.id.spKecamatan)?.selectedItem?.toString() ?: ""
        val sek = view.findViewById<Spinner>(R.id.spSekolah)?.selectedItem?.toString() ?: ""
        val agama = view.findViewById<Spinner>(R.id.spAgama)?.selectedItem?.toString() ?: ""
        
        val rbLaki = view.findViewById<RadioButton>(R.id.rbLaki)
        val gender = if (rbLaki?.isChecked == true) "Laki-laki" else "Perempuan"

        if (nama.isEmpty() || nis.length != 10) {
            Toast.makeText(context, "Nama wajib diisi & NIS harus 10 digit", Toast.LENGTH_SHORT).show()
            return
        }

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
            "isProfileComplete" to true
        )

        db.child("users").child(userId).updateChildren(userData)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                Toast.makeText(context, "Profil Berhasil Diperbarui!", Toast.LENGTH_SHORT).show()
                isEditMode = false
                btnSimpan.text = "Ubah Profil"
                btnBatal.visibility = View.GONE
                toggleInput(view, false)
                loadUserData(view)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal menyimpan: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
