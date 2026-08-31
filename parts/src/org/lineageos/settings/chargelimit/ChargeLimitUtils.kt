/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.chargelimit

import android.content.Context
import androidx.preference.PreferenceManager
import org.lineageos.settings.charge.ChargeUtils
import org.lineageos.settings.utils.FileUtils

object ChargeLimitUtils {
    const val CHARGE_CONTROL_NODE = "/sys/class/qcom-battery/charge_control_en"
    const val INPUT_SUSPEND_NODE = "/sys/class/qcom-battery/input_suspend"
    const val PREF_ENABLED = "charge_limit_enabled"
    const val PREF_LIMIT = "charge_limit_percent"
    const val PREF_PAUSED = "charge_limit_paused"
    const val DEFAULT_LIMIT = 80

    private const val HYSTERESIS = 2

    fun isSupported(): Boolean =
        FileUtils.isFileWritable(CHARGE_CONTROL_NODE) &&
            FileUtils.isFileWritable(INPUT_SUSPEND_NODE)

    fun evaluate(context: Context, batteryLevel: Int) {
        if (batteryLevel !in 0..100) return

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (!prefs.getBoolean(PREF_ENABLED, false)) {
            resumeCharging(context)
            return
        }

        val limit = prefs.getInt(PREF_LIMIT, DEFAULT_LIMIT).coerceIn(70, 100)
        val paused = prefs.getBoolean(PREF_PAUSED, false)
        when {
            !paused && batteryLevel >= limit -> pauseCharging(context)
            paused && batteryLevel <= limit - HYSTERESIS -> resumeCharging(context)
        }
    }

    fun pauseCharging(context: Context) {
        val controlWritten = FileUtils.writeLine(CHARGE_CONTROL_NODE, "1")
        val inputWritten = FileUtils.writeLine(INPUT_SUSPEND_NODE, "1")
        if (controlWritten && inputWritten) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_PAUSED, true)
                .apply()
        }
    }

    fun resumeCharging(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val bypassEnabled = prefs.getBoolean(ChargeUtils.PREF_BYPASS_CHARGE, false)

        // Restore the bypass owner's value before allowing USB input again.
        FileUtils.writeLine(CHARGE_CONTROL_NODE, if (bypassEnabled) "1" else "0")
        FileUtils.writeLine(INPUT_SUSPEND_NODE, "0")
        prefs.edit().putBoolean(PREF_PAUSED, false).apply()
    }
}
