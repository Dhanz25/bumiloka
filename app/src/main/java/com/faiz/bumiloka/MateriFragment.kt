package com.faiz.bumiloka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar

class MateriFragment : Fragment() {

    companion object {
        private const val ARG_MATERI_ID = "materi_id"

        fun newInstance(materiId: Int): MateriFragment {
            val fragment = MateriFragment()
            val args = Bundle()
            args.putInt(ARG_MATERI_ID, materiId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_materi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔥 TOOLBAR (SESUAI XML)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)

        // 🔙 BACK
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 🔥 VIEW
        val imgMateri = view.findViewById<ImageView>(R.id.imgMateri)
        val tvJudulUtama = view.findViewById<TextView>(R.id.tvJudulUtama)

        val tvIsiTitle = view.findViewById<TextView>(R.id.tvIsiTitle)
        val tvIsiMateri = view.findViewById<TextView>(R.id.tvIsiMateri)

        val tvPentingTitle = view.findViewById<TextView>(R.id.tvPentingTitle)
        val tvPenting = view.findViewById<TextView>(R.id.tvPenting)

        val tvContohTitle = view.findViewById<TextView>(R.id.tvContohTitle)
        val tvContoh = view.findViewById<TextView>(R.id.tvContoh)

        val btnMulaiKuis = view.findViewById<Button>(R.id.btnMulaiKuis)

        val materiId = arguments?.getInt(ARG_MATERI_ID) ?: 1

        // 🔥 SET TITLE TOOLBAR
        toolbar.title = "MATERI $materiId"

        when (materiId) {

            // ================== MATERI 1 ==================
            1 -> {
                tvJudulUtama.text = "Dasar : Peduli Lingkungan"
                imgMateri.setImageResource(R.drawable.img_lingkungan)

                tvIsiTitle.text = "🌿 Apa itu Peduli Lingkungan?"
                tvPentingTitle.text = "🌍 Kenapa Penting?"
                tvContohTitle?.text = "♻️ Contoh Perilaku"

                tvIsiMateri.text = "Peduli lingkungan adalah sikap dan kebiasaan untuk menjaga kebersihan serta kelestarian alam di sekitar kita. Sikap ini dapat dimulai dari hal-hal kecil yang dilakukan setiap hari.\n\nContohnya seperti membuang sampah pada tempatnya, menjaga kebersihan rumah, serta tidak merusak lingkungan. Dengan kebiasaan sederhana ini, kita sudah ikut berkontribusi menjaga bumi.\n\nLingkungan yang terjaga akan memberikan kehidupan yang lebih sehat, nyaman, dan aman bagi semua makhluk hidup."

                tvPenting.text = "Lingkungan yang bersih memberikan banyak manfaat bagi kehidupan manusia. Udara menjadi lebih segar, air tetap bersih untuk digunakan, dan kita terhindar dari berbagai penyakit.\n\nSelain itu, menjaga lingkungan juga membantu mencegah bencana seperti banjir yang disebabkan oleh sampah serta kerusakan alam akibat ulah manusia.\n\nJika lingkungan tidak dijaga, maka dampaknya akan kembali ke manusia sendiri dalam bentuk penyakit dan bencana."

                tvContoh.text = "• Membuang sampah pada tempatnya\n" +
                        "• Menghemat penggunaan air\n" +
                        "• Mematikan listrik saat tidak digunakan\n" +
                        "• Mengurangi penggunaan plastik\n" +
                        "• Membawa botol minum sendiri\n" +
                        "• Menanam tanaman di sekitar rumah\n\n" +
                        "💡 Tips: Lakukan kebiasaan kecil ini setiap hari agar menjadi kebiasaan baik."
            }

            // ================== MATERI 2 ==================
            2 -> {
                tvJudulUtama.text = "Kenalan Dulu Sama Sampah!"
                imgMateri.setImageResource(R.drawable.img_sampah)

                tvIsiTitle.text = "🗑️ Apa itu Sampah?"
                tvPentingTitle.text = "⚠️ Kenapa Sampah Jadi Masalah?"
                tvContohTitle.text = "♻️ Jenis-Jenis Sampah"

                tvIsiMateri.text = "Sampah adalah sisa barang atau bahan yang sudah tidak digunakan lagi oleh manusia. Sampah dihasilkan setiap hari dari berbagai aktivitas seperti di rumah, sekolah, dan lingkungan sekitar.\n\nJika tidak dikelola dengan baik, sampah akan menumpuk dan menjadi masalah serius bagi lingkungan."

                tvPenting.text = "Sampah yang tidak dikelola dengan baik dapat menyebabkan pencemaran lingkungan, bau tidak sedap, hingga menjadi sarang penyakit.\n\nSelain itu, sampah yang menumpuk juga dapat menyumbat saluran air dan menyebabkan banjir.\n\nOleh karena itu, penting bagi kita untuk mengelola sampah dengan benar."

                tvContoh.text = "• Sampah Organik (mudah terurai)\n" +
                        "  Contoh: sisa makanan, daun, kulit buah\n\n" +
                        "• Sampah Anorganik (sulit terurai)\n" +
                        "  Contoh: plastik, botol, kaleng\n\n" +
                        "💡 Tips: Pisahkan sampah agar mudah didaur ulang."

            }

            // ================== MATERI 3 ==================
            3 -> {
                tvJudulUtama.text = "Menghemat Air Saat Digunakan"
                imgMateri.setImageResource(R.drawable.img_air)

                tvIsiTitle.text = "💧 Mengapa Harus Hemat Air?"
                tvPentingTitle.text = "⚠️ Dampak Jika Boros Air"
                tvContohTitle.text = "🚿 Cara Menghemat Air"

                tvIsiMateri.text = "Air adalah sumber kehidupan yang sangat penting bagi manusia, hewan, dan tumbuhan. Kita menggunakan air setiap hari untuk berbagai kebutuhan.\n\nNamun, jika digunakan secara berlebihan, ketersediaan air bersih dapat berkurang di masa depan.\n\nKarena itu, kita harus menggunakan air dengan bijak."

                tvPenting.text = "Penggunaan air yang berlebihan dapat menyebabkan kekurangan air bersih dan kekeringan.\n\nSelain itu, pemborosan air juga dapat merusak keseimbangan lingkungan dan mengganggu kehidupan makhluk hidup.\n\nJika tidak mulai menghemat sekarang, masa depan bisa kekurangan air."

                tvContoh.text = "• Menutup keran saat tidak digunakan\n" +
                        "• Menggunakan air secukupnya\n" +
                        "• Memperbaiki keran yang bocor\n" +
                        "• Menggunakan air bekas untuk menyiram tanaman\n" +
                        "• Tidak membuang air sia-sia\n\n" +
                        "💡 Tips: Gunakan air seperlunya agar tidak boros."
            }
        }
        btnMulaiKuis.setOnClickListener {
            val dariTantangan = arguments?.getBoolean("DARI_TANTANGAN", false) ?: false

            val resultBundle = Bundle().apply {
                putInt("materi_id", materiId)
            }
            parentFragmentManager.setFragmentResult("materi_selesai_result", resultBundle)

            val fragment = when (materiId) {
                1 -> QuizSoalFragment.newInstance(materiId)
                2 -> QuizSoal2Fragment.newInstance(materiId)
                3 -> QuizSoal3Fragment.newInstance(materiId)
                else -> QuizSoalFragment.newInstance(materiId)
            }

            // ✅ Teruskan flag DARI_TANTANGAN ke quiz
            val args = fragment.arguments ?: Bundle()
            args.putBoolean("DARI_TANTANGAN", dariTantangan)
            fragment.arguments = args

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    // 🔻 SEMBUNYIKAN NAV
    override fun onResume() {
        super.onResume()
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav?.visibility = View.GONE
    }
}