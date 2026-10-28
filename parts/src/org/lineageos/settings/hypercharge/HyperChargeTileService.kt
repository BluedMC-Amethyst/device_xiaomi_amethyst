package org.lineageos.settings.hypercharge

import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.preference.PreferenceManager
import org.lineageos.settings.Constants
import org.lineageos.settings.R

class HyperChargeTileService : TileService() {
    private lateinit var prefs: SharedPreferences
    override fun onCreate() { super.onCreate(); prefs = PreferenceManager.getDefaultSharedPreferences(this) }
    override fun onStartListening() { super.onStartListening(); updateTile() }
    override fun onClick() { super.onClick(); toggle(); updateTile() }

    private fun toggle() {
        val enabled = !prefs.getBoolean(Constants.KEY_HYPERCHARGE_STATUS, false)
        prefs.edit().putBoolean(Constants.KEY_HYPERCHARGE_STATUS, enabled).apply()
        val limit = prefs.getString(Constants.KEY_HYPERCHARGE_LIMIT, Constants.CHARGE_LIMIT_120W)
        val intent = android.content.Intent(this, HyperChargeService::class.java)
        if (!enabled || limit == Constants.CHARGE_LIMIT_120W) stopService(intent) else startService(intent)
    }

    private fun updateTile() {
        getQsTile()?.let { tile ->
            val enabled = prefs.getBoolean(Constants.KEY_HYPERCHARGE_STATUS, false)
            val limit = prefs.getString(Constants.KEY_HYPERCHARGE_LIMIT, Constants.CHARGE_LIMIT_120W)!!
            tile.label = getString(R.string.hypercharge_tile_label)
            tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.icon = Icon.createWithResource(this, if (enabled) R.drawable.ic_qs_hypercharge_on else R.drawable.ic_qs_hypercharge_off)
            tile.subtitle = if (enabled) getString(when (limit) {
                Constants.CHARGE_LIMIT_120W -> R.string.hypercharge_speed_120w
                Constants.CHARGE_LIMIT_90W -> R.string.hypercharge_speed_90w
                Constants.CHARGE_LIMIT_67W -> R.string.hypercharge_speed_67w
                Constants.CHARGE_LIMIT_50W -> R.string.hypercharge_speed_50w
                Constants.CHARGE_LIMIT_33W -> R.string.hypercharge_speed_33w
                Constants.CHARGE_LIMIT_18W -> R.string.hypercharge_speed_18w
                else -> R.string.hypercharge_speed_10w
            }) else getString(R.string.tile_off)
            tile.updateTile()
        }
    }
}
