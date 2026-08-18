package org.lineageos.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.lineageos.settings.gpumanager.GpuManagerUtils
import org.lineageos.settings.gpumanager.GpuManagerViewModel
import org.lineageos.settings.kernelmanager.KernelManagerUtils
import org.lineageos.settings.kernelmanager.KernelManagerViewModel

class ApplyOnBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                applyKernelSettings(context)
                applyGpuSettings(context)
            }
        }
    }

    private fun applyKernelSettings(context: Context) {
        val prefs = context.getSharedPreferences(
            KernelManagerViewModel.PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KernelManagerViewModel.PREF_APPLY_ON_BOOT, false)) return

        val governor = prefs.getString(KernelManagerViewModel.PREF_GOVERNOR, null)
            ?: return
        KernelManagerUtils.setGovernor(governor)

        val effMin = prefs.getString(KernelManagerViewModel.PREF_EFF_MIN, null)
        val effMax = prefs.getString(KernelManagerViewModel.PREF_EFF_MAX, null)
        if (effMin != null && effMax != null) {
            KernelManagerUtils.setFrequencyRange(
                KernelManagerUtils.EFFICIENCY_CLUSTER, effMin, effMax)
        }

        val perfMin = prefs.getString(KernelManagerViewModel.PREF_PERF_MIN, null)
        val perfMax = prefs.getString(KernelManagerViewModel.PREF_PERF_MAX, null)
        if (perfMin != null && perfMax != null) {
            KernelManagerUtils.setFrequencyRange(
                KernelManagerUtils.PERFORMANCE_CLUSTER, perfMin, perfMax)
        }
    }

    private fun applyGpuSettings(context: Context) {
        val prefs = context.getSharedPreferences(
            GpuManagerViewModel.PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(GpuManagerViewModel.PREF_APPLY_ON_BOOT, false)) return

        val utils = GpuManagerUtils()
        val governor = prefs.getString(GpuManagerViewModel.PREF_GOVERNOR, null)
            ?: return
        utils.setGovernor(governor)

        val minFreq = prefs.getString(GpuManagerViewModel.PREF_MIN_FREQ, null)
        val maxFreq = prefs.getString(GpuManagerViewModel.PREF_MAX_FREQ, null)
        if (minFreq != null && maxFreq != null) {
            utils.setFrequencyRange(minFreq, maxFreq)
        }

        utils.setForceClkOn(prefs.getBoolean(GpuManagerViewModel.PREF_FORCE_CLK_ON, false))
        utils.setForceBusOn(prefs.getBoolean(GpuManagerViewModel.PREF_FORCE_BUS_ON, false))
        utils.setForceRailOn(prefs.getBoolean(GpuManagerViewModel.PREF_FORCE_RAIL_ON, false))
        utils.setForceNoNap(prefs.getBoolean(GpuManagerViewModel.PREF_FORCE_NO_NAP, true))
        utils.setBusSplit(prefs.getBoolean(GpuManagerViewModel.PREF_BUS_SPLIT, true))
    }
}
