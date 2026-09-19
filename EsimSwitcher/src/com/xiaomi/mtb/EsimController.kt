/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.mtb

import android.content.Context
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.util.Log
import com.qti.extphone.Client
import com.qti.extphone.ExtPhoneCallbackListener
import com.qti.extphone.ExtTelephonyManager
import com.qti.extphone.QtiSimType
import com.qti.extphone.ServiceCallback
import com.qti.extphone.Status
import com.qti.extphone.Token

class EsimController private constructor(private val context: Context) {

    companion object {
        private const val TAG = "EsimController"
        private const val EVENT_SET_SIM_TYPE_RESPONSE = 34
        private const val SET_SIM_TYPE_TIMEOUT_MS = 4_000L
        private val DEBUG = Log.isLoggable(TAG, Log.DEBUG)

        @Volatile private var instance: EsimController? = null

        fun getInstance(context: Context): EsimController {
            return instance
                ?: synchronized(this) {
                    instance ?: EsimController(context.applicationContext).also { instance = it }
                }
        }
    }

    private val responseLock = Object()
    private val extTelephonyManager = ExtTelephonyManager.getInstance(context)

    @Volatile private var client: Client? = null
    private var pendingToken: Token? = null
    private var responseReceived = false
    private var setSimTypeSucceeded = false

    private val extPhoneCallback =
        object : ExtPhoneCallbackListener() {
            override fun setSimTypeResponse(token: Token?, status: Status?) {
                if (DEBUG) Log.d(TAG, "setSimTypeResponse: token=$token status=$status")
                synchronized(responseLock) {
                    val expectedToken = pendingToken
                    if (token == null || expectedToken == null || token.get() != expectedToken.get()) {
                        Log.w(TAG, "Ignoring response for an unexpected token: $token")
                        return
                    }
                    setSimTypeSucceeded = status?.get() == Status.SUCCESS
                    responseReceived = true
                    responseLock.notifyAll()
                }
            }
        }

    private val serviceCallback =
        object : ServiceCallback {
            override fun onConnected() {
                if (DEBUG) Log.d(TAG, "ExtTelephonyService connected")
                synchronized(responseLock) {
                    client =
                        extTelephonyManager.registerCallbackWithEvents(
                            context.packageName,
                            extPhoneCallback,
                            intArrayOf(EVENT_SET_SIM_TYPE_RESPONSE),
                        )
                    if (client == null) Log.e(TAG, "Failed to register ExtTelephony callback")
                    responseLock.notifyAll()
                }
            }

            override fun onDisconnected() {
                if (DEBUG) Log.d(TAG, "ExtTelephonyService disconnected")
                synchronized(responseLock) {
                    client = null
                    pendingToken = null
                    setSimTypeSucceeded = false
                    responseReceived = true
                    responseLock.notifyAll()
                }
            }
        }

    init {
        connectService()
    }

    fun onBootCompleted() {
        if (DEBUG) Log.d(TAG, "onBootCompleted")
        connectService()
    }

    fun getEsimActive(): Boolean {
        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
        return subscriptionManager?.activeSubscriptionInfoList?.any { it.isEmbedded } == true
    }

    fun getEsimEnabled(): Boolean {
        val simTypes = runCatching { extTelephonyManager.currentSimType }.getOrNull()
        if (simTypes != null && simTypes.size > 1) {
            return simTypes[1].get() == QtiSimType.SIM_TYPE_ESIM
        }

        val storedState = Settings.Secure.getString(context.contentResolver, "esim_enabled")
        return storedState?.toIntOrNull()?.let { it == 1 } ?: getEsimActive()
    }

    fun setEsimEnabled(isEnabled: Boolean): Boolean {
        if (DEBUG) Log.d(TAG, "setEsimEnabled: $isEnabled")
        if (client == null) {
            connectService()
            synchronized(responseLock) {
                val deadline = android.os.SystemClock.uptimeMillis() + SET_SIM_TYPE_TIMEOUT_MS
                while (client == null) {
                    val remaining = deadline - android.os.SystemClock.uptimeMillis()
                    if (remaining <= 0) break
                    try {
                        responseLock.wait(remaining)
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                        return false
                    }
                }
            }
        }
        val registeredClient = client ?: return false

        val simTypes =
            arrayOf(
                QtiSimType(QtiSimType.SIM_TYPE_PHYSICAL),
                QtiSimType(
                    if (isEnabled) {
                        QtiSimType.SIM_TYPE_ESIM
                    } else {
                        QtiSimType.SIM_TYPE_PHYSICAL
                    },
                ),
            )

        synchronized(responseLock) {
            pendingToken = null
            responseReceived = false
            setSimTypeSucceeded = false
            val token =
                runCatching { extTelephonyManager.setSimType(registeredClient, simTypes) }
                    .onFailure { Log.e(TAG, "setSimType failed", it) }
                    .getOrNull()
            if (token == null) return false
            pendingToken = token

            val deadline = android.os.SystemClock.uptimeMillis() + SET_SIM_TYPE_TIMEOUT_MS
            while (!responseReceived) {
                val remaining = deadline - android.os.SystemClock.uptimeMillis()
                if (remaining <= 0) break
                try {
                    responseLock.wait(remaining)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    pendingToken = null
                    return false
                }
            }
            pendingToken = null
            if (!setSimTypeSucceeded) {
                Log.e(TAG, "setSimType was rejected or timed out")
                return false
            }
        }

        Settings.Secure.putInt(
            context.contentResolver,
            "esim_enabled",
            if (isEnabled) 1 else 0,
        )
        return true
    }

    private fun connectService() {
        if (client != null) return
        if (!extTelephonyManager.connectService(serviceCallback)) {
            Log.e(TAG, "Failed to bind ExtTelephonyService")
        }
    }

    fun dispose() {
        runCatching { extTelephonyManager.unregisterCallback(extPhoneCallback) }
        client = null
        extTelephonyManager.disconnectService(serviceCallback)
    }
}
