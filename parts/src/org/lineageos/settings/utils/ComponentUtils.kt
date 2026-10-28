package org.lineageos.settings.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object ComponentUtils {
    fun toggleComponent(context: Context, componentClass: Class<*>, enable: Boolean) {
        val name = ComponentName(context, componentClass)
        val manager = context.packageManager
        val state = if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        if (manager.getComponentEnabledSetting(name) != state) {
            manager.setComponentEnabledSetting(name, state, PackageManager.DONT_KILL_APP)
        }
    }
}
