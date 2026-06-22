package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.faiz.bumiloka.databinding.FragmentStatistikBinding

class StatistikFragment : Fragment() {

    private var _binding: FragmentStatistikBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatistikBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewModel.fetchStatistik()
    }

    private fun observeViewModel() {
        viewModel.statistik.observe(viewLifecycleOwner) { stats ->
            binding.tvCountEdukasi.text = (stats["edukasi"] ?: 0).toString()
            binding.tvCountKuis.text = (stats["kuis"] ?: 0).toString()
            binding.tvCountSoal.text = (stats["soal"] ?: 0).toString()
            binding.tvCountChallenge.text = (stats["tantangan"] ?: 0).toString()
            binding.tvCountUser.text = (stats["users"] ?: 0).toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
