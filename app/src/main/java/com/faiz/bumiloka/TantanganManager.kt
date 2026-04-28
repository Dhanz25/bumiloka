package com.faiz.bumiloka.data

import android.content.Context
import com.faiz.bumiloka.AktivitasManager

object ChallengeManager {

    private const val PREF_NAME = "challenge_pref"

    // ==========================
    // MULAI TANTANGAN
    // ==========================
    fun mulaiTantangan(context: Context) {

        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val startDate = System.currentTimeMillis()
        val endDate = startDate + (7 * 24 * 60 * 60 * 1000)

        editor.putString("challengeName", "Penjelajah Mingguan")
        editor.putInt("totalMateri", 3)
        editor.putInt("materiSelesai", 0)
        editor.putBoolean("kuisSelesai", false)
        editor.putInt("progress", 0)
        editor.putLong("startDate", startDate)
        editor.putLong("endDate", endDate)
        editor.putString("status", "aktif")

        editor.apply()
    }

    // ==========================
    // UPDATE SAAT BACA MATERI
    // ==========================
    fun updateProgressMateri(context: Context): Pair<Int, Int> {

        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val totalMateri = sharedPref.getInt("totalMateri", 3)
        var materiSelesai = sharedPref.getInt("materiSelesai", 0)

        if (materiSelesai < totalMateri) {
            materiSelesai++
        }

        val progress = ((materiSelesai.toDouble() / totalMateri) * 75).toInt()

        sharedPref.edit()
            .putInt("materiSelesai", materiSelesai)
            .putInt("progress", progress)
            .apply()

        return Pair(materiSelesai, totalMateri)
    }

    // ==========================
    // UPDATE SAAT KUIS
    // ==========================
    fun updateProgressKuis(context: Context) {

        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        sharedPref.edit()
            .putBoolean("kuisSelesai", true)
            .putInt("progress", 100)
            .putString("status", "selesai")
            .apply()
    }

    // ==========================
    // LOAD DATA
    // ==========================
    fun loadChallenge(context: Context): ChallengeData {

        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        return ChallengeData(
            materiSelesai = sharedPref.getInt("materiSelesai", 0),
            totalMateri = sharedPref.getInt("totalMateri", 3),
            progress = sharedPref.getInt("progress", 0),
            status = sharedPref.getString("status", "belum_mulai") ?: "belum_mulai"
        )
    }
    // CEK APAKAH TANTANGAN EXPIRED
// ==========================
    fun isChallengeExpired(context: Context): Boolean {

        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Ambil endDate
        val endDate = sharedPref.getLong("endDate", 0L)

        // Kalau belum pernah mulai tantangan
        if (endDate == 0L) {
            return false
        }

        // Jika waktu sekarang lebih besar dari endDate
        return System.currentTimeMillis() > endDate
    }

    // ==========================
    // DATA CLASS
    // ==========================
    data class ChallengeData(
        val materiSelesai: Int,
        val totalMateri: Int,
        val progress: Int,
        val status: String
    )
}