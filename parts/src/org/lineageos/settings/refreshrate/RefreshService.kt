package org.lineageos.settings.refreshrate

import android.app.ActivityTaskManager
import android.app.Service
import android.app.TaskStackListener
import android.content.Intent
import android.os.IBinder

class RefreshService : Service() {
    private lateinit var utils: RefreshUtils
    private var previous = ""
    private val tasks = ActivityTaskManager.getService()
    private val listener = object : TaskStackListener() {
        override fun onTaskStackChanged() {
            runCatching {
                val pkg = tasks.focusedRootTaskInfo?.topActivity?.packageName ?: return
                if (!RefreshUtils.isAppInList) utils.getOldRate()
                if (pkg != previous) { utils.setRefreshRate(pkg); previous = pkg }
                if (utils.getStateForPackage(pkg) in RefreshUtils.STATE_60_LAND..RefreshUtils.STATE_120_LAND && RefreshUtils.isAppInList) utils.checkOrientationAndSetRate(pkg)
            }
        }
    }
    override fun onCreate() { super.onCreate(); utils = RefreshUtils(this); runCatching { tasks.registerTaskStackListener(listener) } }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY
    override fun onDestroy() { runCatching { tasks.unregisterTaskStackListener(listener) }; utils.close(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
