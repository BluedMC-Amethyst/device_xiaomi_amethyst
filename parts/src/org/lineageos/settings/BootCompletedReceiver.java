/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2019 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Display.HdrCapabilities;

import org.lineageos.settings.Constants;
import org.lineageos.settings.thermal.ThermalUtils;
import org.lineageos.settings.refreshrate.RefreshUtils;
import org.lineageos.settings.hypercharge.HyperChargeService;
import org.lineageos.settings.chargelimit.ChargeLimitService;
import org.lineageos.settings.chargelimit.ChargeLimitUtils;
import static org.lineageos.settings.kprofiles.KprofilesSettingsFragment.IS_SUPPORTED;
import static org.lineageos.settings.kprofiles.KprofilesSettingsFragment.KPROFILES_AUTO_KEY;
import static org.lineageos.settings.kprofiles.KprofilesSettingsFragment.KPROFILES_AUTO_NODE;
import static org.lineageos.settings.kprofiles.KprofilesSettingsFragment.KPROFILES_MODES_KEY;
import static org.lineageos.settings.kprofiles.KprofilesSettingsFragment.KPROFILES_MODES_NODE;
import static org.lineageos.settings.kprofiles.KprofilesSettingsFragment.ON;
import static org.lineageos.settings.kprofiles.KprofilesSettingsFragment.OFF;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.utils.FileUtils;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final boolean DEBUG = false;
    private static final String TAG = "XiaomiParts";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (DEBUG) Log.i(TAG, "Received intent: " + intent.getAction());
        
        PreferenceManager.setDefaultValues(context, R.xml.hypercharge_settings, false);

        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean(ChargeLimitUtils.PREF_ENABLED, false)) {
                context.startService(new Intent(context, ChargeLimitService.class));
            }
            boolean isHyperChargeEnabled = prefs.getBoolean(Constants.KEY_HYPERCHARGE_STATUS, true);

            // Note: We use a try-catch here as well just in case boot happens before UI sanitization
            String currentLimit;
            try {
                currentLimit = prefs.getString(Constants.KEY_HYPERCHARGE_LIMIT, Constants.CHARGE_LIMIT_120W);
            } catch (ClassCastException e) {
                currentLimit = Constants.CHARGE_LIMIT_120W;
            }

            if (!isHyperChargeEnabled || !Constants.CHARGE_LIMIT_120W.equals(currentLimit)) {
                context.startService(new Intent(context, HyperChargeService.class));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start HyperChargeService on boot", e);
        }

        switch (intent.getAction()) {
            case Intent.ACTION_LOCKED_BOOT_COMPLETED:
                handleLockedBootCompleted(context);
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                handleBootCompleted(context);
                break;
        }
        
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        
        if (FileUtils.fileExists(KPROFILES_AUTO_NODE)) {
            boolean kProfilesAutoEnabled = sharedPrefs.getBoolean(KPROFILES_AUTO_KEY, false);
            FileUtils.writeLine(KPROFILES_AUTO_NODE, kProfilesAutoEnabled ? ON : OFF);
        }
        if (IS_SUPPORTED) {
            String kProfileMode = sharedPrefs.getString(KPROFILES_MODES_KEY, FileUtils.readOneLine(KPROFILES_MODES_NODE));
            FileUtils.writeLine(KPROFILES_MODES_NODE, kProfileMode);
        }
    }

    private void handleLockedBootCompleted(Context context) {
        if (DEBUG) Log.i(TAG, "Handling locked boot completed.");
        try {
            // Start necessary services
            startServices(context);

            // Override HDR types
            overrideHdrTypes(context);

        } catch (Exception e) {
            Log.e(TAG, "Error during locked boot completed processing", e);
        }
    }

    private void handleBootCompleted(Context context) {
        if (DEBUG) Log.i(TAG, "Handling boot completed.");
        // Add additional boot-completed actions if needed
    }

    private void startServices(Context context) {
        if (DEBUG) Log.i(TAG, "Starting services...");

        // Start Thermal Management Services
        ThermalUtils.getInstance(context).startService();

        // Start Refresh Rate Services
        RefreshUtils.startService(context);
    }

    private void overrideHdrTypes(Context context) {
        try {
            final DisplayManager dm = context.getSystemService(DisplayManager.class);
            if (dm != null) {
                dm.overrideHdrTypes(Display.DEFAULT_DISPLAY, new int[]{
                        HdrCapabilities.HDR_TYPE_DOLBY_VISION,
                        HdrCapabilities.HDR_TYPE_HDR10,
                        HdrCapabilities.HDR_TYPE_HLG,
                        HdrCapabilities.HDR_TYPE_HDR10_PLUS
                });
                if (DEBUG) Log.i(TAG, "HDR types overridden successfully.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error overriding HDR types", e);
        }
    }
}
