package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AktivitasFragment : Fragment(R.layout.fragment_aktivitas) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: AktivitasAdapter

    private val displayList = mutableListOf<AktivitasItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Tampilkan Bottom Navigation
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE

        recyclerView = view.findViewById(R.id.recyclerAktivitas)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AktivitasAdapter(displayList)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // ✅ Tampilkan Bottom Navigation saat kembali ke fragment ini
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
        loadData()
    }

    private fun loadData() {
        val data = AktivitasManager.getAktivitas(requireContext())

        displayList.clear()

        var lastHeader = ""

        for (item in data) {

            val label = getHariLabel(item.timestamp)

            if (label != lastHeader) {
                displayList.add(AktivitasItem.Header(label))
                lastHeader = label
            }

            displayList.add(AktivitasItem.Item(item))
        }

        if (displayList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }

        adapter.notifyDataSetChanged()
    }

    private fun getHariLabel(timestamp: Long): String {

        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val oneDay = 24 * 60 * 60 * 1000L

        return when {
            diff < oneDay -> "Hari Ini"
            diff < 2 * oneDay -> "Kemarin"
            else -> {
                val sdf = java.text.SimpleDateFormat(
                    "dd MMMM yyyy",
                    java.util.Locale("id", "ID")
                )
                sdf.format(java.util.Date(timestamp))
            }
        }
    }
}
