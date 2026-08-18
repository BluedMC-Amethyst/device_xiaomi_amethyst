/*
 * Copyright (C) 2025 KamiKaonashi
 *           (C) 2026 zylhdrXP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

package org.lineageos.settings.kernelmanager

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class KernelState(
    val availableGovernors: List<String> = emptyList(),
    val currentGovernor: String = "",
    val effAvailableFreqs: List<String> = emptyList(),
    val effMinFreq: String = "",
    val effMaxFreq: String = "",
    val perfAvailableFreqs: List<String> = emptyList(),
    val perfMinFreq: String = "",
    val perfMaxFreq: String = "",
    val applyOnBoot: Boolean = false,
)

class KernelManagerViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(KernelState())
    val state: StateFlow<KernelState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        val govs = KernelManagerUtils.getAvailableGovernors()
        val curGov = prefs.getString(PREF_GOVERNOR, null)
            ?: KernelManagerUtils.getCurrentGovernor(KernelManagerUtils.EFFICIENCY_CLUSTER)
        
        val effFreqs = KernelManagerUtils.getAvailableFrequencies(KernelManagerUtils.EFFICIENCY_CLUSTER) ?: emptyList()
        val effMin = prefs.getString(PREF_EFF_MIN, null)
            ?: KernelManagerUtils.getCurrentMinFrequency(KernelManagerUtils.EFFICIENCY_CLUSTER)
        val effMax = prefs.getString(PREF_EFF_MAX, null)
            ?: KernelManagerUtils.getCurrentMaxFrequency(KernelManagerUtils.EFFICIENCY_CLUSTER)
        
        val perfFreqs = KernelManagerUtils.getAvailableFrequencies(KernelManagerUtils.PERFORMANCE_CLUSTER) ?: emptyList()
        val perfMin = prefs.getString(PREF_PERF_MIN, null)
            ?: KernelManagerUtils.getCurrentMinFrequency(KernelManagerUtils.PERFORMANCE_CLUSTER)
        val perfMax = prefs.getString(PREF_PERF_MAX, null)
            ?: KernelManagerUtils.getCurrentMaxFrequency(KernelManagerUtils.PERFORMANCE_CLUSTER)

        _state.update {
            it.copy(
                availableGovernors = govs,
                currentGovernor = curGov,
                effAvailableFreqs = effFreqs,
                effMinFreq = effMin,
                effMaxFreq = effMax,
                perfAvailableFreqs = perfFreqs,
                perfMinFreq = perfMin,
                perfMaxFreq = perfMax,
                applyOnBoot = prefs.getBoolean(PREF_APPLY_ON_BOOT, false)
            )
        }
    }

    fun updateGovernor(gov: String) {
        _state.update { it.copy(currentGovernor = gov) }
    }

    fun updateEffMinFreq(freq: String) {
        _state.update { it.copy(effMinFreq = freq) }
    }

    fun updateEffMaxFreq(freq: String) {
        _state.update { it.copy(effMaxFreq = freq) }
    }

    fun updatePerfMinFreq(freq: String) {
        _state.update { it.copy(perfMinFreq = freq) }
    }

    fun updatePerfMaxFreq(freq: String) {
        _state.update { it.copy(perfMaxFreq = freq) }
    }

    fun setApplyOnBoot(enabled: Boolean) {
        _state.update { it.copy(applyOnBoot = enabled) }
        prefs.edit().putBoolean(PREF_APPLY_ON_BOOT, enabled).apply()
    }

    fun applySettings() {
        val s = state.value
        KernelManagerUtils.setGovernor(s.currentGovernor)
        KernelManagerUtils.setFrequencyRange(KernelManagerUtils.EFFICIENCY_CLUSTER, s.effMinFreq, s.effMaxFreq)
        KernelManagerUtils.setFrequencyRange(KernelManagerUtils.PERFORMANCE_CLUSTER, s.perfMinFreq, s.perfMaxFreq)
        prefs.edit()
            .putString(PREF_GOVERNOR, s.currentGovernor)
            .putString(PREF_EFF_MIN, s.effMinFreq)
            .putString(PREF_EFF_MAX, s.effMaxFreq)
            .putString(PREF_PERF_MIN, s.perfMinFreq)
            .putString(PREF_PERF_MAX, s.perfMaxFreq)
            .apply()
    }

    fun resetSettings() {
        val keepApplyOnBoot = prefs.getBoolean(PREF_APPLY_ON_BOOT, false)
        prefs.edit().clear().putBoolean(PREF_APPLY_ON_BOOT, keepApplyOnBoot).apply()
        KernelManagerUtils.resetToDefaults()

        // Update state directly with known defaults instead of reading
        // back from sysfs, because the walt governor may immediately
        // override scaling_min_freq with its own floor value.
        val effFreqs = KernelManagerUtils.getAvailableFrequencies(KernelManagerUtils.EFFICIENCY_CLUSTER) ?: emptyList()
        val perfFreqs = KernelManagerUtils.getAvailableFrequencies(KernelManagerUtils.PERFORMANCE_CLUSTER) ?: emptyList()
        val defaultMinFreq = KernelManagerUtils.DEFAULT_MIN_FREQ

        _state.update {
            it.copy(
                availableGovernors = KernelManagerUtils.getAvailableGovernors(),
                currentGovernor = KernelManagerUtils.DEFAULT_GOVERNOR,
                effAvailableFreqs = effFreqs,
                effMinFreq = defaultMinFreq,
                effMaxFreq = effFreqs.lastOrNull() ?: "0",
                perfAvailableFreqs = perfFreqs,
                perfMinFreq = defaultMinFreq,
                perfMaxFreq = perfFreqs.lastOrNull() ?: "0",
                applyOnBoot = keepApplyOnBoot
            )
        }
    }

    companion object {
        const val PREFS_NAME = "kernel_manager_settings"
        const val PREF_GOVERNOR = "governor"
        const val PREF_EFF_MIN = "efficiency_min_freq"
        const val PREF_EFF_MAX = "efficiency_max_freq"
        const val PREF_PERF_MIN = "performance_min_freq"
        const val PREF_PERF_MAX = "performance_max_freq"
        const val PREF_APPLY_ON_BOOT = "apply_on_boot"
    }
}
