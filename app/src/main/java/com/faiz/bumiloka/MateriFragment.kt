package com.faiz.bumiloka

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.model.Edukasi
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.bumptech.glide.Glide
import com.faiz.bumiloka.data.BonusChallengeRepository

class MateriFragment : Fragment() {

    private var edukasiId: String? = null
    private var linkedKuisId: String? = null
    private var isTantanganBonus = false
    private var challengeId = ""
    private var quizIdFromTantangan = ""
    private var badgeIdFromTantangan = ""
    private var levelMateri = 1
    
    private lateinit var btnMulaiKuis: Button
    private var countDownTimer: CountDownTimer? = null
    private var hasBeenPromptedToRead = false

    companion object {
        private const val ARG_MATERI_ID = "materi_id"
        private const val ARG_EDUKASI_ID = "edukasi_id"
        private const val ARG_LEVEL = "LEVEL"

        @JvmStatic
        fun newInstance(edukasiId: String, level: Int = 1): MateriFragment {
            val fragment = MateriFragment()
            val args = Bundle()
            args.putString(ARG_EDUKASI_ID, edukasiId)
            args.putInt(ARG_LEVEL, level)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstanceForChallenge(materiId: String): MateriFragment {
            return newInstance(materiId, 1)
        }

        @JvmStatic 
        fun newInstanceLegacy(materiId: Int): MateriFragment {
            val fragment = MateriFragment()
            val args = Bundle()
            args.putInt(ARG_MATERI_ID, materiId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_materi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        btnMulaiKuis = view.findViewById(R.id.btnMulaiKuis)
        
        toolbar.setNavigationOnClickListener { if (isAdded) parentFragmentManager.popBackStack() }

        arguments?.let {
            edukasiId = it.getString(ARG_EDUKASI_ID)
            levelMateri = it.getInt(ARG_LEVEL, 1)
            
            challengeId = it.getString("challenge_id") ?: ""
            quizIdFromTantangan = it.getString("quiz_id", "")
            badgeIdFromTantangan = it.getString("badge_id", "")
            isTantanganBonus = it.getBoolean("IS_TANTANGAN_BONUS", false)
            
            if (quizIdFromTantangan.isNotEmpty()) {
                linkedKuisId = quizIdFromTantangan
            }

            updateButtonStatus()

            edukasiId?.let { id ->
                loadMateriFromFirebase(id)
                if (linkedKuisId == null) findLinkedKuis(id)
            }
        }
    }

    private fun findLinkedKuis(eId: String) {
        FirebaseDatabase.getInstance().reference.child("kuis")
            .orderByChild("edukasiId").equalTo(eId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    snapshot.children.firstOrNull()?.let { linkedKuisId = it.key }
                    updateButtonStatus()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun updateButtonStatus() {
        if (!isAdded) return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val prefKuis = requireActivity().getSharedPreferences("KUIS_${userId}_LEVEL_$levelMateri", Context.MODE_PRIVATE)
        
        // Pengecekan status kuis yang lebih kuat
        val isSelesai = if (linkedKuisId != null) {
            prefKuis.getBoolean("kuis_${linkedKuisId}_selesai", false) || 
            (challengeId.isNotEmpty() && TantanganStatusHelper.isTantanganSelesai(requireContext(), challengeId))
        } else {
            prefKuis.getBoolean("quiz1_selesai", false) || prefKuis.getBoolean("materi1_selesai", false)
        }

        if (isSelesai && !isTantanganBonus) setButtonAsFinished() else setButtonAsActive()
    }

    private fun setButtonAsFinished() {
        if (!isAdded) return
        btnMulaiKuis.text = "Kuis Selesai ✓"
        btnMulaiKuis.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.grey_button)
        btnMulaiKuis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_subtitle))
        btnMulaiKuis.setOnClickListener {
            showCustomTopDialog("Kuis Selesai", "Anda telah menyelesaikan kuis untuk materi ini.", false)
        }
    }

    private fun setButtonAsActive() {
        if (!isAdded) return
        btnMulaiKuis.text = "Mulai Kuis"
        btnMulaiKuis.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.dark_green)
        btnMulaiKuis.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        btnMulaiKuis.setOnClickListener {
            showCustomTopDialog("Perhatian", "Silakan baca materi dengan seksama sebelum mengerjakan kuis.", true)
        }
    }

    private fun showCustomTopDialog(title: String, message: String, isWarning: Boolean) {
        if (!isAdded) return
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
                    override fun onTick(m: Long) { btnAction.text = "Tunggu (${m / 1000}s)" }
                    override fun onFinish() {
                        if (!isAdded) return
                        btnAction.isEnabled = true
                        btnAction.alpha = 1.0f
                        btnAction.text = "MULAI KUIS"
                        btnAction.setOnClickListener {
                            countDownTimer?.cancel()
                            dialog.dismiss()
                            syncAndStartQuiz()
                        }
                    }
                }.start()
            }
        }

        btnBatal.setOnClickListener { dialog.dismiss() }
        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes.gravity = Gravity.TOP
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            attributes.y = 50 
        }
        dialog.show()
    }

    private fun syncAndStartQuiz() {
        if (!isAdded) return
        val mId = edukasiId ?: "-1"
        TantanganStatusHelper.syncAllProgress(requireContext(), levelMateri, "MATERI", mId)
        
        if (isTantanganBonus && challengeId.isNotEmpty()) {
            BonusChallengeRepository.updateMateriDone(challengeId)
        }

        val finalQuizId = linkedKuisId ?: "1"
        val fragment = when (finalQuizId) {
            "2" -> QuizSoal2Fragment.newInstance(levelMateri)
            "3" -> QuizSoal3Fragment.newInstance(levelMateri)
            else -> QuizSoalFragment.newInstance(finalQuizId, levelMateri)
        }
        
        fragment.arguments = (fragment.arguments ?: Bundle()).apply {
            putBoolean("IS_TANTANGAN_BONUS", isTantanganBonus)
            putString("challenge_id", challengeId)
            putString("badge_id", badgeIdFromTantangan)
            putString("quiz_id", finalQuizId)
            putString("materi_id", edukasiId)
            putInt("LEVEL", levelMateri)
            putString("KUIS_ID", finalQuizId)
        }
        
        parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
    }

    private fun loadMateriFromFirebase(id: String) {
        FirebaseDatabase.getInstance().reference.child("edukasi").child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    try {
                        val map = snapshot.value as? Map<*, *> ?: return
                        val edukasi = Edukasi(
                            id = snapshot.key ?: "",
                            title = map["title"]?.toString() ?: "",
                            description = map["description"]?.toString() ?: "",
                            imageUrl = map["imageUrl"]?.toString() ?: "",
                            section1Title = map["section1Title"]?.toString() ?: map["isiTitle"]?.toString() ?: "",
                            section1Content = map["section1Content"]?.toString() ?: map["content"]?.toString() ?: "",
                            section2Title = map["section2Title"]?.toString() ?: map["pentingTitle"]?.toString() ?: "",
                            section2Content = map["section2Content"]?.toString() ?: map["pentingContent"]?.toString() ?: "",
                            section3Title = map["section3Title"]?.toString() ?: map["contohTitle"]?.toString() ?: "",
                            section3Content = map["section3Content"]?.toString() ?: map["contohContent"]?.toString() ?: ""
                        )
                        displayMateri(edukasi)
                    } catch (e: Exception) { Log.e("Materi", "Parse Error: ${e.message}") }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun displayMateri(edukasi: Edukasi) {
        val v = view ?: return
        v.findViewById<TextView>(R.id.tvJudulUtama).text = edukasi.title
        v.findViewById<MaterialToolbar>(R.id.toolbar).title = "Materi : ${edukasi.title}"
        
        v.findViewById<TextView>(R.id.tvIsiTitle).text = edukasi.section1Title
        v.findViewById<TextView>(R.id.tvIsiMateri).text = edukasi.section1Content
        v.findViewById<TextView>(R.id.tvPentingTitle).text = edukasi.section2Title
        v.findViewById<TextView>(R.id.tvPenting).text = edukasi.section2Content
        v.findViewById<TextView>(R.id.tvContohTitle).text = edukasi.section3Title
        v.findViewById<TextView>(R.id.tvContoh).text = edukasi.section3Content

        val imgMateri = v.findViewById<ImageView>(R.id.imgMateri)
        if (edukasi.imageUrl.isNotEmpty()) {
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

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
        updateButtonStatus()
    }
}
