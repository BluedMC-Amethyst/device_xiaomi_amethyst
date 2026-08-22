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

import java.io.File

object KernelManagerUtils {
    const val EFFICIENCY_CLUSTER = 0
    const val PERFORMANCE_CLUSTER = 4
    const val PRIME_CLUSTER = 7
    
    private val POLICIES = intArrayOf(EFFICIENCY_CLUSTER, PERFORMANCE_CLUSTER, PRIME_CLUSTER)
    internal const val DEFAULT_GOVERNOR = "walt"
    internal const val DEFAULT_MIN_FREQ = "691200"
    
    private const val CPU_BASE_PATH = "/sys/devices/system/cpu/cpufreq/policy"
    private const val SCALING_GOVERNOR = "/scaling_governor"
    private const val SCALING_MIN_FREQ = "/scaling_min_freq"
    private const val SCALING_MAX_FREQ = "/scaling_max_freq"
    private const val SCALING_AVAILABLE_GOVERNORS = "/scaling_available_governors"
    private const val SCALING_AVAILABLE_FREQUENCIES = "/scaling_available_frequencies"

    fun getAvailableGovernors(): List<String> {
        return runCatching {
            readFile(CPU_BASE_PATH + EFFICIENCY_CLUSTER + SCALING_AVAILABLE_GOVERNORS)
                .trim().split("\\s+".toRegex())
        }.getOrDefault(listOf(DEFAULT_GOVERNOR, "schedutil", "performance", "powersave", "ondemand", "conservative"))
    }

    fun getAvailableFrequencies(cluster: Int): List<String>? {
        return runCatching {
            readFile(CPU_BASE_PATH + cluster + SCALING_AVAILABLE_FREQUENCIES)
                .trim().split("\\s+".toRegex())
        }.getOrNull()
    }

    fun getCurrentGovernor(cluster: Int): String {
        return runCatching {
            readFile(CPU_BASE_PATH + cluster + SCALING_GOVERNOR).trim()
        }.getOrDefault(DEFAULT_GOVERNOR)
    }

    fun getCurrentMinFrequency(cluster: Int): String {
        return runCatching {
            readFile(CPU_BASE_PATH + cluster + SCALING_MIN_FREQ).trim()
        }.getOrDefault(DEFAULT_MIN_FREQ)
    }

    fun getCurrentMaxFrequency(cluster: Int): String {
        return runCatching {
            readFile(CPU_BASE_PATH + cluster + SCALING_MAX_FREQ).trim()
        }.getOrDefault(getAvailableFrequencies(cluster)?.lastOrNull() ?: "0")
    }

    fun setGovernor(governor: String) {
        for (cluster in POLICIES) {
            runCatching { writeFile(CPU_BASE_PATH + cluster + SCALING_GOVERNOR, governor) }
        }
    }

    fun setFrequencyRange(cluster: Int, minFreq: String, maxFreq: String) {
        runCatching {
            // Write max before min to avoid kernel rejecting min > current_max
            writeFile(CPU_BASE_PATH + cluster + SCALING_MAX_FREQ, maxFreq)
            writeFile(CPU_BASE_PATH + cluster + SCALING_MIN_FREQ, minFreq)
        }
    }

    fun resetToDefaults() {
        setGovernor(DEFAULT_GOVERNOR)
        for (cluster in POLICIES) {
            val frequencies = getAvailableFrequencies(cluster) ?: continue
            setFrequencyRange(cluster, DEFAULT_MIN_FREQ, frequencies.last())
        }
    }

    private fun readFile(path: String): String {
        return File(path).readText()
    }

    private fun writeFile(path: String, value: String) {
        File(path).writeText(value)
    }
}
