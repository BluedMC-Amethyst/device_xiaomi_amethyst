package org.lineageos.settings.refreshrate

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settingslib.applications.ApplicationsState
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import org.lineageos.settings.R

class RefreshSettingsFragment : SettingsBasePreferenceFragment(), ApplicationsState.Callbacks {
    private lateinit var state: ApplicationsState
    private lateinit var session: ApplicationsState.Session
    private lateinit var utils: RefreshUtils
    private val adapter = AppsAdapter()
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) = Unit
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); state = ApplicationsState.getInstance(requireActivity().application); session = state.newSession(this); session.onResume(); utils = RefreshUtils(requireContext())
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater.inflate(R.layout.refresh_layout, container, false)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { super.onViewCreated(view, savedInstanceState); view.findViewById<RecyclerView>(R.id.refresh_rv_view).apply { layoutManager = LinearLayoutManager(context); adapter = this@RefreshSettingsFragment.adapter } }
    override fun onResume() { super.onResume(); activity?.setTitle(R.string.refresh_title); rebuild() }
    override fun onDestroy() { session.onPause(); session.onDestroy(); super.onDestroy() }
    private fun rebuild() { session.rebuild(AppFilter(requireContext().packageManager), ApplicationsState.ALPHA_COMPARATOR) }
    override fun onRebuildComplete(entries: ArrayList<ApplicationsState.AppEntry>?) { adapter.entries = entries.orEmpty(); adapter.notifyDataSetChanged() }
    override fun onPackageListChanged() = rebuild(); override fun onLoadEntriesCompleted() = rebuild()
    override fun onAllSizesComputed() = Unit; override fun onLauncherInfoChanged() = Unit; override fun onPackageIconChanged() = Unit
    override fun onPackageSizeChanged(packageName: String) = Unit; override fun onRunningStateChanged(running: Boolean) = Unit
    private inner class Holder(view: View) : RecyclerView.ViewHolder(view) { val title: TextView = view.findViewById(R.id.app_name); val mode: Spinner = view.findViewById(R.id.app_mode); val icon: ImageView = view.findViewById(R.id.app_icon); val stateIcon: ImageView = view.findViewById(R.id.state) }
    private inner class AppsAdapter : RecyclerView.Adapter<Holder>() {
        var entries: List<ApplicationsState.AppEntry> = emptyList()
        override fun getItemCount() = entries.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(layoutInflater.inflate(R.layout.refresh_list_item, parent, false))
        override fun onBindViewHolder(holder: Holder, position: Int) {
            val entry = entries[position]; state.ensureIcon(entry); holder.title.text = entry.label; holder.icon.setImageDrawable(entry.icon)
            holder.mode.adapter = ArrayAdapter.createFromResource(requireContext(), R.array.refresh_modes, android.R.layout.simple_spinner_dropdown_item)
            val current = utils.getStateForPackage(entry.info.packageName); holder.mode.setSelection(current, false)
            holder.mode.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener { override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit; override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, selected: Int, id: Long) { if (selected != utils.getStateForPackage(entry.info.packageName)) utils.writePackage(entry.info.packageName, selected) } }
            holder.stateIcon.setImageResource(when (current) { 1,4 -> R.drawable.ic_refresh_60; 2,5 -> R.drawable.ic_refresh_90; 3,6 -> R.drawable.ic_refresh_120; else -> R.drawable.ic_refresh_default })
        }
    }
    private class AppFilter(pm: PackageManager) : ApplicationsState.AppFilter {
        private val packages = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0).map { it.activityInfo.packageName }.toSet()
        private val seen = mutableSetOf<String>()
        override fun init() { seen.clear() }
        override fun filterApp(entry: ApplicationsState.AppEntry) = entry.info.packageName in packages && seen.add(entry.info.packageName)
    }
}
