package org.lineageos.settings.charge

import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.preference.PreferenceManager
import org.lineageos.settings.R

class ChargeTileService : TileService() {
    private lateinit var prefs: SharedPreferences
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "bypass_charge") updateTile()
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onStartListening() { super.onStartListening(); updateTile() }
    override fun onDestroy() { prefs.unregisterOnSharedPreferenceChangeListener(listener); super.onDestroy() }

    override fun onClick() {
        val enabled = !prefs.getBoolean("bypass_charge", false)
        ChargeUtils(this).enableBypassCharge(enabled)
        updateTile()
    }

    private fun updateTile() {
        getQsTile()?.let { tile ->
            val enabled = prefs.getBoolean("bypass_charge", false)
            tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.icon = Icon.createWithResource(this, if (enabled) R.drawable.ic_bypass_charge_enabled else R.drawable.ic_bypass_charge_disabled)
            tile.label = getString(R.string.bypass_charge_tile_label)
            tile.subtitle = getString(if (enabled) R.string.bypass_charge_tile_enabled_subtitle else R.string.bypass_charge_tile_disabled_subtitle)
            tile.updateTile()
        }
    }
}
