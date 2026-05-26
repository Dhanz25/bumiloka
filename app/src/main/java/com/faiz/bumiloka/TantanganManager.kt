package com.faiz.bumiloka

import android.content.Context

data class ChallengeData(
    val materiSelesai: Int,
    val totalMateri: Int,
    val status: String,
    val progress: Int,
    val deadline: Long
)

object TantanganManager {

    private const val PREF_NAME = "tantangan_pref"

    private const val KEY_MATERI = "materi_selesai"
    private const val KEY_STATUS = "status"
    private const val KEY_PROGRESS = "progress"
    private const val KEY_DEADLINE = "deadline"

    private const val TOTAL_MATERI = 3

    // =========================
    // LOAD DATA
    // =========================
    fun loadChallenge(context: Context): ChallengeData {

        val prefs =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val materi =
            prefs.getInt(KEY_MATERI, 0)

        val status =
            prefs.getString(KEY_STATUS, "belum") ?: "belum"

        val progress =
            prefs.getInt(KEY_PROGRESS, 0)

        val deadline =
            prefs.getLong(
                KEY_DEADLINE,
                System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
            )

        return ChallengeData(
            materiSelesai = materi,
            totalMateri = TOTAL_MATERI,
            status = status,
            progress = progress,
            deadline = deadline
        )
    }

    // =========================
    // UPDATE PROGRESS MATERI
    // =========================
    fun updateProgressMateri(
        context: Context
    ): Pair<Int, Int> {

        val prefs =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        var materi =
            prefs.getInt(KEY_MATERI, 0)

        if (materi < TOTAL_MATERI) {
            materi++
        }

        val progress =
            ((materi.toDouble() / TOTAL_MATERI) * 80).toInt()

        prefs.edit()
            .putInt(KEY_MATERI, materi)
            .putInt(KEY_PROGRESS, progress)
            .apply()

        return Pair(materi, TOTAL_MATERI)
    }

    // =========================
    // UPDATE PROGRESS KUIS
    // =========================
    fun updateProgressKuis(context: Context) {

        val prefs =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .putString(KEY_STATUS, "selesai")
            .putInt(KEY_PROGRESS, 100)
            .apply()
    }

    // =========================
    // RESET
    // =========================
    fun resetChallenge(context: Context) {

        val prefs =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        prefs.edit().clear().apply()
    }

    // =========================
    // CEK DEADLINE
    // =========================
    fun isChallengeExpired(context: Context): Boolean {

        val challenge = loadChallenge(context)

        return System.currentTimeMillis() > challenge.deadline
    }
}