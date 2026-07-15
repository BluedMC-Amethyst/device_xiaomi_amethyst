/*
 * Copyright (C) 2025 The LineageOS Project
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

package org.lineageos.settings.charge;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.preference.PreferenceManager;

import org.lineageos.settings.utils.FileUtils;

public class ChargeUtils {

    private static final String TAG = "ChargeUtils";
    public static final String BYPASS_CHARGE_NODE = "/sys/class/qcom-battery/charge_control_en";
    private static final String PREF_BYPASS_CHARGE = "bypass_charge";

    // Bypass modes
    public static final int BYPASS_DISABLED = 0;
    public static final int BYPASS_ENABLED = 1;

    private Context mContext;
    private SharedPreferences mSharedPrefs;
    private static ChargeUtils sInstance;

    public ChargeUtils(Context context) {
        // Use application context to avoid leaking Activity/Service contexts
        mContext = context.getApplicationContext();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public boolean isBypassChargeEnabled() {
        try {
            String value = FileUtils.readOneLine(BYPASS_CHARGE_NODE);
            return value != null && value.equals("1");
        } catch (Exception e) {
            Log.e(TAG, "Failed to read bypass charge status", e);
            return false;
        }
    }

    public void enableBypassCharge(boolean enable) {
        try {
            boolean ok = FileUtils.writeLine(BYPASS_CHARGE_NODE, enable ? "1" : "0");
            if (ok) {
                mSharedPrefs.edit().putBoolean(PREF_BYPASS_CHARGE, enable).apply();
            } else {
                Log.e(TAG, "Failed to write bypass charge status (writeLine returned false)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to write bypass charge status", e);
        }
    }
    
    public boolean isNodeAccessible(String node) {
        try {
            String value = FileUtils.readOneLine(node);
            return value != null;
        } catch (Exception e) {
            Log.e(TAG, "Node " + node + " not accessible", e);
            return false;
        }
    }
    
    public boolean isBypassChargeSupported() {
        return isNodeAccessible(BYPASS_CHARGE_NODE);
    }

    public static synchronized ChargeUtils getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ChargeUtils(context);
        }
        return sInstance;
    }
}
