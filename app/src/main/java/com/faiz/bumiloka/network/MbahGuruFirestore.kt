package com.faiz.bumiloka.network
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore

object MbahGuruFirestore {
    private const val APP_NAME = "MbahGuruApp"

    fun initialize(context: Context) {

        try {

            val options = FirebaseOptions.Builder()
                .setProjectId("mbahguru-68f12")
                .setApplicationId("1:237695256104:android:4992dd79a01ba101cb571d")
                .setApiKey("AIzaSyCpxxUADcEhzWxXUsvyluoscRcpqPKdo5A")
                .build()

            FirebaseApp.initializeApp(
                context,
                options,
                APP_NAME
            )

        } catch (e: Exception) {
            // Firebase kedua sudah pernah dibuat
        }
    }

    fun getFirestore(): FirebaseFirestore {

        return FirebaseFirestore.getInstance(
            FirebaseApp.getInstance(APP_NAME)
        )
    }
}