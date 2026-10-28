package org.lineageos.settings.kprofiles

import android.content.Intent
import android.os.UserHandle
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.preference.PreferenceManager
import org.lineageos.settings.R
import org.lineageos.settings.utils.FileUtils
import org.lineageos.settings.kprofiles.KprofilesSettingsFragment.Companion.INTENT_ACTION
import org.lineageos.settings.kprofiles.KprofilesSettingsFragment.Companion.IS_SUPPORTED
import org.lineageos.settings.kprofiles.KprofilesSettingsFragment.Companion.KPROFILES_MODES_KEY
import org.lineageos.settings.kprofiles.KprofilesSettingsFragment.Companion.KPROFILES_MODES_NODE

class KProfilesModesTileService : TileService() {
    private var registered = false
    private val receiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: Intent) = updateTile()
    }
    override fun onCreate() {
        super.onCreate()
        if (!IS_SUPPORTED) getQsTile()?.apply { state = Tile.STATE_UNAVAILABLE; updateTile() }
    }
    override fun onStartListening() { if (IS_SUPPORTED) { super.onStartListening(); registerReceiver(receiver, android.content.IntentFilter(INTENT_ACTION), RECEIVER_NOT_EXPORTED); registered = true; updateTile() } }
    override fun onStopListening() { if (registered) { unregisterReceiver(receiver); registered = false }; super.onStopListening() }
    override fun onClick() {
        if (!IS_SUPPORTED) return
        val mode = when (getMode()) { "0" -> "1"; "1" -> "2"; "2" -> "3"; else -> "0" }
        FileUtils.writeLine(KPROFILES_MODES_NODE, mode)
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(KPROFILES_MODES_KEY, mode).apply()
        sendBroadcastAsUser(Intent(INTENT_ACTION).setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY), UserHandle.CURRENT)
        updateTile(mode)
    }
    private fun getMode() = FileUtils.readOneLine(KPROFILES_MODES_NODE)?.trim() ?: "0"
    private fun updateTile(value: String = getMode()) {
        getQsTile()?.apply {
            state = if (value == "0") Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            val res = when (value) { "1" -> R.string.kprofiles_modes_battery; "2" -> R.string.kprofiles_modes_balanced; "3" -> R.string.kprofiles_modes_performance; else -> R.string.kprofiles_modes_none }
            contentDescription = getString(res); subtitle = getString(res); updateTile()
        }
    }
}
