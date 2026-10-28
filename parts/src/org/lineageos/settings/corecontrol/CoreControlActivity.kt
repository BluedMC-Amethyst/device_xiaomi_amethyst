package org.lineageos.settings.corecontrol

import android.os.Bundle
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity

class CoreControlActivity : CollapsingToolbarBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(
            com.android.settingslib.collapsingtoolbar.R.id.content_frame,
            CoreControlFragment(), "corecontrol"
        ).commit()
    }
}
