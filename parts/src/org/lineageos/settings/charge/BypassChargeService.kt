package org.lineageos.settings.charge

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import org.lineageos.settings.utils.FileUtils

class BypassChargeService : Service() {
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            apply(intent.action != Intent.ACTION_SCREEN_OFF)
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
        apply(true)
    }

    private fun apply(screenOn: Boolean) {
        if (screenOn) {
            FileUtils.writeLine(ChargeUtils.INPUT_SUSPEND_NODE, "0")
            FileUtils.writeLine(ChargeUtils.BYPASS_CHARGE_NODE, "1")
        } else {
            FileUtils.writeLine(ChargeUtils.BYPASS_CHARGE_NODE, "0")
            FileUtils.writeLine(ChargeUtils.INPUT_SUSPEND_NODE, "1")
        }
    }

    override fun onDestroy() {
        unregisterReceiver(screenReceiver)
        FileUtils.writeLine(ChargeUtils.INPUT_SUSPEND_NODE, "0")
        FileUtils.writeLine(ChargeUtils.BYPASS_CHARGE_NODE, "0")
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
}
