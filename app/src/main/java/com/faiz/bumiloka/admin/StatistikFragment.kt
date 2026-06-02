package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.adapters.UserStatsAdapter
import com.google.firebase.database.*
import com.faiz.bumiloka.model.UserModel

class StatistikFragment : Fragment() {

    private val dbRef = FirebaseDatabase.getInstance().reference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserStatsAdapter
    private val userList = mutableListOf<UserModel>()

    private lateinit var tvTotalUser: TextView
    private lateinit var tvTotalKuis: TextView
    private lateinit var tvTotalMateri: TextView
    private lateinit var tvTotalTantangan: TextView
    private lateinit var progressBar: ProgressBar

    // Listeners supaya bisa di-remove saat destroy
    private var listenerMateri: ValueEventListener? = null
    private var listenerKuis: ValueEventListener? = null
    private var listenerTantangan: ValueEventListener? = null
    private var listenerUsers: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(com.faiz.bumiloka.R.layout.fragment_statistik, container, false)

        tvTotalUser = view.findViewById(com.faiz.bumiloka.R.id.tv_total_user)
        tvTotalKuis = view.findViewById(com.faiz.bumiloka.R.id.tv_total_kuis_statistik)
        tvTotalMateri = view.findViewById(com.faiz.bumiloka.R.id.tv_total_edukasi_statistik)
        tvTotalTantangan = view.findViewById(com.faiz.bumiloka.R.id.tv_total_tantangan_statistik)
        progressBar = view.findViewById(com.faiz.bumiloka.R.id.progress_statistik)

        recyclerView = view.findViewById(com.faiz.bumiloka.R.id.rv_user_stats)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = UserStatsAdapter(userList)
        recyclerView.adapter = adapter

        loadStatistik()
        return view
    }

    private fun loadStatistik() {
        progressBar.visibility = View.VISIBLE

        // Hitung total materi
        listenerMateri = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvTotalMateri.text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        dbRef.child("materi").addValueEventListener(listenerMateri!!)

        // Hitung total kuis
        listenerKuis = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvTotalKuis.text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        dbRef.child("kuis").addValueEventListener(listenerKuis!!)

        // Hitung total tantangan
        listenerTantangan = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvTotalTantangan.text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        dbRef.child("tantangan").addValueEventListener(listenerTantangan!!)

        // Load data users untuk leaderboard
        listenerUsers = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressBar.visibility = View.GONE
                tvTotalUser.text = snapshot.childrenCount.toString()

                userList.clear()
                for (child in snapshot.children) {
                    val user = UserModel(
                        uid = child.key ?: "",
                        nama = child.child("nama").getValue(String::class.java) ?: "Tanpa Nama",
                        email = child.child("email").getValue(String::class.java) ?: "",
                        role = child.child("role").getValue(String::class.java) ?: "user",
                        totalPoin = child.child("totalPoin").getValue(Int::class.java) ?: 0,
                        kuisSelesai = child.child("kuisSelesai").getValue(Int::class.java) ?: 0,
                        edukasiDibaca = child.child("edukasiDibaca").getValue(Int::class.java) ?: 0,
                        tantanganSelesai = child.child("tantanganSelesai").getValue(Int::class.java) ?: 0
                    )
                    // Tampilkan semua user kecuali admin
                    if (user.role != "admin") userList.add(user)
                }

                // Urutkan berdasarkan poin tertinggi (leaderboard)
                userList.sortByDescending { it.totalPoin }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Gagal load statistik: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        dbRef.child("users").addValueEventListener(listenerUsers!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Bersihkan semua listener agar tidak memory leak
        listenerMateri?.let { dbRef.child("materi").removeEventListener(it) }
        listenerKuis?.let { dbRef.child("kuis").removeEventListener(it) }
        listenerTantangan?.let { dbRef.child("tantangan").removeEventListener(it) }
        listenerUsers?.let { dbRef.child("users").removeEventListener(it) }
    }
}