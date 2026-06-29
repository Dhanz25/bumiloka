package com.faiz.bumiloka.admin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
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
    private lateinit var spinnerMateri: AutoCompleteTextView
    private lateinit var spinnerKuis: AutoCompleteTextView
    private lateinit var etImageUrl: EditText
    private lateinit var ivPreview: ImageView
    private lateinit var btnSimpan: Button
    private lateinit var swAktif: com.google.android.material.materialswitch.MaterialSwitch
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView

    private var selectedMateriId: String = ""
    private var selectedKuisId: String = ""
    private val listMateri = mutableListOf<Edukasi>()
    private val listKuis = mutableListOf<Kuis>()

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
        spinnerMateri = view.findViewById(R.id.spinner_materi_tantangan)
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

        arguments?.let { args ->
            editId = args.getString("id")
            if (editId != null) {
                tvTitle.text = "Edit Tantangan"
                btnSimpan.text = "Update Tantangan"
                etJudul.setText(args.getString("judul"))
                etDeskripsi.setText(args.getString("deskripsi"))
                
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

        fetchMateriAndKuis()

        btnSimpan.setOnClickListener { simpanTantangan() }
        return view
    }

    private fun fetchMateriAndKuis() {
        // Fetch Materi
        db.child("edukasi").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                listMateri.clear()
                val materiTitles = mutableListOf<String>()
                materiTitles.add("Pilih Materi")
                
                var selectedPosition = 0
                for (child in snapshot.children) {
                    // Check if data is an object before converting
                    if (child.value is Map<*, *>) {
                        try {
                            child.getValue(Edukasi::class.java)?.let {
                                it.id = child.key ?: ""
                                listMateri.add(it)
                                materiTitles.add(it.title)
                                if (it.id == selectedMateriId) {
                                    selectedPosition = listMateri.size
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, materiTitles)
                spinnerMateri.setAdapter(adapter)
                
                if (selectedPosition > 0) {
                    spinnerMateri.setText(materiTitles[selectedPosition], false)
                }

                spinnerMateri.setOnItemClickListener { _, _, position, _ ->
                    selectedMateriId = if (position == 0) "" else listMateri[position - 1].id
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Fetch Kuis
        db.child("kuis").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                listKuis.clear()
                val kuisTitles = mutableListOf<String>()
                kuisTitles.add("Pilih Kuis")

                var selectedPosition = 0
                for (child in snapshot.children) {
                    // Check if data is an object before converting
                    if (child.value is Map<*, *>) {
                        try {
                            child.getValue(Kuis::class.java)?.let {
                                it.id = child.key ?: ""
                                listKuis.add(it)
                                kuisTitles.add(it.judul)
                                if (it.id == selectedKuisId) {
                                    selectedPosition = listKuis.size
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, kuisTitles)
                spinnerKuis.setAdapter(adapter)

                if (selectedPosition > 0) {
                    spinnerKuis.setText(kuisTitles[selectedPosition], false)
                }

                spinnerKuis.setOnItemClickListener { _, _, position, _ ->
                    selectedKuisId = if (position == 0) "" else listKuis[position - 1].id
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
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
        val finalImageUrl = if (imageInput.startsWith("[")) encodedImageBase64 ?: "" else imageInput

        if (judul.isEmpty()) { etJudul.error = "Judul wajib diisi"; return }
        if (deskripsi.isEmpty()) { etDeskripsi.error = "Deskripsi wajib diisi"; return }
        if (selectedMateriId.isEmpty()) { Toast.makeText(requireContext(), "Pilih Materi!", Toast.LENGTH_SHORT).show(); return }
        if (selectedKuisId.isEmpty()) { Toast.makeText(requireContext(), "Pilih Kuis!", Toast.LENGTH_SHORT).show(); return }

        progressBar.visibility = View.VISIBLE
        btnSimpan.isEnabled = false

        val id = editId ?: db.child("tantangan").push().key ?: ""
        
        val tantangan = Tantangan(
            id = id,
            judul = judul,
            deskripsi = deskripsi,
            imageUrl = finalImageUrl,
            badgeId = "",
            materiId = selectedMateriId,
            quizId = selectedKuisId,
            aktif = swAktif.isChecked,
            createdAt = if (editId == null) System.currentTimeMillis() else arguments?.getLong("createdAt") ?: System.currentTimeMillis()
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