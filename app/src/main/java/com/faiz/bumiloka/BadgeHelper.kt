package com.faiz.bumiloka

import android.content.Context

object BadgeHelper {

    private const val PREF_NAME = "BADGE_PREF"

    fun tambahBadge(
        context: Context,
        badgeId: String
    ) {
        val pref = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )

        if (!pref.getBoolean(badgeId, false)) {

            pref.edit()
                .putBoolean(badgeId, true)
                .apply()
        }
    }

    fun getTotalBadge(
        context: Context
    ): Int {

        val pref = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )

        return pref.all.count {
            it.value == true
        }
    }

    fun punyaBadge(
        context: Context,
        badgeId: String
    ): Boolean {

        return context
            .getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )
            .getBoolean(badgeId, false)
    }
}