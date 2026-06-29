package com.faiz.bumiloka.admin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.faiz.bumiloka.R
import com.faiz.bumiloka.databinding.FragmentTambahEdukasiBinding
import com.faiz.bumiloka.model.Edukasi
import com.faiz.bumiloka.model.Kuis
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream
import java.io.InputStream

class TambahEdukasiFragment : Fragment() {

    private var _binding: FragmentTambahEdukasiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private var editId: String? = null
    
    private var encodedImageBase64: String? = null
    
    private var selectedKuisId: String = ""
    private val kuisList = mutableListOf<Kuis>()

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleImageSelection(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahEdukasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Setup Dropdown Level
        val levels = arrayOf("1", "2", "3")
        val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        binding.spinnerLevel.setAdapter(levelAdapter)

        binding.btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        
        binding.etImageUrl.addTextChangedListener { text ->
            val input = text.toString().trim()
            if (input.isNotEmpty() && !input.startsWith("[") && input.length < 100) {
                encodedImageBase64 = null
                displayImageToPreview(input)
            }
        }

        fetchKuisList()

        arguments?.let { args ->
            editId = args.getString("id")
            if (editId != null) {
                binding.tvToolbarTitle.text = "Edit Materi"
                binding.spinnerLevel.setText(args.getInt("level", 1).toString(), false)
                binding.etTitle.setText(args.getString("title"))
                binding.etDescription.setText(args.getString("description"))
                
                binding.etSection1Title.setText(args.getString("section1Title"))
                binding.etSection1Content.setText(args.getString("section1Content"))
                binding.etSection2Title.setText(args.getString("section2Title"))
                binding.etSection2Content.setText(args.getString("section2Content"))
                binding.etSection3Title.setText(args.getString("section3Title"))
                binding.etSection3Content.setText(args.getString("section3Content"))
                
                selectedKuisId = args.getString("kuisId") ?: ""
                
                val imgUrl = args.getString("imageUrl") ?: ""
                if (imgUrl.length > 100) {
                    encodedImageBase64 = imgUrl
                    binding.etImageUrl.setText("[GAMBAR DARI GALERI]")
                } else {
                    binding.etImageUrl.setText(imgUrl)
                }
                
                displayImageToPreview(imgUrl)
                
                binding.switchAktif.isChecked = args.getBoolean("aktif", true)
                binding.btnSave.text = "UPDATE MATERI"
            }
        }

        binding.btnSave.setOnClickListener { saveEdukasi() }
    }

    private fun fetchKuisList() {
        FirebaseDatabase.getInstance().reference.child("kuis")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    kuisList.clear()
                    val titles = mutableListOf<String>()
                    titles.add("Tanpa Kuis") // Opsi jika tidak ada kuis
                    
                    for (child in snapshot.children) {
                        // Perbaikan: Cek apakah data benar-benar bertipe Map (Object), bukan String legacy
                        if (child.value is Map<*, *>) {
                            try {
                                child.getValue(Kuis::class.java)?.let {
                                    it.id = child.key ?: ""
                                    kuisList.add(it)
                                    titles.add(it.judul)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, titles)
                    binding.spinnerKuis.setAdapter(adapter)
                    
                    // Set pilihan saat ini jika mode edit
                    if (selectedKuisId.isNotEmpty()) {
                        val currentKuis = kuisList.find { it.id == selectedKuisId }
                        currentKuis?.let { binding.spinnerKuis.setText(it.judul, false) }
                    } else {
                        binding.spinnerKuis.setText("Tanpa Kuis", false)
                    }

                    binding.spinnerKuis.setOnItemClickListener { _, _, position, _ ->
                        selectedKuisId = if (position == 0) "" else kuisList[position - 1].id
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
            binding.etImageUrl.setText("[GAMBAR DARI GALERI]")
            displayImageToPreview(encodedImageBase64!!)
            binding.tvImageInfo.text = "Gambar Galeri Terpilih"
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        val maxSide = 600
        val scale = Math.min(maxSide.toFloat() / bitmap.width, maxSide.toFloat() / bitmap.height)
        val resized = if (scale < 1f) {
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else bitmap
        
        resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun displayImageToPreview(source: String) {
        if (source.isEmpty()) {
            binding.ivPreview.setImageResource(R.drawable.img_lingkungan)
            return
        }
        
        try {
            if (source.length > 100) {
                val imageBytes = Base64.decode(source, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.ivPreview.setImageBitmap(decodedImage)
            } else {
                if (source.startsWith("[")) return
                val resId = resources.getIdentifier(source, "drawable", requireContext().packageName)
                if (resId != 0) {
                    binding.ivPreview.setImageResource(resId)
                } else {
                    binding.ivPreview.setImageResource(R.drawable.img_lingkungan)
                }
            }
        } catch (e: Exception) {
            binding.ivPreview.setImageResource(R.drawable.img_lingkungan)
        }
    }

    private fun saveEdukasi() {
        val levelStr = binding.spinnerLevel.text.toString().trim()
        val title = binding.etTitle.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()
        val imageInputText = binding.etImageUrl.text.toString().trim()
        
        val finalImageUrl = if (imageInputText.startsWith("[")) {
            encodedImageBase64 ?: ""
        } else {
            imageInputText
        }

        if (levelStr.isEmpty() || title.isEmpty() || finalImageUrl.isEmpty()) {
            Toast.makeText(requireContext(), "Harap lengkapi semua data dan gambar", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        val s1Title = binding.etSection1Title.text.toString().trim()
        val s1Content = binding.etSection1Content.text.toString().trim()
        val s2Title = binding.etSection2Title.text.toString().trim()
        val s2Content = binding.etSection2Content.text.toString().trim()
        val s3Title = binding.etSection3Title.text.toString().trim()
        val s3Content = binding.etSection3Content.text.toString().trim()

        val edukasi = Edukasi(
            id = editId ?: "",
            kuisId = selectedKuisId,
            level = levelStr.toInt(),
            title = title,
            description = desc,
            imageUrl = finalImageUrl,
            section1Title = s1Title,
            section1Content = s1Content,
            section2Title = s2Title,
            section2Content = s2Content,
            section3Title = s3Title,
            section3Content = s3Content,
            isiTitle = s1Title,
            content = s1Content,
            pentingTitle = s2Title,
            pentingContent = s2Content,
            contohTitle = s3Title,
            contohContent = s3Content,
            aktif = binding.switchAktif.isChecked,
            createdAt = if (editId == null) System.currentTimeMillis() else arguments?.getLong("createdAt") ?: System.currentTimeMillis()
        )

        viewModel.saveEdukasi(edukasi) { success ->
            if (!isAdded) return@saveEdukasi
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true
            if (success) {
                Toast.makeText(requireContext(), "Materi berhasil disimpan", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Gagal menyimpan materi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
