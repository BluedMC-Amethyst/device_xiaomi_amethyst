package org.lineageos.settings.refreshrate

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.UserHandle
import android.provider.Settings
import android.view.OrientationEventListener
import androidx.preference.PreferenceManager

class RefreshUtils(private val context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private var defaultMax = 120f
    private var defaultMin = 120f
    private var listener: OrientationEventListener? = null
    private var landscape: Boolean? = null

    fun getOldRate() { defaultMax = rate("peak_refresh_rate"); defaultMin = rate("min_refresh_rate") }
    fun getStateForPackage(pkg: String): Int = modes().indexOfFirst { it.contains("$pkg,") }.let { if (it < 0) STATE_DEFAULT else it + 1 }
    fun writePackage(pkg: String, state: Int) {
        val list = modes().map { it.replace("$pkg,", "") }.toMutableList()
        if (state in 1..6) list[state - 1] += "$pkg,"
        prefs.edit().putString(KEY, list.joinToString(":")).apply()
    }
    fun setRefreshRate(pkg: String) {
        val state = getStateForPackage(pkg); isAppInList = state != STATE_DEFAULT
        if (state in STATE_60_LAND..STATE_120_LAND) { listen(pkg); checkOrientationAndSetRate(pkg); return }
        closeListener()
        val value = when (state) { STATE_60 -> 60f; STATE_90 -> 90f; STATE_120 -> 120f; else -> null }
        apply(value ?: defaultMin, value ?: defaultMax)
    }
    fun checkOrientationAndSetRate(pkg: String) {
        val landscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val state = getStateForPackage(pkg)
        val value = when (state) { STATE_60_LAND -> 60f; STATE_90_LAND -> 90f; STATE_120_LAND -> 120f; else -> null }
        if (landscape && value != null) apply(value, value) else apply(defaultMin, defaultMax)
    }
    private fun listen(pkg: String) {
        closeListener()
        landscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        listener = object : OrientationEventListener(context) { override fun onOrientationChanged(orientation: Int) {
            if (orientation == ORIENTATION_UNKNOWN) return
            val current = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            if (current != landscape) { landscape = current; checkOrientationAndSetRate(pkg) }
        } }.also { if (it.canDetectOrientation()) it.enable() }
    }
    private fun rate(key: String) = Settings.System.getFloat(context.contentResolver, key, 120f)
    private fun apply(min: Float, max: Float) {
        if (rate("min_refresh_rate") != min) Settings.System.putFloat(context.contentResolver, "min_refresh_rate", min)
        if (rate("peak_refresh_rate") != max) Settings.System.putFloat(context.contentResolver, "peak_refresh_rate", max)
    }
    fun close() = closeListener()
    private fun closeListener() { listener?.disable(); listener = null; landscape = null }
    private fun modes(): List<String> {
        val defaults = listOf("refresh.60=", "refresh.90=", "refresh.120=", "refresh.60land=", "refresh.90land=", "refresh.120land=")
        val current = prefs.getString(KEY, null)?.split(":") ?: emptyList()
        val result = defaults.mapIndexed { i, value -> current.getOrNull(i) ?: value }
        if (current.size != defaults.size) prefs.edit().putString(KEY, result.joinToString(":")).apply()
        return result
    }
    companion object {
        const val STATE_DEFAULT = 0; const val STATE_60 = 1; const val STATE_90 = 2; const val STATE_120 = 3
        const val STATE_60_LAND = 4; const val STATE_90_LAND = 5; const val STATE_120_LAND = 6
        @JvmField var isAppInList = false
        private const val KEY = "refresh_control"
        @JvmStatic fun startService(context: Context) = context.startServiceAsUser(Intent(context, RefreshService::class.java), UserHandle.CURRENT)
    }
}
