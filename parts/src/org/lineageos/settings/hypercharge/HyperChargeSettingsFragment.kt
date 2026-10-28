package org.lineageos.settings.hypercharge

import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.PreferenceManager
import com.android.settingslib.widget.MainSwitchPreference
import org.lineageos.settings.Constants
import org.lineageos.settings.R

class HyperChargeSettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {
    private lateinit var mainSwitch: MainSwitchPreference
    private lateinit var limitPreference: ListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val fragmentContext = context ?: return
        val prefs = PreferenceManager.getDefaultSharedPreferences(fragmentContext)
        runCatching { prefs.getBoolean(Constants.KEY_HYPERCHARGE_STATUS, false) }.onFailure { prefs.edit().remove(Constants.KEY_HYPERCHARGE_STATUS).commit() }
        runCatching { prefs.getString(Constants.KEY_HYPERCHARGE_LIMIT, Constants.CHARGE_LIMIT_120W) }.onFailure { prefs.edit().remove(Constants.KEY_HYPERCHARGE_LIMIT).commit() }
        addPreferencesFromResource(R.xml.hypercharge_settings)
        mainSwitch = findPreference(Constants.KEY_HYPERCHARGE_STATUS)!!
        limitPreference = findPreference(Constants.KEY_HYPERCHARGE_LIMIT)!!
        mainSwitch.onPreferenceChangeListener = this
        limitPreference.onPreferenceChangeListener = this
        limitPreference.isEnabled = mainSwitch.isChecked
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val enabled = if (preference == mainSwitch) newValue as Boolean else mainSwitch.isChecked
        val limit = if (preference == limitPreference) newValue as String else limitPreference.value
        if (preference == mainSwitch) limitPreference.isEnabled = enabled
        val fragmentContext = context ?: return false
        val intent = Intent(fragmentContext, HyperChargeService::class.java)
        if (!enabled || limit == Constants.CHARGE_LIMIT_120W) fragmentContext.stopService(intent) else fragmentContext.startService(intent)
        return true
    }
}
