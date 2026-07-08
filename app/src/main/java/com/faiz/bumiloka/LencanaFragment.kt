package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.adapters.UserBadgeAdapter
import com.faiz.bumiloka.model.Badge
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LencanaFragment : Fragment(R.layout.fragment_lencana) {

    private lateinit var rvLencana: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: UserBadgeAdapter
    private val badgeList = mutableListOf<Badge>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        rvLencana = view.findViewById(R.id.rvLencana)
        progressBar = view.findViewById(R.id.progressBar)

        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        rvLencana.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = UserBadgeAdapter(badgeList)
        rvLencana.adapter = adapter

        fetchBadges()
    }

    private fun fetchBadges() {
        progressBar.visibility = View.VISIBLE
        FirebaseDatabase.getInstance().reference.child("badges")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    progressBar.visibility = View.GONE
                    badgeList.clear()
                    for (child in snapshot.children) {
                        child.getValue(Badge::class.java)?.let {
                            it.id = child.key ?: ""
                            badgeList.add(it)
                        }
                    }
                    // Urutkan berdasarkan level
                    badgeList.sortBy { it.level }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    if (isAdded) progressBar.visibility = View.GONE
                }
            })
    }
}
