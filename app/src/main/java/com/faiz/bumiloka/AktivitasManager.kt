package com.faiz.bumiloka

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth

object AktivitasManager {

    private const val PREF_BASE_NAME = "AKTIVITAS_PREF_"
    private const val KEY = "DATA_AKTIVITAS"

    private val gson = Gson()

    private fun getUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    }

    fun tambahAktivitas(context: Context, title: String, type: String, points: Int) {
        val userId = getUserId()
        val list = getAktivitas(context).toMutableList()

        val aktivitas = AktivitasModel(
            title = title,
            type = type,
            points = points,
            timestamp = System.currentTimeMillis()
        )

        list.add(0, aktivitas)
        simpanAktivitas(context, list, userId)
    }

    fun getAktivitas(context: Context): List<AktivitasModel> {
        val userId = getUserId()
        val pref = context.getSharedPreferences(PREF_BASE_NAME + userId, Context.MODE_PRIVATE)
        val json = pref.getString(KEY, null)

        return if (json != null) {
            val type = object : TypeToken<List<AktivitasModel>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    private fun simpanAktivitas(context: Context, list: List<AktivitasModel>, userId: String) {
        val pref = context.getSharedPreferences(PREF_BASE_NAME + userId, Context.MODE_PRIVATE)
        pref.edit().putString(KEY, gson.toJson(list)).apply()
    }
}
