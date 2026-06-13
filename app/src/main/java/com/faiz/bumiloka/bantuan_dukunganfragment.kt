package com.faiz.bumiloka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import android.content.Intent
import android.net.Uri
import androidx.appcompat.widget.Toolbar

class bantuan_dukungan : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_bantuan_dukungan,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val cardTentang =
            view.findViewById<View>(R.id.cardAbout)

        val cardFaq =
            view.findViewById<View>(R.id.cardFaq)

        val cardHubungi =
            view.findViewById<View>(R.id.cardContact)

        val privacyPolicy =
            view.findViewById<View>(R.id.rowPrivacyPolicy)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }


        cardTentang.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    tentang_aplikasi()
                )
                .addToBackStack(null)
                .commit()
        }

        cardFaq.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    faqfragment()
                )
                .addToBackStack(null)
                .commit()
        }

        cardHubungi.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    hubungi_kami()
                )
                .addToBackStack(null)
                .commit()
        }

        privacyPolicy.setOnClickListener {

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://doc-hosting.flycricket.io/bumiloka-terms-of-use/5e84f1f7-aba9-4217-bb44-41ba44a902e7/terms")
            )

            startActivity(intent)
        }
    }
}
