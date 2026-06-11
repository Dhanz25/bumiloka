package com.faiz.bumiloka

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.databinding.FragmentHubungiKamiBinding
import com.google.android.material.snackbar.Snackbar

class hubungi_kami : Fragment() {

    private var _binding: FragmentHubungiKamiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHubungiKamiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupContactButtons()
        setupForm()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupContactButtons() {
        // Email Intent
        binding.btnEmail.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:kelompok3pmo@gmail.com")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.contact_email_address)))
                putExtra(Intent.EXTRA_SUBJECT, "Tanya Eco Warrior")
            }
            try {
                startActivity(Intent.createChooser(emailIntent, "Pilih aplikasi email"))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Aplikasi email tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }

        // WhatsApp Intent
        binding.btnWhatsapp.setOnClickListener {

            val phoneNumber = "62882003407888"

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/62882003407888")
            )

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "WhatsApp tidak ditemukan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupForm() {
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                // Success
                Snackbar.make(binding.root, getString(R.string.contact_form_success), Snackbar.LENGTH_LONG).show()
                clearForm()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val message = binding.etMessage.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.contact_form_error_empty)
            isValid = false
        } else {
            binding.tilName.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.contact_form_error_empty)
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Format email tidak valid"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (message.isEmpty()) {
            binding.tilMessage.error = getString(R.string.contact_form_error_empty)
            isValid = false
        } else {
            binding.tilMessage.error = null
        }

        return isValid
    }

    private fun clearForm() {
        binding.etName.text?.clear()
        binding.etEmail.text?.clear()
        binding.etMessage.text?.clear()

        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilMessage.error = null

        binding.etName.clearFocus()
        binding.etEmail.clearFocus()
        binding.etMessage.clearFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = hubungi_kami()
    }
}
