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

import java.io.File

class GpuManagerUtils {

    companion object {
        private const val GPU_BASE_PATH = "/sys/class/kgsl/kgsl-3d0"
        private const val DEFAULT_GOVERNOR = "msm-adreno-tz"
        
        // GPU paths
        private const val GPU_MODEL = "$GPU_BASE_PATH/gpu_model"
        private const val GPU_AVAILABLE_FREQUENCIES = "$GPU_BASE_PATH/gpu_available_frequencies"
        private const val GPU_CURRENT_FREQ = "$GPU_BASE_PATH/gpuclk"
        private const val GPU_MIN_FREQ = "$GPU_BASE_PATH/devfreq/min_freq"
        private const val GPU_MAX_FREQ = "$GPU_BASE_PATH/devfreq/max_freq"
        private const val GPU_GOVERNOR = "$GPU_BASE_PATH/devfreq/governor"
        private const val GPU_AVAILABLE_GOVERNORS = "$GPU_BASE_PATH/devfreq/available_governors"
        private const val GPU_BUSY_PERCENTAGE = "$GPU_BASE_PATH/gpu_busy_percentage"
        private const val GPU_TEMPERATURE = "$GPU_BASE_PATH/temp"
        private const val GPU_THERMAL_PWRLEVEL = "$GPU_BASE_PATH/thermal_pwrlevel"
        private const val GPU_FORCE_CLK_ON = "$GPU_BASE_PATH/force_clk_on"
        private const val GPU_FORCE_BUS_ON = "$GPU_BASE_PATH/force_bus_on"
        private const val GPU_FORCE_RAIL_ON = "$GPU_BASE_PATH/force_rail_on"
        private const val GPU_FORCE_NO_NAP = "$GPU_BASE_PATH/force_no_nap"
        private const val GPU_BUS_SPLIT = "$GPU_BASE_PATH/bus_split"
    }

    fun getGpuModel(): String {
        return runCatching { readFile(GPU_MODEL).trim() }.getOrDefault("Unknown GPU")
    }

    fun getAvailableGovernors(): Array<String> {
        return runCatching {
            readFile(GPU_AVAILABLE_GOVERNORS).trim().split("\\s+".toRegex()).toTypedArray()
        }.getOrDefault(arrayOf("msm-adreno-tz", "performance", "powersave", "simple_ondemand"))
    }

    fun getAvailableFrequencies(): Array<String>? {
        return runCatching {
            readFile(GPU_AVAILABLE_FREQUENCIES).trim().split("\\s+".toRegex()).toTypedArray()
        }.getOrNull()
    }

    fun getCurrentGovernor(): String {
        return runCatching { readFile(GPU_GOVERNOR).trim() }.getOrDefault(DEFAULT_GOVERNOR)
    }

    fun getCurrentFrequency(): String {
        return runCatching { readFile(GPU_CURRENT_FREQ).trim() }.getOrDefault("0")
    }

    fun getCurrentMinFrequency(): String {
        return runCatching { readFile(GPU_MIN_FREQ).trim() }.getOrDefault("0")
    }

    fun getCurrentMaxFrequency(): String {
        return runCatching { readFile(GPU_MAX_FREQ).trim() }.getOrDefault("0")
    }

    fun getGpuBusyPercentage(): String {
        return runCatching { readFile(GPU_BUSY_PERCENTAGE).trim() }.getOrDefault("0")
    }

    fun getGpuTemperature(): String {
        return runCatching {
            val rawTemp = readFile(GPU_TEMPERATURE).trim()
            val tempMilliCelsius = rawTemp.toInt()
            val tempCelsius = tempMilliCelsius / 1000.0
            String.format("%.1f", tempCelsius)
        }.getOrDefault("0")
    }

    fun getThermalPowerLevel(): String {
        return runCatching { readFile(GPU_THERMAL_PWRLEVEL).trim() }.getOrDefault("0")
    }

    fun getForceClkOn(): Boolean {
        return runCatching { readFile(GPU_FORCE_CLK_ON).trim() == "1" }.getOrDefault(false)
    }

    fun getForceBusOn(): Boolean {
        return runCatching { readFile(GPU_FORCE_BUS_ON).trim() == "1" }.getOrDefault(false)
    }

    fun getForceRailOn(): Boolean {
        return runCatching { readFile(GPU_FORCE_RAIL_ON).trim() == "1" }.getOrDefault(false)
    }

    fun getForceNoNap(): Boolean {
        return runCatching { readFile(GPU_FORCE_NO_NAP).trim() == "1" }.getOrDefault(true)
    }

    fun getBusSplit(): Boolean {
        return runCatching { readFile(GPU_BUS_SPLIT).trim() == "1" }.getOrDefault(true)
    }

    fun setGovernor(governor: String) {
        runCatching { writeFile(GPU_GOVERNOR, governor) }
    }

    fun setFrequencyRange(minFreq: String, maxFreq: String) {
        runCatching {
            writeFile(GPU_MIN_FREQ, minFreq)
            writeFile(GPU_MAX_FREQ, maxFreq)
        }
    }

    fun setForceClkOn(enabled: Boolean) {
        runCatching { writeFile(GPU_FORCE_CLK_ON, if (enabled) "1" else "0") }
    }

    fun setForceBusOn(enabled: Boolean) {
        runCatching { writeFile(GPU_FORCE_BUS_ON, if (enabled) "1" else "0") }
    }

    fun setForceRailOn(enabled: Boolean) {
        runCatching { writeFile(GPU_FORCE_RAIL_ON, if (enabled) "1" else "0") }
    }

    fun setForceNoNap(enabled: Boolean) {
        runCatching { writeFile(GPU_FORCE_NO_NAP, if (enabled) "1" else "0") }
    }

    fun setBusSplit(enabled: Boolean) {
        runCatching { writeFile(GPU_BUS_SPLIT, if (enabled) "1" else "0") }
    }

    fun resetToDefaults() {
        setGovernor(DEFAULT_GOVERNOR)
        val frequencies = getAvailableFrequencies()
        if (!frequencies.isNullOrEmpty()) {
            setFrequencyRange(frequencies.first(), frequencies.last())
        }
        
        setForceClkOn(false)
        setForceBusOn(false)
        setForceRailOn(false)
        setForceNoNap(true)
        setBusSplit(true)
    }

    private fun readFile(path: String): String {
        return File(path).readText()
    }

    private fun writeFile(path: String, value: String) {
        File(path).writeText(value)
    }
}
