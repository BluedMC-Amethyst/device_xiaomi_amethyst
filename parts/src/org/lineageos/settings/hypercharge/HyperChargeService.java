/*
 * Copyright (C) 2025 TheMysticle
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.hypercharge;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.preference.PreferenceManager;

import org.lineageos.settings.Constants;
import org.lineageos.settings.utils.FileUtils;


public class HyperChargeService extends Service {
    private static final String TAG = "HyperChargeService";
    
    private static final int POLLING_INTERVAL_MS = 5000;
    private static final int HANDSHAKE_DELAY_MS = 2000;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mMonitoringRunnable;
    private Runnable mPendingStartRunnable;
    private SharedPreferences mPrefs;

    private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                Log.i(TAG, "Charger connected — waiting " + HANDSHAKE_DELAY_MS + "ms for handshake.");
                
                stopMonitoring();
                mPendingStartRunnable = () -> {
                    startMonitoring();
                };
                mHandler.postDelayed(mPendingStartRunnable, HANDSHAKE_DELAY_MS);

            } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                Log.i(TAG, "Charger disconnected — stopping monitoring.");
                stopMonitoring();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(powerReceiver, filter);

        if (isDevicePluggedIn()) {
            Log.i(TAG, "Device plugged in at boot — waiting " + HANDSHAKE_DELAY_MS + "ms for handshake.");
            mPendingStartRunnable = this::startMonitoring;
            mHandler.postDelayed(mPendingStartRunnable, HANDSHAKE_DELAY_MS);
        }
    }

    private void startMonitoring() {
        if (mMonitoringRunnable != null) return;

        Log.d(TAG, "Monitoring loop started (interval=" + POLLING_INTERVAL_MS + "ms).");
        mMonitoringRunnable = new Runnable() {
            @Override
            public void run() {
                String fastChgMode = FileUtils.readOneLine(Constants.NODE_FASTCHG_MODE);

                if ("1".equals(fastChgMode)) {
                    String targetValue = getTargetLimit();
                    String currentValue = FileUtils.readOneLine(Constants.NODE_CONSTANT_CHARGE_CURRENT);

                    if (!targetValue.equals(currentValue)) {
                        Log.i(TAG, "Applying charge limit: " + currentValue + " -> " + targetValue);
                        FileUtils.writeLine(Constants.NODE_CONSTANT_CHARGE_CURRENT, targetValue);
                    }
                    mHandler.postDelayed(this, POLLING_INTERVAL_MS); // Continue loop
                } else {
                    // The charger may briefly leave fast-charge mode while renegotiating.
                    // Keep monitoring so the limit is reapplied when it returns.
                    mHandler.postDelayed(this, POLLING_INTERVAL_MS);
                }
            }
        };
        mHandler.post(mMonitoringRunnable);
    }

    private String getTargetLimit() {
        boolean masterEnabled = mPrefs.getBoolean(Constants.KEY_HYPERCHARGE_STATUS, false);
        if (!masterEnabled) {
            return Constants.CHARGE_LIMIT_33W;
        }
        return mPrefs.getString(Constants.KEY_HYPERCHARGE_LIMIT, Constants.CHARGE_LIMIT_120W);
    }

    private void stopMonitoring() {
        if (mPendingStartRunnable != null) {
            mHandler.removeCallbacks(mPendingStartRunnable);
            mPendingStartRunnable = null;
        }

        if (mMonitoringRunnable != null) {
            mHandler.removeCallbacks(mMonitoringRunnable);
            mMonitoringRunnable = null;
            Log.d(TAG, "Monitoring loop stopped.");
        }

        restoreDefaultChargeLimit();
    }

    private void restoreDefaultChargeLimit() {
        try {
            FileUtils.writeLine(Constants.NODE_CONSTANT_CHARGE_CURRENT, Constants.CHARGE_LIMIT_120W);
            Log.i(TAG, "Restored charge current limit to default (120W)");
        } catch (Exception e) {
            Log.e(TAG, "Failed to restore charge current limit", e);
        }
    }

    private boolean isDevicePluggedIn() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        if (batteryStatus == null) return false;

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return status == BatteryManager.BATTERY_PLUGGED_AC || 
               status == BatteryManager.BATTERY_PLUGGED_USB || 
               status == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed — Cleaning up.");
        unregisterReceiver(powerReceiver);
        stopMonitoring();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
