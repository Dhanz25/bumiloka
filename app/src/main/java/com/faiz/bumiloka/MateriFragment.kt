package com.faiz.bumiloka

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.model.Edukasi
import com.faiz.bumiloka.model.Kuis
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.bumptech.glide.Glide

class MateriFragment : Fragment() {

    private var edukasiId: String? = null
    private var linkedKuisId: String? = null
    private var isTantanganBonus = false
    private var challengeId = ""
    private var quizIdFromTantangan = ""
    private var badgeIdFromTantangan = ""
    
    private lateinit var btnMulaiKuis: Button
    private var countDownTimer: CountDownTimer? = null
    
    private var hasBeenPromptedToRead = false

    companion object {
        private const val ARG_MATERI_ID = "materi_id"
        private const val ARG_EDUKASI_ID = "edukasi_id"

        @JvmStatic
        fun newInstance(materiId: Int): MateriFragment = MateriFragment().apply {
            arguments = Bundle().apply { putInt(ARG_MATERI_ID, materiId) }
        }

        @JvmStatic
        fun newInstance(edukasiId: String): MateriFragment = MateriFragment().apply {
            arguments = Bundle().apply { putString(ARG_EDUKASI_ID, edukasiId) }
        }

        @JvmStatic fun newInstanceLegacy(materiId: Int) = newInstance(materiId)
        
        @JvmStatic fun newInstanceForChallenge(materiId: String) = newInstance(materiId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_materi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        btnMulaiKuis = view.findViewById(R.id.btnMulaiKuis)
        
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        arguments?.let {
            edukasiId = it.getString(ARG_EDUKASI_ID)
            val legacyId = it.getInt(ARG_MATERI_ID, -1)
            
            challengeId = it.getString("challenge_id") ?: ""
            quizIdFromTantangan = it.getString("quiz_id", "")
            badgeIdFromTantangan = it.getString("badge_id", "")
            isTantanganBonus = it.getBoolean("IS_TANTANGAN_BONUS", false)
            
            if (edukasiId != null) {
                loadMateriFromFirebase(edukasiId!!)
                findLinkedKuis(edukasiId!!)
            } else if (legacyId != -1) {
                loadLegacyMateri(legacyId)
                updateButtonStatus()
            }
        }
    }

    private fun findLinkedKuis(edukasiId: String) {
        // Jika sudah ada quizId dari tantangan, gunakan itu
        if (quizIdFromTantangan.isNotEmpty()) {
            linkedKuisId = quizIdFromTantangan
            updateButtonStatus()
            return
        }

        FirebaseDatabase.getInstance().reference.child("kuis")
            .orderByChild("edukasiId").equalTo(edukasiId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    for (child in snapshot.children) {
                        linkedKuisId = child.key
                        break
                    }
                    updateButtonStatus()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun updateButtonStatus() {
        if (!isAdded) return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val materiIndex = arguments?.getInt(ARG_MATERI_ID, -1)

        LevelHelper.getCurrentLevel(requireContext()) { level ->
            val prefKuis = requireActivity().getSharedPreferences("KUIS_${userId}_LEVEL_$level", Context.MODE_PRIVATE)
            
            val isSelesai = if (linkedKuisId != null) {
                prefKuis.getBoolean("kuis_${linkedKuisId}_selesai", false) || 
                TantanganStatusHelper.isTantanganSelesai(requireContext(), challengeId)
            } else {
                when (materiIndex) {
                    1 -> prefKuis.getBoolean("materi1_selesai", false)
                    2 -> prefKuis.getBoolean("quiz2_selesai", false)
                    3 -> prefKuis.getBoolean("quiz3_selesai", false)
                    else -> false
                }
            }

            if (isSelesai && !isTantanganBonus) {
                btnMulaiKuis.text = "Kuis Selesai ✓"
                btnMulaiKuis.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.grey_button)
                btnMulaiKuis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_subtitle))
                
                btnMulaiKuis.setOnClickListener {
                    showCustomTopDialog("Kuis Selesai", "Anda telah menyelesaikan kuis untuk materi ini.", false, level, materiIndex ?: 1)
                }
            } else {
                btnMulaiKuis.text = "Mulai Kuis"
                btnMulaiKuis.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.dark_green)
                btnMulaiKuis.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                
                btnMulaiKuis.setOnClickListener {
                    showCustomTopDialog("Perhatian", "Silakan baca materi dengan seksama sebelum mengerjakan kuis.", true, level, materiIndex ?: 1)
                }
            }
        }
    }

    private fun showCustomTopDialog(title: String, message: String, isWarning: Boolean, level: Int, materiIndex: Int) {
        val dialog = Dialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.popup_kuis_peringatan, null)
        dialog.setContentView(dialogView)

        dialogView.findViewById<TextView>(R.id.tvTitle).text = title
        dialogView.findViewById<TextView>(R.id.tvMessage).text = message
        
        val btnAction = dialogView.findViewById<Button>(R.id.btnMulai)
        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)

        if (!isWarning) {
            btnAction.visibility = View.GONE
            btnBatal.text = "OK"
        } else {
            if (!hasBeenPromptedToRead) {
                btnAction.text = "BACA MATERI"
                btnAction.setOnClickListener {
                    hasBeenPromptedToRead = true
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Silakan baca materi sampai selesai", Toast.LENGTH_SHORT).show()
                }
            } else {
                btnAction.isEnabled = false
                btnAction.alpha = 0.5f
                
                countDownTimer?.cancel()
                countDownTimer = object : CountDownTimer(5000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        btnAction.text = "Tunggu (${millisUntilFinished / 1000}s)"
                    }
                    override fun onFinish() {
                        btnAction.isEnabled = true
                        btnAction.alpha = 1.0f
                        btnAction.text = "MULAI KUIS"
                        btnAction.setOnClickListener {
                            countDownTimer?.cancel()
                            dialog.dismiss()
                            startQuiz(materiIndex, level)
                        }
                    }
                }.start()
            }
        }

        btnBatal.setOnClickListener { 
            countDownTimer?.cancel()
            dialog.dismiss() 
        }
        btnClose.setOnClickListener { 
            countDownTimer?.cancel()
            dialog.dismiss() 
        }

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes.gravity = Gravity.TOP
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            attributes.y = 50 
        }
        dialog.show()
    }

    private fun startQuiz(materiIndex: Int, level: Int) {
        val fragment = if (linkedKuisId != null) {
            QuizSoalFragment.newInstance(linkedKuisId!!, level)
        } else {
            when (materiIndex) {
                1 -> QuizSoalFragment.newInstance("1", level)
                2 -> QuizSoal2Fragment()
                3 -> QuizSoal3Fragment()
                else -> QuizSoalFragment.newInstance(materiIndex.toString(), level)
            }
        }
        
        fragment.arguments = (fragment.arguments ?: Bundle()).apply {
            putBoolean("IS_TANTANGAN_BONUS", isTantanganBonus)
            putString("challenge_id", challengeId)
            putString("badge_id", badgeIdFromTantangan)
            putString("quiz_id", quizIdFromTantangan)
            putString("materi_id", edukasiId ?: "")
            putInt("LEVEL", level)
            if (linkedKuisId != null) putString("KUIS_ID", linkedKuisId)
        }
        
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadMateriFromFirebase(id: String) {
        FirebaseDatabase.getInstance().reference.child("edukasi").child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    snapshot.getValue(Edukasi::class.java)?.let { displayMateri(it) }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun displayMateri(edukasi: Edukasi) {
        val v = view ?: return
        v.findViewById<TextView>(R.id.tvJudulUtama).text = edukasi.title
        v.findViewById<MaterialToolbar>(R.id.toolbar).title = "Materi : ${edukasi.title}"
        
        v.findViewById<TextView>(R.id.tvIsiTitle).text = edukasi.section1Title.ifEmpty { edukasi.isiTitle }
        v.findViewById<TextView>(R.id.tvIsiMateri).text = edukasi.section1Content.ifEmpty { edukasi.content }
        v.findViewById<TextView>(R.id.tvPentingTitle).text = edukasi.section2Title.ifEmpty { edukasi.pentingTitle }
        v.findViewById<TextView>(R.id.tvPenting).text = edukasi.section2Content.ifEmpty { edukasi.pentingContent }
        v.findViewById<TextView>(R.id.tvContohTitle).text = edukasi.section3Title.ifEmpty { edukasi.contohTitle }
        v.findViewById<TextView>(R.id.tvContoh).text = edukasi.section3Content.ifEmpty { edukasi.contohContent }

        val imgMateri = v.findViewById<ImageView>(R.id.imgMateri)
        if (!edukasi.imageUrl.isNullOrEmpty()) {
            if (edukasi.imageUrl.length > 100 || edukasi.imageUrl.startsWith("http")) {
                val source = if (edukasi.imageUrl.length > 100 && !edukasi.imageUrl.startsWith("http")) 
                    android.util.Base64.decode(edukasi.imageUrl, android.util.Base64.DEFAULT) else edukasi.imageUrl
                Glide.with(this).load(source).placeholder(R.drawable.img_lingkungan).into(imgMateri)
            } else {
                val resId = resources.getIdentifier(edukasi.imageUrl, "drawable", requireContext().packageName)
                imgMateri.setImageResource(if (resId != 0) resId else R.drawable.img_lingkungan)
            }
        }
    }

    private fun loadLegacyMateri(index: Int) {
        val tvJudulUtama = view?.findViewById<TextView>(R.id.tvJudulUtama) ?: return
        val imgMateri = view?.findViewById<ImageView>(R.id.imgMateri) ?: return
        val tvIsiTitle = view?.findViewById<TextView>(R.id.tvIsiTitle) ?: return
        val tvIsiMateri = view?.findViewById<TextView>(R.id.tvIsiMateri) ?: return
        val tvPentingTitle = view?.findViewById<TextView>(R.id.tvPentingTitle) ?: return
        val tvPenting = view?.findViewById<TextView>(R.id.tvPenting) ?: return
        val tvContohTitle = view?.findViewById<TextView>(R.id.tvContohTitle) ?: return
        val tvContoh = view?.findViewById<TextView>(R.id.tvContoh) ?: return

        when (index) {
            1 -> {
                tvJudulUtama.text = "Dasar : Peduli Lingkungan"
                imgMateri.setImageResource(R.drawable.img_lingkungan)
                tvIsiTitle.text = "🌿 Apa itu Peduli Lingkungan?"
                tvIsiMateri.text = "Peduli lingkungan adalah sikap menjaga kebersihan serta kelestarian alam..."
                tvPentingTitle.text = "🌍 Kenapa Penting?"
                tvPenting.text = "Lingkungan bersih memberikan udara segar dan air bersih..."
                tvContohTitle.text = "♻️ Contoh Perilaku"
                tvContoh.text = "• Buang sampah pada tempatnya\n• Hemat listrik\n• Kurangi plastik"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
        if (edukasiId != null) findLinkedKuis(edukasiId!!)
        else updateButtonStatus()
    }

    override fun onDestroyView() {
        countDownTimer?.cancel()
        super.onDestroyView()
    }
}