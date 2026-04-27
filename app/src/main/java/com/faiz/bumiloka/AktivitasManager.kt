package com.faiz.bumiloka

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AktivitasManager {

    private const val PREF_NAME = "AKTIVITAS_PREF"
    private const val KEY = "DATA_AKTIVITAS"

    private val gson = Gson()

    fun tambahAktivitas(context: Context, title: String, type: String, points: Int) {

        val list = getAktivitas(context).toMutableList()

        val aktivitas = AktivitasModel(
            title = title,
            type = type,
            points = points,
            timestamp = System.currentTimeMillis()
        )

        list.add(0, aktivitas)

        simpanAktivitas(context, list)
    }

    fun getAktivitas(context: Context): List<AktivitasModel> {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = pref.getString(KEY, null)

        return if (json != null) {
            val type = object : TypeToken<List<AktivitasModel>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    private fun simpanAktivitas(context: Context, list: List<AktivitasModel>) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(KEY, gson.toJson(list)).apply()
    }
}
