package com.faiz.bumiloka.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.faiz.bumiloka.R
import com.faiz.bumiloka.databinding.FragmentDashboardAdminBinding
import com.faiz.bumiloka.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class DashboardAdminFragment : Fragment() {

    private var _binding: FragmentDashboardAdminBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setupUI()
        observeViewModel()
        viewModel.fetchStatistik()
    }

    private fun setupUI() {
        binding.tvAdminName.text = "Halo, ${auth.currentUser?.displayName ?: "Admin"} 👋"

        binding.cardEdukasi.setOnClickListener {
            navigateTo(EdukasiFragment())
        }

        binding.cardKuis.setOnClickListener {
            navigateTo(KuisFragment())
        }

        binding.cardTantangan.setOnClickListener {
            navigateTo(TantanganFragment())
        }

        binding.cardStatistik.setOnClickListener {
            navigateTo(StatistikFragment())
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.statistik.observe(viewLifecycleOwner) { stats ->
            binding.tvCountEdukasi.text = "${stats["edukasi"] ?: 0} Materi"
            binding.tvCountKuis.text = "${stats["kuis"] ?: 0} Kuis"
            binding.tvCountTantangan.text = "${stats["tantangan"] ?: 0} Tantangan"
        }
    }

    private fun navigateTo(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Yakin ingin keluar dari admin?")
            .setPositiveButton("Ya") { _, _ ->
                auth.signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
