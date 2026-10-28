package org.lineageos.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.Display.HdrCapabilities
import android.util.Log
import androidx.preference.PreferenceManager
import org.lineageos.settings.charge.BypassChargeService
import org.lineageos.settings.charge.ChargeUtils
import org.lineageos.settings.chargelimit.ChargeLimitService
import org.lineageos.settings.chargelimit.ChargeLimitUtils
import org.lineageos.settings.hypercharge.HyperChargeService
import org.lineageos.settings.kprofiles.KprofilesSettingsFragment
import org.lineageos.settings.refreshrate.RefreshUtils
import org.lineageos.settings.thermal.ThermalUtils
import org.lineageos.settings.utils.FileUtils

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!initialized) runCatching { initialize(context) }
            .onSuccess { initialized = true }
            .onFailure { Log.e("XiaomiParts", "Boot initialization failed", it) }
        if (intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            ThermalUtils.getInstance(context).startService(); RefreshUtils.startService(context)
            context.getSystemService(DisplayManager::class.java)?.overrideHdrTypes(Display.DEFAULT_DISPLAY, intArrayOf(
                HdrCapabilities.HDR_TYPE_DOLBY_VISION, HdrCapabilities.HDR_TYPE_HDR10,
                HdrCapabilities.HDR_TYPE_HLG, HdrCapabilities.HDR_TYPE_HDR10_PLUS
            ))
        }
    }
    private fun initialize(context: Context) {
        PreferenceManager.setDefaultValues(context, R.xml.hypercharge_settings, false)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.getBoolean(ChargeLimitUtils.PREF_ENABLED, false)) context.startService(Intent(context, ChargeLimitService::class.java))
        if (prefs.getBoolean(ChargeUtils.PREF_BYPASS_CHARGE, false)) context.startService(Intent(context, BypassChargeService::class.java))
        val enabled = runCatching { prefs.getBoolean(Constants.KEY_HYPERCHARGE_STATUS, true) }.getOrDefault(true)
        val limit = runCatching { prefs.getString(Constants.KEY_HYPERCHARGE_LIMIT, Constants.CHARGE_LIMIT_120W) }.getOrNull()
        if (enabled && limit != Constants.CHARGE_LIMIT_120W) context.startService(Intent(context, HyperChargeService::class.java))
        if (FileUtils.fileExists(KprofilesSettingsFragment.KPROFILES_AUTO_NODE)) FileUtils.writeLine(KprofilesSettingsFragment.KPROFILES_AUTO_NODE, if (prefs.getBoolean(KprofilesSettingsFragment.KPROFILES_AUTO_KEY, false)) KprofilesSettingsFragment.ON else KprofilesSettingsFragment.OFF)
        if (KprofilesSettingsFragment.IS_SUPPORTED) FileUtils.writeLine(KprofilesSettingsFragment.KPROFILES_MODES_NODE, prefs.getString(KprofilesSettingsFragment.KPROFILES_MODES_KEY, FileUtils.readOneLine(KprofilesSettingsFragment.KPROFILES_MODES_NODE)) ?: "0")
    }
    companion object { private var initialized = false }
}
