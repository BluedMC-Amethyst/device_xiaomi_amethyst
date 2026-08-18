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

package org.lineageos.settings.gpumanager

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GpuManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val gpuUtils = GpuManagerUtils()
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class GpuState(
        val gpuModel: String = "",
        val currentGovernor: String = "",
        val availableGovernors: List<String> = emptyList(),
        val currentMinFreq: String = "",
        val currentMaxFreq: String = "",
        val availableFrequencies: List<String> = emptyList(),
        val currentFreq: String = "",
        val busyPercentage: String = "",
        val temperature: String = "",
        val thermalPowerLevel: String = "",
        val forceClkOn: Boolean = false,
        val forceBusOn: Boolean = false,
        val forceRailOn: Boolean = false,
        val forceNoNap: Boolean = false,
        val busSplit: Boolean = false,
        val applyOnBoot: Boolean = false
    )

    private val _uiState = MutableStateFlow(GpuState())
    val uiState: StateFlow<GpuState> = _uiState.asStateFlow()

    init {
        loadInitialState()
        startPeriodicUpdates()
    }

    private fun loadInitialState() {
        val governor = prefs.getString(PREF_GOVERNOR, null)
            ?: gpuUtils.getCurrentGovernor()
        val minFreq = prefs.getString(PREF_MIN_FREQ, null)
            ?: gpuUtils.getCurrentMinFrequency()
        val maxFreq = prefs.getString(PREF_MAX_FREQ, null)
            ?: gpuUtils.getCurrentMaxFrequency()
        _uiState.update {
            it.copy(
                gpuModel = gpuUtils.getGpuModel(),
                currentGovernor = governor,
                availableGovernors = gpuUtils.getAvailableGovernors().toList(),
                currentMinFreq = minFreq,
                currentMaxFreq = maxFreq,
                availableFrequencies = gpuUtils.getAvailableFrequencies()?.toList() ?: emptyList(),
                forceClkOn = prefs.getBoolean(PREF_FORCE_CLK_ON, gpuUtils.getForceClkOn()),
                forceBusOn = prefs.getBoolean(PREF_FORCE_BUS_ON, gpuUtils.getForceBusOn()),
                forceRailOn = prefs.getBoolean(PREF_FORCE_RAIL_ON, gpuUtils.getForceRailOn()),
                forceNoNap = prefs.getBoolean(PREF_FORCE_NO_NAP, gpuUtils.getForceNoNap()),
                busSplit = prefs.getBoolean(PREF_BUS_SPLIT, gpuUtils.getBusSplit()),
                applyOnBoot = prefs.getBoolean(PREF_APPLY_ON_BOOT, false)
            )
        }
        updateDynamicInfo()
    }

    private fun startPeriodicUpdates() {
        viewModelScope.launch {
            while (true) {
                updateDynamicInfo()
                delay(2000)
            }
        }
    }

    private fun updateDynamicInfo() {
        _uiState.update {
            it.copy(
                currentFreq = gpuUtils.getCurrentFrequency(),
                busyPercentage = gpuUtils.getGpuBusyPercentage(),
                temperature = gpuUtils.getGpuTemperature(),
                thermalPowerLevel = gpuUtils.getThermalPowerLevel()
            )
        }
    }

    fun setGovernor(governor: String) {
        _uiState.update { it.copy(currentGovernor = governor) }
    }

    fun setMinFrequency(freq: String) {
        _uiState.update { it.copy(currentMinFreq = freq) }
    }

    fun setMaxFrequency(freq: String) {
        _uiState.update { it.copy(currentMaxFreq = freq) }
    }

    fun setForceClkOn(enabled: Boolean) {
        _uiState.update { it.copy(forceClkOn = enabled) }
    }

    fun setForceBusOn(enabled: Boolean) {
        _uiState.update { it.copy(forceBusOn = enabled) }
    }

    fun setForceRailOn(enabled: Boolean) {
        _uiState.update { it.copy(forceRailOn = enabled) }
    }

    fun setForceNoNap(enabled: Boolean) {
        _uiState.update { it.copy(forceNoNap = enabled) }
    }

    fun setBusSplit(enabled: Boolean) {
        _uiState.update { it.copy(busSplit = enabled) }
    }

    fun setApplyOnBoot(enabled: Boolean) {
        _uiState.update { it.copy(applyOnBoot = enabled) }
        prefs.edit().putBoolean(PREF_APPLY_ON_BOOT, enabled).apply()
    }

    fun applySettings() {
        val state = _uiState.value
        gpuUtils.setGovernor(state.currentGovernor)
        gpuUtils.setFrequencyRange(state.currentMinFreq, state.currentMaxFreq)
        gpuUtils.setForceClkOn(state.forceClkOn)
        gpuUtils.setForceBusOn(state.forceBusOn)
        gpuUtils.setForceRailOn(state.forceRailOn)
        gpuUtils.setForceNoNap(state.forceNoNap)
        gpuUtils.setBusSplit(state.busSplit)
        prefs.edit()
            .putString(PREF_GOVERNOR, state.currentGovernor)
            .putString(PREF_MIN_FREQ, state.currentMinFreq)
            .putString(PREF_MAX_FREQ, state.currentMaxFreq)
            .putBoolean(PREF_FORCE_CLK_ON, state.forceClkOn)
            .putBoolean(PREF_FORCE_BUS_ON, state.forceBusOn)
            .putBoolean(PREF_FORCE_RAIL_ON, state.forceRailOn)
            .putBoolean(PREF_FORCE_NO_NAP, state.forceNoNap)
            .putBoolean(PREF_BUS_SPLIT, state.busSplit)
            .apply()
    }

    fun resetSettings() {
        prefs.edit().clear().apply()
        gpuUtils.resetToDefaults()
        loadInitialState()
    }

    companion object {
        const val PREFS_NAME = "gpu_manager_settings"
        const val PREF_GOVERNOR = "governor"
        const val PREF_MIN_FREQ = "min_freq"
        const val PREF_MAX_FREQ = "max_freq"
        const val PREF_FORCE_CLK_ON = "force_clk_on"
        const val PREF_FORCE_BUS_ON = "force_bus_on"
        const val PREF_FORCE_RAIL_ON = "force_rail_on"
        const val PREF_FORCE_NO_NAP = "force_no_nap"
        const val PREF_BUS_SPLIT = "bus_split"
        const val PREF_APPLY_ON_BOOT = "apply_on_boot"
    }
}
