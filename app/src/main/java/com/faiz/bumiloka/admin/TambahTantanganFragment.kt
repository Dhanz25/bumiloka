package com.faiz.bumiloka.admin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.faiz.bumiloka.R
import com.faiz.bumiloka.model.Edukasi
import com.faiz.bumiloka.model.Kuis
import com.faiz.bumiloka.model.Tantangan
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.io.InputStream

class TambahTantanganFragment : Fragment() {

    private val db = FirebaseDatabase.getInstance().reference
    private var editId: String? = null
    private var encodedImageBase64: String? = null

    private lateinit var etJudul: EditText
    private lateinit var etDeskripsi: EditText
    private lateinit var spinnerTipe: AutoCompleteTextView
    private lateinit var layoutTargetCount: View
    private lateinit var etTargetCount: EditText
    private lateinit var spinnerLevel: AutoCompleteTextView
    private lateinit var layoutMateri: View
    private lateinit var spinnerMateri: AutoCompleteTextView
    private lateinit var layoutKuis: View
    private lateinit var spinnerKuis: AutoCompleteTextView
    private lateinit var etImageUrl: EditText
    private lateinit var ivPreview: ImageView
    private lateinit var btnSimpan: Button
    private lateinit var swAktif: com.google.android.material.materialswitch.MaterialSwitch
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView

    private var selectedLevel: Int = 1
    private var selectedMateriId: String = ""
    private var selectedKuisId: String = ""
    private var selectedTipe: String = "SINGLE"
    
    private val allMateri = mutableListOf<Edukasi>()
    private val allKuis = mutableListOf<Kuis>()
    private val filteredMateri = mutableListOf<Edukasi>()
    private val filteredKuis = mutableListOf<Kuis>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let { handleImageSelection(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tambah_tantangan, container, false)

        etJudul = view.findViewById(R.id.et_judul_tantangan)
        etDeskripsi = view.findViewById(R.id.et_deskripsi_tantangan)
        spinnerTipe = view.findViewById(R.id.spinner_tipe_tantangan)
        layoutTargetCount = view.findViewById(R.id.layout_target_count)
        etTargetCount = view.findViewById(R.id.et_target_count)
        spinnerLevel = view.findViewById(R.id.spinner_level_tantangan)
        layoutMateri = view.findViewById(R.id.layout_spinner_materi)
        spinnerMateri = view.findViewById(R.id.spinner_materi_tantangan)
        layoutKuis = view.findViewById(R.id.layout_spinner_kuis)
        spinnerKuis = view.findViewById(R.id.spinner_kuis_tantangan)
        etImageUrl = view.findViewById(R.id.et_image_url_tantangan)
        ivPreview = view.findViewById(R.id.iv_preview_tantangan)
        btnSimpan = view.findViewById(R.id.btn_simpan_tantangan)
        swAktif = view.findViewById(R.id.switch_aktif_tantangan)
        progressBar = view.findViewById(R.id.progress_tambah_tantangan)
        tvTitle = view.findViewById(R.id.tv_title_tambah_tantangan)

        view.findViewById<View>(R.id.btn_select_image).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        setupTipeSpinner()
        setupLevelSpinner()

        arguments?.let { args ->
            editId = args.getString("id")
            if (editId != null) {
                tvTitle.text = "Edit Tantangan"
                btnSimpan.text = "Update Tantangan"
                etJudul.setText(args.getString("judul"))
                etDeskripsi.setText(args.getString("deskripsi"))
                
                selectedTipe = args.getString("type", "SINGLE")
                updateUIBasedOnTipe(selectedTipe)
                etTargetCount.setText(args.getInt("targetCount", 1).toString())

                selectedLevel = args.getInt("level", 1)
                spinnerLevel.setText("Level $selectedLevel", false)
                
                selectedMateriId = args.getString("materiId", "")
                selectedKuisId = args.getString("quizId", "")
                
                val img = args.getString("imageUrl") ?: ""
                if (img.length > 100) {
                    encodedImageBase64 = img
                    etImageUrl.setText("[GAMBAR DARI GALERI]")
                    displayImage(img)
                } else {
                    etImageUrl.setText(img)
                    displayImage(img)
                }
                swAktif.isChecked = args.getBoolean("aktif", true)
            }
        }

        fetchData()

        btnSimpan.setOnClickListener { simpanTantangan() }
        return view
    }

    private fun setupTipeSpinner() {
        val tipeOptions = listOf("SINGLE (1 Materi & 1 Kuis)", "QUIZ_COUNT (Banyak Kuis)", "MATERI_COUNT (Banyak Materi)")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tipeOptions)
        spinnerTipe.setAdapter(adapter)
        spinnerTipe.setOnItemClickListener { _, _, position, _ ->
            selectedTipe = when(position) {
                0 -> "SINGLE"
                1 -> "QUIZ_COUNT"
                2 -> "MATERI_COUNT"
                else -> "SINGLE"
            }
            updateUIBasedOnTipe(selectedTipe)
        }
    }

    private fun updateUIBasedOnTipe(tipe: String) {
        val tipeName = when(tipe) {
            "SINGLE" -> "SINGLE (1 Materi & 1 Kuis)"
            "QUIZ_COUNT" -> "QUIZ_COUNT (Banyak Kuis)"
            "MATERI_COUNT" -> "MATERI_COUNT (Banyak Materi)"
            else -> "SINGLE (1 Materi & 1 Kuis)"
        }
        spinnerTipe.setText(tipeName, false)

        if (tipe == "SINGLE") {
            layoutTargetCount.visibility = View.GONE
            layoutMateri.visibility = View.VISIBLE
            layoutKuis.visibility = View.VISIBLE
        } else {
            layoutTargetCount.visibility = View.VISIBLE
            layoutMateri.visibility = View.GONE
            layoutKuis.visibility = View.GONE
        }
    }

    private fun setupLevelSpinner() {
        val levels = listOf("Level 1", "Level 2", "Level 3")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        spinnerLevel.setAdapter(adapter)
        spinnerLevel.setOnItemClickListener { _, _, position, _ ->
            selectedLevel = position + 1
            selectedMateriId = ""
            selectedKuisId = ""
            spinnerMateri.setText("", false)
            spinnerKuis.setText("", false)
            updateFilteredOptions()
        }
    }

    private fun fetchData() {
        db.child("edukasi").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                allMateri.clear()
                for (child in snapshot.children) {
                    if (child.value is Map<*, *>) {
                        try {
                            child.getValue(Edukasi::class.java)?.let {
                                it.id = child.key ?: ""
                                allMateri.add(it)
                            }
                        } catch (e: Exception) {
                            Log.e("TambahTantangan", "Gagal parse Edukasi: ${child.key}")
                        }
                    }
                }
                updateFilteredOptions()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        db.child("kuis").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                allKuis.clear()
                for (child in snapshot.children) {
                    if (child.value is Map<*, *>) {
                        try {
                            child.getValue(Kuis::class.java)?.let {
                                it.id = child.key ?: ""
                                allKuis.add(it)
                            }
                        } catch (e: Exception) {
                            Log.e("TambahTantangan", "Gagal parse Kuis: ${child.key}")
                        }
                    }
                }
                updateFilteredOptions()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateFilteredOptions() {
        if (!isAdded) return

        filteredMateri.clear()
        filteredMateri.addAll(allMateri.filter { it.level == selectedLevel })
        val materiTitles = mutableListOf("Pilih Materi")
        materiTitles.addAll(filteredMateri.map { it.title })
        spinnerMateri.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, materiTitles))
        
        val currentMateri = filteredMateri.find { it.id == selectedMateriId }
        if (currentMateri != null) spinnerMateri.setText(currentMateri.title, false)

        spinnerMateri.setOnItemClickListener { _, _, position, _ ->
            selectedMateriId = if (position == 0) "" else filteredMateri[position - 1].id
        }

        filteredKuis.clear()
        filteredKuis.addAll(allKuis.filter { it.level == selectedLevel })
        val kuisTitles = mutableListOf("Pilih Kuis")
        kuisTitles.addAll(filteredKuis.map { it.judul })
        spinnerKuis.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, kuisTitles))

        val currentKuis = filteredKuis.find { it.id == selectedKuisId }
        if (currentKuis != null) spinnerKuis.setText(currentKuis.judul, false)

        spinnerKuis.setOnItemClickListener { _, _, position, _ ->
            selectedKuisId = if (position == 0) "" else filteredKuis[position - 1].id
        }
    }

    private fun handleImageSelection(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            encodedImageBase64 = encodeImageToBase64(bitmap)
            etImageUrl.setText("[GAMBAR DARI GALERI]")
            displayImage(encodedImageBase64!!)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        val maxSide = 800
        val scale = Math.min(maxSide.toFloat() / bitmap.width, maxSide.toFloat() / bitmap.height)
        val resized = if (scale < 1f) {
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else bitmap
        resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun displayImage(source: String) {
        if (source.isEmpty()) return
        if (source.length > 100) {
            val imageBytes = Base64.decode(source, Base64.DEFAULT)
            Glide.with(this).asBitmap().load(imageBytes).into(ivPreview)
        } else {
            val resId = resources.getIdentifier(source, "drawable", requireContext().packageName)
            if (resId != 0) ivPreview.setImageResource(resId)
        }
    }

    private fun simpanTantangan() {
        val judul = etJudul.text.toString().trim()
        val deskripsi = etDeskripsi.text.toString().trim()
        val imageInput = etImageUrl.text.toString().trim()
        val targetCount = etTargetCount.text.toString().toIntOrNull() ?: 1
        val finalImageUrl = if (imageInput.startsWith("[")) encodedImageBase64 ?: "" else imageInput

        if (judul.isEmpty()) { etJudul.error = "Wajib diisi"; return }
        if (deskripsi.isEmpty()) { etDeskripsi.error = "Wajib diisi"; return }

        progressBar.visibility = View.VISIBLE
        btnSimpan.isEnabled = false

        val id = editId ?: db.child("tantangan").push().key ?: ""
        val tantangan = Tantangan(
            id = id, judul = judul, deskripsi = deskripsi, imageUrl = finalImageUrl,
            materiId = if (selectedTipe == "SINGLE") selectedMateriId else "", 
            quizId = if (selectedTipe == "SINGLE") selectedKuisId else "", 
            type = selectedTipe, targetCount = targetCount,
            level = selectedLevel, aktif = swAktif.isChecked, createdAt = System.currentTimeMillis()
        )

        db.child("tantangan").child(id).setValue(tantangan).addOnSuccessListener {
            if (isAdded) {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Berhasil disimpan", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }.addOnFailureListener { e ->
            if (isAdded) {
                progressBar.visibility = View.GONE
                btnSimpan.isEnabled = true
                Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
