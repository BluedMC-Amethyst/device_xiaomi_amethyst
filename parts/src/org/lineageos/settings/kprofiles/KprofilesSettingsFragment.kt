package org.lineageos.settings.kprofiles

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.UserHandle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import org.lineageos.settings.R
import org.lineageos.settings.utils.FileUtils

class KprofilesSettingsFragment : SettingsBasePreferenceFragment(), Preference.OnPreferenceChangeListener {
    private lateinit var auto: SwitchPreferenceCompat
    private lateinit var mode: ListPreference
    private lateinit var info: Preference
    private var selfChange = false
    private var registered = false
    private val receiver = object : BroadcastReceiver() { override fun onReceive(context: Context, intent: Intent) { if (intent.action == INTENT_ACTION && !selfChange) updateValues() else selfChange = false } }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.kprofiles_settings, rootKey)
        auto = findPreference(KPROFILES_AUTO_KEY)!!; mode = findPreference(KPROFILES_MODES_KEY)!!; info = findPreference(KPROFILES_MODES_INFO)!!
        auto.isEnabled = FileUtils.fileExists(KPROFILES_AUTO_NODE); mode.isEnabled = IS_SUPPORTED; info.isEnabled = IS_SUPPORTED
        auto.onPreferenceChangeListener = this; mode.onPreferenceChangeListener = this
        updateValues(); requireContext().registerReceiver(receiver, IntentFilter(INTENT_ACTION), Context.RECEIVER_NOT_EXPORTED); registered = true
    }
    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference.key == KPROFILES_AUTO_KEY) FileUtils.writeLine(KPROFILES_AUTO_NODE, if (newValue as Boolean) ON else OFF)
        else { val value = newValue as String; FileUtils.writeLine(KPROFILES_MODES_NODE, value); updateTitle(value); selfChange = true; requireContext().sendBroadcastAsUser(Intent(INTENT_ACTION).setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY), UserHandle.CURRENT) }
        return true
    }
    override fun onResume() { super.onResume(); if (::auto.isInitialized) updateValues() }
    override fun onDestroy() { if (registered) { requireContext().unregisterReceiver(receiver); registered = false }; super.onDestroy() }
    private fun updateValues() { auto.isChecked = FileUtils.readOneLine(KPROFILES_AUTO_NODE) == ON; mode.value = FileUtils.readOneLine(KPROFILES_MODES_NODE) ?: "0"; updateTitle(mode.value) }
    private fun updateTitle(value: String?) { Handler.getMain().post { info.title = getString(R.string.kprofiles_modes_description, description(value)) } }
    private fun description(value: String?) = getString(when (value) { "1" -> R.string.kprofiles_modes_battery_description; "2" -> R.string.kprofiles_modes_balanced_description; "3" -> R.string.kprofiles_modes_performance_description; else -> R.string.kprofiles_modes_none_description })
    companion object {
        const val INTENT_ACTION = "org.lineageos.settings.kprofiles.KPROFILE_CHANGED"; const val KPROFILES_MODES_NODE = "/sys/kernel/kprofiles/kp_mode"
        const val KPROFILES_AUTO_KEY = "kprofiles_auto"; const val KPROFILES_AUTO_NODE = "/sys/module/kprofiles/parameters/auto_kp"
        const val KPROFILES_MODES_KEY = "kprofiles_modes"; const val KPROFILES_MODES_INFO = "pref_kprofiles_modes_info"; const val ON = "Y"; const val OFF = "N"
        @JvmField val IS_SUPPORTED = FileUtils.fileExists(KPROFILES_MODES_NODE)
    }
}
