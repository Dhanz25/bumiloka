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
import java.io.ByteArrayOutputStream
import java.io.InputStream

class TambahEdukasiFragment : Fragment() {

    private var _binding: FragmentTambahEdukasiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private var editId: String? = null
    
    // Variable to store the actual Base64 data, so we don't show it in the EditText
    private var encodedImageBase64: String? = null

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
        
        // Listener for manual input (drawable name)
        binding.etImageUrl.addTextChangedListener { text ->
            val input = text.toString().trim()
            // If it's NOT our gallery placeholder and looks like a drawable name
            if (input.isNotEmpty() && !input.startsWith("[") && input.length < 100) {
                encodedImageBase64 = null // reset gallery image if user types manually
                displayImageToPreview(input)
            }
        }

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
                
                val imgUrl = args.getString("imageUrl") ?: ""
                if (imgUrl.length > 100) {
                    // It's a Base64 string from gallery
                    encodedImageBase64 = imgUrl
                    binding.etImageUrl.setText("[GAMBAR DARI GALERI]")
                } else {
                    // It's likely a drawable name
                    binding.etImageUrl.setText(imgUrl)
                }
                
                displayImageToPreview(imgUrl)
                
                binding.switchAktif.isChecked = args.getBoolean("aktif", true)
                binding.btnSave.text = "UPDATE MATERI"
            }
        }

        binding.btnSave.setOnClickListener { saveEdukasi() }
    }

    private fun handleImageSelection(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            // Kompres & Encode to Base64
            encodedImageBase64 = encodeImageToBase64(bitmap)
            
            // TAMPILKAN PLACEHOLDER agar tidak panjang di EditText
            binding.etImageUrl.setText("[GAMBAR DARI GALERI]")
            
            // Tampilkan preview gambarnya
            displayImageToPreview(encodedImageBase64!!)
            
            binding.tvImageInfo.text = "Gambar Galeri Terpilih"
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Resize agar tidak terlalu berat disimpan di Firebase
        val maxSide = 600
        val scale = Math.min(maxSide.toFloat() / bitmap.width, maxSide.toFloat() / bitmap.height)
        val resized = if (scale < 1f) {
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else bitmap
        
        resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        // Gunakan NO_WRAP agar tidak ada newline yang merusak string
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun displayImageToPreview(source: String) {
        if (source.isEmpty()) {
            binding.ivPreview.setImageResource(R.drawable.img_lingkungan)
            return
        }
        
        try {
            if (source.length > 100) { // Base64
                val imageBytes = Base64.decode(source, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.ivPreview.setImageBitmap(decodedImage)
            } else { // Drawable Name
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
        
        // Tentukan nilai imageUrl yang akan disimpan ke DB
        val finalImageUrl = if (imageInputText.startsWith("[")) {
            encodedImageBase64 ?: "" // Gunakan data Base64 asli jika di input ada placeholder
        } else {
            imageInputText // Gunakan teks manual jika diketik nama drawable
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
            // Fallback legacy
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
