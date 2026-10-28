package org.lineageos.settings.refreshrate

import android.os.Bundle
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity

class RefreshSettingsActivity : CollapsingToolbarBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(
            com.android.settingslib.collapsingtoolbar.R.id.content_frame,
            RefreshSettingsFragment(), "refresh"
        ).commit()
    }
}
