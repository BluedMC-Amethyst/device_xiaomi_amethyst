package org.lineageos.settings.hypercharge

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.preference.PreferenceManager
import org.lineageos.settings.Constants
import org.lineageos.settings.utils.FileUtils

class HyperChargeService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var monitor: Runnable? = null
    private var pending: Runnable? = null
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_POWER_CONNECTED) schedule() else if (intent.action == Intent.ACTION_POWER_DISCONNECTED) stopMonitoring(true)
        }
    }
    override fun onCreate() {
        super.onCreate()
        registerReceiver(receiver, IntentFilter().apply { addAction(Intent.ACTION_POWER_CONNECTED); addAction(Intent.ACTION_POWER_DISCONNECTED) })
        if (isPluggedIn()) schedule()
    }
    private fun schedule() { stopMonitoring(false); pending = Runnable(::startMonitoring).also { handler.postDelayed(it, 5000) } }
    private fun startMonitoring() {
        if (monitor != null) return
        monitor = object : Runnable {
            override fun run() {
                if (FileUtils.readOneLine(Constants.NODE_FASTCHG_MODE) == "1") {
                    val prefs = PreferenceManager.getDefaultSharedPreferences(this@HyperChargeService)
                    if (!prefs.getBoolean(Constants.KEY_HYPERCHARGE_STATUS, false)) {
                        stopSelf()
                        return
                    }
                    val target = prefs.getString(Constants.KEY_HYPERCHARGE_LIMIT, Constants.CHARGE_LIMIT_120W) ?: Constants.CHARGE_LIMIT_120W
                    if (FileUtils.readOneLine(Constants.NODE_CONSTANT_CHARGE_CURRENT) != target) FileUtils.writeLine(Constants.NODE_CONSTANT_CHARGE_CURRENT, target)
                }
                handler.postDelayed(this, 5000)
            }
        }.also(handler::post)
    }
    private fun stopMonitoring(restoreDefault: Boolean) {
        pending?.let(handler::removeCallbacks); pending = null
        monitor?.let(handler::removeCallbacks); monitor = null
        if (restoreDefault) FileUtils.writeLine(Constants.NODE_CONSTANT_CHARGE_CURRENT, Constants.CHARGE_LIMIT_120W)
    }
    private fun isPluggedIn(): Boolean {
        val battery = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return false
        return battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY
    override fun onDestroy() { unregisterReceiver(receiver); stopMonitoring(true); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
