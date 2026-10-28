package org.lineageos.settings.corecontrol

import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import org.lineageos.settings.R
import org.lineageos.settings.utils.FileUtils

class CoreControlFragment : SettingsBasePreferenceFragment(), Preference.OnPreferenceChangeListener {
    private val preferences = arrayOfNulls<TwoStatePreference>(8)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.core_control_settings)
        preferences.indices.forEach { core ->
            preferences[core] = findPreference("core_$core")
            preferences[core]?.let { preference ->
                if (core == 0) preference.apply { isChecked = true; isEnabled = false; summary = "Primary boot core (always online)" }
                else preference.apply { onPreferenceChangeListener = this@CoreControlFragment; isChecked = isCoreOnline(core) }
            }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val core = preferences.indexOfFirst { it == preference }
        if (core < 1) return false
        val online = newValue as Boolean
        if (!online && !canOffline(core)) {
            Toast.makeText(context, "At least 2 little cores must remain online", Toast.LENGTH_SHORT).show()
            return false
        }
        FileUtils.writeLine("/sys/devices/system/cpu/cpu$core/online", if (online) "1" else "0")
        return true
    }

    private fun isCoreOnline(core: Int) = core == 0 || FileUtils.readOneLine("/sys/devices/system/cpu/cpu$core/online") == "1"
    private fun canOffline(core: Int) = core !in 1..3 || (0..3).count { it != core && isCoreOnline(it) } >= 2
}
