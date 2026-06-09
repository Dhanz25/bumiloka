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

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val imgMateri = view.findViewById<ImageView>(R.id.imgMateri)
        val tvJudulUtama = view.findViewById<TextView>(R.id.tvJudulUtama)
        val tvIsiTitle = view.findViewById<TextView>(R.id.tvIsiTitle)
        val tvIsiMateri = view.findViewById<TextView>(R.id.tvIsiMateri)
        val tvPentingTitle = view.findViewById<TextView>(R.id.tvPentingTitle)
        val tvPenting = view.findViewById<TextView>(R.id.tvPenting)
        val tvContohTitle = view.findViewById<TextView>(R.id.tvContohTitle)
        val tvContoh = view.findViewById<TextView>(R.id.tvContoh)
        val btnMulaiKuis = view.findViewById<Button>(R.id.btnMulaiKuis)

        val materiIndex = arguments?.getInt(ARG_MATERI_ID) ?: 1
        
        LevelHelper.getCurrentLevel(requireContext()) { level ->
            toolbar.title = "Level $level - Materi $materiIndex"
            
            when (level) {
                1 -> loadLevel1Materi(materiIndex, tvJudulUtama, imgMateri, tvIsiTitle, tvIsiMateri, tvPentingTitle, tvPenting, tvContohTitle, tvContoh)
                2 -> loadLevel2Materi(materiIndex, tvJudulUtama, imgMateri, tvIsiTitle, tvIsiMateri, tvPentingTitle, tvPenting, tvContohTitle, tvContoh)
                3 -> loadLevel3Materi(materiIndex, tvJudulUtama, imgMateri, tvIsiTitle, tvIsiMateri, tvPentingTitle, tvPenting, tvContohTitle, tvContoh)
            }
        }

        btnMulaiKuis.setOnClickListener {
            val dariTantangan = arguments?.getBoolean("DARI_TANTANGAN", false) ?: false
            val resultBundle = Bundle().apply { putInt("materi_id", materiIndex) }
            parentFragmentManager.setFragmentResult("materi_selesai_result", resultBundle)

            LevelHelper.getCurrentLevel(requireContext()) { level ->
                val fragment = when (materiIndex) {
                    1 -> QuizSoalFragment.newInstance(materiIndex)
                    2 -> QuizSoal2Fragment.newInstance(materiIndex)
                    3 -> QuizSoal3Fragment.newInstance(materiIndex)
                    else -> QuizSoalFragment.newInstance(materiIndex)
                }
                val args = fragment.arguments ?: Bundle()
                args.putBoolean("DARI_TANTANGAN", dariTantangan)
                args.putInt("LEVEL", level)
                fragment.arguments = args

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun loadLevel1Materi(index: Int, judul: TextView, img: ImageView, t1: TextView, m1: TextView, t2: TextView, p2: TextView, t3: TextView, c3: TextView) {
        when (index) {
            1 -> {
                judul.text = "Dasar : Peduli Lingkungan"
                img.setImageResource(R.drawable.img_lingkungan)
                t1.text = "🌿 Apa itu Peduli Lingkungan?"
                m1.text = "Peduli lingkungan adalah sikap menjaga kebersihan serta kelestarian alam. Dimulai dari hal kecil setiap hari seperti membuang sampah pada tempatnya."
                t2.text = "🌍 Kenapa Penting?"
                p2.text = "Lingkungan bersih memberikan udara segar dan air bersih, serta menjauhkan kita dari penyakit dan bencana banjir."
                t3.text = "♻️ Contoh Perilaku"
                c3.text = "• Buang sampah pada tempatnya\n• Hemat listrik\n• Kurangi plastik"
            }
            2 -> {
                judul.text = "Kenalan Sama Sampah"
                img.setImageResource(R.drawable.img_sampah)
                t1.text = "🗑️ Apa itu Sampah?"
                m1.text = "Sampah adalah sisa barang yang sudah tidak digunakan. Jika tidak dikelola, sampah akan menumpuk dan mencemari lingkungan."
                t2.text = "⚠️ Masalah Sampah"
                p2.text = "Sampah yang menumpuk menyumbat saluran air dan menjadi sarang penyakit."
                t3.text = "♻️ Jenis Sampah"
                c3.text = "• Organik (Sisa makanan)\n• Anorganik (Plastik/Logam)"
            }
            3 -> {
                judul.text = "Dasar Hemat Air"
                img.setImageResource(R.drawable.img_air)
                t1.text = "💧 Pentingnya Air"
                m1.text = "Air adalah sumber kehidupan. Ketersediaan air bersih terbatas, maka kita harus menggunakannya dengan bijak."
                t2.text = "⚠️ Dampak Boros"
                p2.text = "Pemborosan air menyebabkan kekeringan dan krisis air bersih di masa depan."
                t3.text = "🚿 Cara Hemat"
                c3.text = "• Tutup keran saat sikat gigi\n• Perbaiki keran bocor"
            }
        }
    }

    private fun loadLevel2Materi(index: Int, judul: TextView, img: ImageView, t1: TextView, m1: TextView, t2: TextView, p2: TextView, t3: TextView, c3: TextView) {
        when (index) {
            1 -> {
                judul.text = "Organik vs Anorganik"
                img.setImageResource(R.drawable.img_sampah)
                t1.text = "🍏 Sampah Organik"
                m1.text = "Sampah organik berasal dari makhluk hidup dan mudah membusuk. Bisa diolah menjadi kompos."
                t2.text = "🧴 Sampah Anorganik"
                p2.text = "Sampah anorganik berasal dari bahan non-hayati (plastik, kaca). Butuh ratusan tahun untuk terurai."
                t3.text = "📌 Pemilahan"
                c3.text = "Memisahkan sampah sejak dari rumah memudahkan proses daur ulang."
            }
            2 -> {
                judul.text = "Konsep 3R (Reduce, Reuse, Recycle)"
                img.setImageResource(R.drawable.img_sampah)
                t1.text = "♻️ Mengenal 3R"
                m1.text = "Reduce (Mengurangi), Reuse (Gunakan kembali), Recycle (Daur ulang) adalah pilar pengelolaan sampah modern."
                t2.text = "📉 Reduce & Reuse"
                p2.text = "Kurangi pemakaian barang sekali pakai dan gunakan kembali barang yang masih layak."
                t3.text = "🛠️ Recycle"
                c3.text = "Mengolah sampah menjadi produk baru yang bernilai guna."
            }
            3 -> {
                judul.text = "Bahaya Sampah Plastik"
                img.setImageResource(R.drawable.img_sampah)
                t1.text = "🚫 Ancaman Plastik"
                m1.text = "Plastik sulit hancur dan sering berakhir di laut, mengancam ekosistem dan kesehatan hewan laut."
                t2.text = "🧬 Mikroplastik"
                p2.text = "Plastik yang hancur menjadi butiran kecil (mikroplastik) bisa masuk ke rantai makanan manusia."
                t3.text = "✅ Solusi"
                c3.text = "Gunakan tas belanja kain dan botol minum sendiri (tumbler)."
            }
        }
    }

    private fun loadLevel3Materi(index: Int, judul: TextView, img: ImageView, t1: TextView, m1: TextView, t2: TextView, p2: TextView, t3: TextView, c3: TextView) {
        when (index) {
            1 -> {
                judul.text = "Konservasi Air Bersih"
                img.setImageResource(R.drawable.img_air)
                t1.text = "🌊 Krisis Air"
                m1.text = "Hanya sebagian kecil air di bumi yang layak dikonsumsi. Pencemaran memperburuk krisis air bersih."
                t2.text = "🛡️ Perlindungan Sumber Air"
                p2.text = "Menjaga hutan dan sungai adalah kunci menjaga cadangan air tanah kita."
                t3.text = "🌱 Peran Kita"
                c3.text = "Menanam pohon membantu tanah menyerap air hujan lebih baik."
            }
            2 -> {
                judul.text = "Siklus Air & Keberlanjutan"
                img.setImageResource(R.drawable.img_air)
                t1.text = "☁️ Bagaimana Air Berputar?"
                m1.text = "Air menguap, menjadi awan, dan jatuh sebagai hujan. Gangguan pada alam merusak siklus alami ini."
                t2.text = "🌡️ Perubahan Iklim"
                p2.text = "Pemanasan global mengubah pola hujan, menyebabkan banjir atau kekeringan ekstrem."
                t3.text = "🔄 Re-use Air"
                c3.text = "Air bekas cucian beras bisa digunakan untuk menyiram tanaman."
            }
            3 -> {
                judul.text = "Teknik Hemat Air Lanjutan"
                img.setImageResource(R.drawable.img_air)
                t1.text = "🛁 Mandi Pintar"
                m1.text = "Gunakan shower daripada gayung untuk menghemat hingga 50% air setiap kali mandi."
                t2.text = "🧺 Cuci Efisien"
                p2.text = "Cucilah pakaian saat mesin cuci penuh untuk meminimalisir pembuangan air."
                t3.text = "🚰 Cek Kebocoran"
                c3.text = "Satu tetes air per detik dari keran bocor bisa membuang ribuan liter air setahun."
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }
}
