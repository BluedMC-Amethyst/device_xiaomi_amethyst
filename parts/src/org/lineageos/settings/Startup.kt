package org.lineageos.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.ServiceManager
import android.os.IBinder
import android.util.Log
import androidx.preference.PreferenceManager

class Startup : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_REBOOT) {
            Handler(Looper.getMainLooper()).postDelayed({ applySaturation(context) }, 5000)
        }
    }
    private fun applySaturation(context: Context) {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.KEY_SATURATION, 100)
        val saturation = if (value == 100) 1.001f else value / 100f
        val surfaceFlinger: IBinder = ServiceManager.getService("SurfaceFlinger") ?: return
        val data = Parcel.obtain()
        try {
            data.writeInterfaceToken("android.ui.ISurfaceComposer")
            data.writeFloat(saturation)
            surfaceFlinger.transact(1022, data, null, 0)
        } catch (e: Exception) {
            Log.e("Startup", "Failed to apply saturation", e)
        } finally {
            data.recycle()
        }
    }
}
