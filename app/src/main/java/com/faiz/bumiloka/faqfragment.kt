package com.faiz.bumiloka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.faiz.bumiloka.adapters.FaqAdapter
import com.faiz.bumiloka.databinding.FragmentFaqBinding
import com.faiz.bumiloka.model.Faq

class faqfragment : Fragment() {

    private var _binding: FragmentFaqBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupFooter()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        val faqList = listOf(
            Faq(
                getString(R.string.faq_q1),
                getString(R.string.faq_a1)
            ),
            Faq(
                getString(R.string.faq_q2),
                getString(R.string.faq_a2)
            ),
            Faq(
                getString(R.string.faq_q3),
                getString(R.string.faq_a3)
            ),
            Faq(
                getString(R.string.faq_q4),
                getString(R.string.faq_a4)
            ),
            Faq(
                getString(R.string.faq_q5),
                getString(R.string.faq_a5)
            ),
            Faq(
                getString(R.string.faq_q6),
                getString(R.string.faq_a6)
            ),
            Faq(
                getString(R.string.faq_q7),
                getString(R.string.faq_a7)
            )
        )

        binding.rvFaq.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = FaqAdapter(faqList)
            // Optional: for smoother scrolling with NestedScrollView
            isNestedScrollingEnabled = false
        }
    }

    private fun setupFooter() {
        binding.btnContactUs.setOnClickListener {
            // Replace R.id.fragment_container with your actual container ID
            // and ensure HubungiKamiFragment exists.
            /*
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HubungiKamiFragment())
                .addToBackStack(null)
                .commit()
            */
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = faqfragment()
    }
}
