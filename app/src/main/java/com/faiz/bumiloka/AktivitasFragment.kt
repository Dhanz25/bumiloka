package com.faiz.bumiloka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AktivitasFragment : Fragment() {

    private lateinit var recyclerAktivitas: RecyclerView
    private lateinit var adapter: AktivitasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_aktivitas, container, false)

        recyclerAktivitas = view.findViewById(R.id.recyclerAktivitas)
        recyclerAktivitas.layoutManager = LinearLayoutManager(requireContext())

        val aktivitasData = arrayListOf(
            AktivitasItem(
                "Hari ini - baru saja",
                listOf(
                    "Menyelesaikan Materi 1",
                    "Menyelesaikan Kuis Materi 1",
                    "Mendapat skor 80"
                )
            ),
            AktivitasItem(
                "Kemarin",
                listOf(
                    "Membuka aplikasi",
                    "Memulai pembelajaran"
                )
            )
        )

        adapter = AktivitasAdapter(requireContext(), aktivitasData)
        recyclerAktivitas.adapter = adapter

        return view
    }
}