package com.faiz.bumiloka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

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
    }
}
