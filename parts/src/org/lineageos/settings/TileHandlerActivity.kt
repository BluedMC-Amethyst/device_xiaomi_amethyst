package org.lineageos.settings

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.service.quicksettings.TileService
import org.lineageos.settings.charge.ChargeActivity
import org.lineageos.settings.charge.ChargeTileService
import org.lineageos.settings.hypercharge.HyperChargeSettingsActivity
import org.lineageos.settings.hypercharge.HyperChargeTileService

class TileHandlerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action != TileService.ACTION_QS_TILE_PREFERENCES) return finish()
        val tile = intent.getParcelableExtra<ComponentName>(Intent.EXTRA_COMPONENT_NAME) ?: return finish()
        val target = mapOf(ChargeTileService::class.java.name to ChargeActivity::class.java, HyperChargeTileService::class.java.name to HyperChargeSettingsActivity::class.java)[tile.className]
        startActivity((target?.let { Intent(this, it) } ?: Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", tile.packageName, null))).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }
}
