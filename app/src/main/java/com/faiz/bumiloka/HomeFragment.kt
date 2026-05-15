package com.faiz.bumiloka

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Locale
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.ProgressBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    private lateinit var pbProgress: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvLevel: TextView
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onResume() {
        super.onResume()
        loadProgressFirebase(pbProgress, tvProgress, tvLevel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pbProgress = view.findViewById(R.id.pbTargetProgress)
        tvProgress = view.findViewById(R.id.tvProgressPercentage)
        tvLevel = view.findViewById(R.id.tvLevelTitle)

        loadProgressFirebase(pbProgress, tvProgress, tvLevel)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Cek Session: Jika tidak ada user yang login, arahkan ke LoginActivity
        if (currentUser == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)
        val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)
        val btnEdukasi = view.findViewById<CardView>(R.id.btnEdukasi)
        val btnMisi = view.findViewById<CardView>(R.id.btnMisi)
        val btnTantangan = view.findViewById<CardView>(R.id.btnTantangan)
        val btnKuis = view.findViewById<CardView>(R.id.btnKuis)
        val tvRekomendasiHariIni = view.findViewById<TextView>(R.id.tvRekomendasiHariIni)
        val prefs = requireContext().getSharedPreferences("APP", Context.MODE_PRIVATE)
        val sudah = prefs.getBoolean("sudah_welcome", false)

        if (!sudah) {
            showWelcomeThenProfil()
            prefs.edit().putBoolean("sudah_welcome", true).apply()
        }

        // Fungsi untuk memperbarui tampilan nama
        fun updateUserName(user: FirebaseUser?) {
            val rawName = when {
                !user?.displayName.isNullOrBlank() -> user?.displayName
                !user?.email.isNullOrBlank() -> user?.email?.substringBefore("@")
                else -> "Bumi Lover"
            }

            // Capitalize: Mengubah huruf pertama setiap kata menjadi huruf besar
            val nameToShow = rawName?.split(" ")?.joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            } ?: ""

            tvGreeting.text = getString(R.string.hello_placeholder, nameToShow)

            // Setup listener profil dengan nama terbaru
            ivProfile.setOnClickListener { profileView ->
                val popupMenu = PopupMenu(requireContext(), profileView)

                // Membuat username menjadi BOLD menggunakan SpannableString
                val spannableName = SpannableString(nameToShow)
                spannableName.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    spannableName.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Menyamakan ID Menu dengan ID di Listener
                popupMenu.menu.add(0, 1, 0, spannableName)
                popupMenu.menu.add(0, 2, 1, "Pengaturan Profil")
                popupMenu.menu.add(0, 3, 2, "Keluar")

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> {
                            Toast.makeText(requireContext(), "Logged in as $nameToShow", Toast.LENGTH_SHORT).show()
                            true
                        }
                        2 -> {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, PengaturanFragment())
                                .addToBackStack(null)
                                .commit()
                            true
                        }
                        3 -> {
                            // Konfirmasi Logout
                            AlertDialog.Builder(requireContext())
                                .setTitle("Konfirmasi Keluar")
                                .setMessage("Apakah Anda yakin ingin keluar?")
                                .setPositiveButton("Ya") { _, _ ->
                                    // Proses Logout
                                    auth.signOut()
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        Toast.makeText(requireContext(), "Berhasil Keluar", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(requireContext(), LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        activity?.finish()
                                    }
                                }
                                .setNegativeButton("Tidak", null)
                                .show()
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }

        btnEdukasi.setOnClickListener {
            requireProfile {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, EdukasiFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        btnMisi.setOnClickListener {
            requireProfile {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MisiFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        btnTantangan.setOnClickListener {
            requireProfile {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TantanganFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        btnKuis.setOnClickListener {
            requireProfile {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, QuizUtamaFragment())
                    .commit()
            }
        }

        // Tampilkan nama awal dari cache session
        updateUserName(currentUser)

        // Reload data dari server Firebase untuk memastikan Nama muncul (sinkronisasi)
        currentUser.reload().addOnCompleteListener {
            if (it.isSuccessful) {
                updateUserName(auth.currentUser)
            }
        }
        // TAMBAHAN: Tampilkan rekomendasi harian
        tampilkanRekomendasiHarian(tvRekomendasiHariIni)
    }

    // ===============================
    // REKOMENDASI HARIAN (1 HARI 1 REKOMENDASI)
    // ===============================
    private fun tampilkanRekomendasiHarian(tvRekomendasi: TextView) {

        val sharedPref = requireActivity().getSharedPreferences(
            "RekomendasiHarian",
            android.content.Context.MODE_PRIVATE
        )

        val editor = sharedPref.edit()

        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        val savedDate = sharedPref.getString("tanggal", "")
        var rekomendasiHariIni = sharedPref.getString("rekomendasi", "")

        if (savedDate != today) {

            val daftarRekomendasi = listOf(

                // Edukasi / Materi
                "📘 Baca materi edukasi baru hari ini!",
                "📚 Lanjutkan membaca materi berikutnya sekarang!",
                "🌍 Pelajari tips baru tentang lingkungan hari ini!",
                "📖 Review kembali materi yang sudah kamu pelajari!",
                "🧠 Tambah wawasanmu dengan membaca materi terbaru!",
                "📗 Yuk lanjutkan edukasi lingkunganmu hari ini!",
                "🌱 Baca materi tentang gaya hidup ramah lingkungan!",
                "📘 Mulai baca materi pertamamu hari ini!",
                "📚 Jangan lupa selesaikan materi yang belum dibaca!",
                "🌿 Baca satu materi untuk menambah progresmu!",

                // Kuis
                "❓ Selesaikan kuis hari ini untuk menambah wawasan!",
                "📝 Kerjakan kuis sekarang dan uji pemahamanmu!",
                "🎯 Yuk lanjutkan kuis yang belum selesai!",
                "🏆 Tantang dirimu dengan menyelesaikan kuis baru!",
                "📊 Coba kerjakan kuis dari materi yang sudah dibaca!",
                "🔥 Selesaikan kuis agar progresmu bertambah!",
                "💡 Uji pengetahuanmu lewat kuis hari ini!",
                "📍 Jangan lewatkan kuis harianmu!",
                "🚀 Kerjakan kuis sekarang juga!",
                "🎓 Lanjutkan kuis berikutnya untuk hasil terbaik!",

                // Tantangan
                "🚩 Selesaikan tantangan hari ini sekarang!",
                "🌱 Lanjutkan tantangan lingkunganmu hari ini!",
                "♻️ Kerjakan tantangan baru untuk bumi yang lebih baik!",
                "🏅 Yuk selesaikan tantangan yang tersedia!",
                "🔥 Jangan berhenti, lanjutkan tantanganmu!",
                "🌍 Tantangan hari ini menunggumu!",
                "🎯 Coba selesaikan satu tantangan sekarang!",
                "🚀 Lanjutkan tantangan agar progres meningkat!",
                "💪 Selesaikan aksi hijau melalui tantangan hari ini!",
                "🌿 Kerjakan tantangan ramah lingkungan sekarang!",

                // Misi
                "🎯 Kerjakan misi hari ini untuk menambah pencapaian!",
                "🚀 Selesaikan misi baru sekarang!",
                "🏆 Lanjutkan misi yang belum selesai!",
                "📍 Coba satu misi baru hari ini!",
                "🔥 Jangan lupa kerjakan misi harianmu!",
                "🌍 Selesaikan misi lingkungan untuk progres lebih baik!",
                "💡 Misi baru siap kamu selesaikan!",
                "🎖️ Yuk tuntaskan misi berikutnya!",
                "📈 Tambah progres dengan menyelesaikan misi!",
                "🌱 Kerjakan misi sederhana untuk bantu bumi!",

                // Aksi lingkungan umum
                "🌱 Gunakan tumbler sendiri hari ini untuk menjaga lingkungan!",
                "♻️ Kurangi penggunaan plastik sekali pakai!",
                "💡 Matikan lampu yang tidak digunakan!",
                "🚶 Jalan kaki ke tempat dekat!",
                "🛍️ Gunakan tas belanja reusable!",
                "🚿 Gunakan air secukupnya!",
                "🪥 Matikan keran saat menyikat gigi!",
                "🥤 Tolak sedotan plastik hari ini!",
                "🗑️ Buang sampah pada tempatnya!",
                "♻️ Pisahkan sampah organik dan anorganik!",
                "🌿 Rawat tanaman di rumah hari ini!",
                "🚲 Gunakan sepeda untuk perjalanan dekat!",
                "🔌 Cabut charger jika tidak digunakan!",
                "📄 Gunakan kertas seperlunya!",
                "🍱 Bawa bekal sendiri untuk kurangi sampah!",
                "🌞 Manfaatkan cahaya alami di siang hari!",
                "🧴 Gunakan botol isi ulang!",
                "🧹 Bersihkan area sekitarmu hari ini!",
                "🥗 Habiskan makananmu agar tidak terbuang!",
                "🌏 Satu aksi kecilmu hari ini bisa bantu bumi!"
            )

            rekomendasiHariIni = daftarRekomendasi.random()

            editor.putString("tanggal", today)
            editor.putString("rekomendasi", rekomendasiHariIni)
            editor.apply()
        }

        tvRekomendasi.text = rekomendasiHariIni
    }
    private fun checkProfileOnce() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference

        // Pastikan path-nya benar: users -> userId
        db.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            // Ambil status profil secara akurat
            val isComplete = snapshot.child("isProfileComplete").getValue(Boolean::class.java) ?: false

            Log.d("BUMILOKA_DEBUG", "Cek profil UID: $userId | Status: $isComplete")

            if (!isComplete) {
                showLengkapiProfilDialog()
            }
        }.addOnFailureListener {
            Log.e("BUMILOKA_DEBUG", "Gagal koneksi Firebase: ${it.message}")
        }
    }

    private fun requireProfile(action: () -> Unit) {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference

        db.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            val isComplete = snapshot.child("isProfileComplete").getValue(Boolean::class.java) ?: false

            if (isComplete) {
                action()
            } else {
                showLengkapiProfilDialog()
            }
        }
    }
    private fun showWelcomeThenProfil() {
        if (!isAdded) return

        val user = auth.currentUser
        val nama = user?.displayName?.split(" ")?.first() ?: "Pengguna"

        // 👉 tampilkan toast kecil
        val toastView = layoutInflater.inflate(R.layout.toast_welcome, null)
        val tv = toastView.findViewById<TextView>(R.id.tvToastMessage)
        tv.text = "Selamat Datang, $nama 👋"

        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT
        toast.view = toastView
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 200)

        toast.show()

        // 👉 setelah itu baru cek profil
        Handler(Looper.getMainLooper()).postDelayed({
            checkProfileOnce()
        }, 1500)
    }

    // Fungsi pembantu agar tidak menulis ulang dialog berkali-kali
    private fun showLengkapiProfilDialog() {
        if (!isAdded) return

        val view = layoutInflater.inflate(R.layout.pop_up_profile, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(true)
            .create()

        dialog.show()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 👉 INI LETAKNYA DI SINI
        view.setOnClickListener {
            dialog.dismiss()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PengaturanFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadProgressFirebase(
        progressBar: ProgressBar,
        tvProgress: TextView,
        tvLevel: TextView
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = com.google.firebase.database.FirebaseDatabase.getInstance()
            .reference.child("users").child(userId)

        db.get().addOnSuccessListener { snapshot ->

            val level = snapshot.child("level").getValue(Int::class.java) ?: 1
            val xp = snapshot.child("xp").getValue(Int::class.java) ?: 0

            val targetXP = level * 100

            val progressPercent = ((xp.toDouble() / targetXP) * 100).toInt()

            // SET UI
            progressBar.progress = progressPercent
            tvProgress.text = "$progressPercent% tercapai"
            tvLevel.text = "🏅 Level $level"

        }.addOnFailureListener {
            Log.e("HOME_PROGRESS", "Gagal ambil data: ${it.message}")
        }
    }
}
