/*
 * Copyright (C) 2025 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.charge

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import org.lineageos.settings.chargelimit.ChargeLimitUtils
import org.lineageos.settings.utils.FileUtils

class ChargeUtils(context: Context) {

    private val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun isBypassChargeEnabled(): Boolean {
        return try {
            if (sharedPrefs.getBoolean(ChargeLimitUtils.PREF_PAUSED, false)) {
                sharedPrefs.getBoolean(PREF_BYPASS_CHARGE, false)
            } else {
                FileUtils.readOneLine(BYPASS_CHARGE_NODE) == "1"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read bypass charge status", e)
            false
        }
    }

    fun enableBypassCharge(enable: Boolean) {
        try {
            sharedPrefs.edit().putBoolean(PREF_BYPASS_CHARGE, enable).apply()
            val limiterPaused = sharedPrefs.getBoolean(ChargeLimitUtils.PREF_PAUSED, false)
            FileUtils.writeLine(BYPASS_CHARGE_NODE, if (enable || limiterPaused) "1" else "0")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write bypass charge status", e)
        }
    }

    private fun isNodeAccessible(node: String): Boolean {
        return try {
            FileUtils.readOneLine(node)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Node $node not accessible", e)
            false
        }
    }

    fun isBypassChargeSupported(): Boolean {
        return isNodeAccessible(BYPASS_CHARGE_NODE)
    }

    companion object {
        private const val TAG = "ChargeUtils"
        const val BYPASS_CHARGE_NODE = "/sys/class/qcom-battery/charge_control_en"
        const val PREF_BYPASS_CHARGE = "bypass_charge"
    }
}
