/*
 * Copyright (C) 2025 TheMysticle
 *           (C) 2026 zylhdrXP
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.hypercharge

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.lineageos.settings.Constants
import org.lineageos.settings.utils.FileUtils

data class HyperChargeState(
    val isEnabled: Boolean = true,
    val currentLimit: String = Constants.CHARGE_LIMIT_120W,
    val fastChgMode: String = "0",
    val constantCurrent: String = "0"
)

class HyperChargeViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val _uiState = MutableStateFlow(HyperChargeState())
    val uiState: StateFlow<HyperChargeState> = _uiState.asStateFlow()

    init {
        loadSettings()
        startMonitoring()
    }

    private fun loadSettings() {
        val enabled = prefs.getBoolean(Constants.KEY_HYPERCHARGE_STATUS, true)
        val limit = prefs.getString(Constants.KEY_HYPERCHARGE_LIMIT, Constants.CHARGE_LIMIT_120W)
            ?: Constants.CHARGE_LIMIT_120W

        _uiState.update {
            it.copy(
                isEnabled = enabled,
                currentLimit = limit
            )
        }
        updateHardwareInfo()
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            while (true) {
                updateHardwareInfo()
                delay(3000)
            }
        }
    }

    private fun updateHardwareInfo() {
        val fastMode = FileUtils.readOneLine(Constants.NODE_FASTCHG_MODE) ?: "0"
        val currCurrent = FileUtils.readOneLine(Constants.NODE_CONSTANT_CHARGE_CURRENT) ?: "0"

        _uiState.update {
            it.copy(
                fastChgMode = fastMode,
                constantCurrent = currCurrent
            )
        }
    }

    fun setHyperChargeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_HYPERCHARGE_STATUS, enabled).apply()
        _uiState.update { it.copy(isEnabled = enabled) }
        syncService(enabled, _uiState.value.currentLimit)
    }

    fun setHyperChargeLimit(limit: String) {
        prefs.edit().putString(Constants.KEY_HYPERCHARGE_LIMIT, limit).apply()
        _uiState.update { it.copy(currentLimit = limit) }
        syncService(_uiState.value.isEnabled, limit)
    }

    private fun syncService(enabled: Boolean, limit: String) {
        val intent = Intent(context, HyperChargeService::class.java)
        if (enabled && Constants.CHARGE_LIMIT_120W == limit) {
            context.stopService(intent)
        } else {
            context.startService(intent)
        }
    }
}
