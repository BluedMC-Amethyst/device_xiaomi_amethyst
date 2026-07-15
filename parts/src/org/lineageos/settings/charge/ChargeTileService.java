package org.lineageos.settings.charge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import androidx.preference.PreferenceManager;

import org.lineageos.settings.Constants;
import org.lineageos.settings.R;

public class ChargeTileService extends TileService {
    private static final String TAG = "ChargeTileService";
    private static final String KEY_BYPASS_CHARGE = "bypass_charge";
    private SharedPreferences mSharedPrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Listen for changes to update the tile immediately.
        mPrefListener = (sharedPreferences, key) -> {
            if (KEY_BYPASS_CHARGE.equals(key)) {
                updateTile();
            }
        };
        mSharedPrefs.registerOnSharedPreferenceChangeListener(mPrefListener);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onDestroy() {
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(mPrefListener);
        super.onDestroy();
    }

    @Override
    public void onClick() {
        // Toggle the master switch via ChargeUtils.
        boolean enabled = mSharedPrefs.getBoolean(KEY_BYPASS_CHARGE, false);
        boolean newState = !enabled;
        ChargeUtils.getInstance(this).enableBypassCharge(newState);
        Log.d(TAG, "Bypass charge switch toggled to: " + newState);
        updateTile();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile != null) {
            boolean enabled = mSharedPrefs.getBoolean(KEY_BYPASS_CHARGE, false);
            if (enabled) {
                tile.setState(Tile.STATE_ACTIVE);
                tile.setIcon(Icon.createWithResource(this, R.drawable.ic_bypass_charge_enabled));
                tile.setLabel(getString(R.string.bypass_charge_tile_label));
                tile.setSubtitle(getString(R.string.bypass_charge_tile_enabled_subtitle));
            } else {
                tile.setState(Tile.STATE_INACTIVE);
                tile.setIcon(Icon.createWithResource(this, R.drawable.ic_bypass_charge_disabled));
                tile.setLabel(getString(R.string.bypass_charge_tile_label));
                tile.setSubtitle(getString(R.string.bypass_charge_tile_disabled_subtitle));
            }
            tile.updateTile();
        }
    }
}
