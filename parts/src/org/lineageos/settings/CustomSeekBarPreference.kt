package org.lineageos.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder

class CustomSeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = TypedArrayUtils.getAttr(context, androidx.preference.R.attr.preferenceStyle, android.R.attr.preferenceStyle),
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes), SeekBar.OnSeekBarChangeListener {
    private var min = attrs?.getAttributeIntValue(SETTINGS_NS, "min", 0) ?: 0
    private var max = attrs?.getAttributeIntValue(ANDROID_NS, "max", 100) ?: 100
    private var interval = attrs?.getAttributeIntValue(SETTINGS_NS, "interval", 1) ?: 1
    private var value = min
    private var seekBar: SeekBar? = null
    init { layoutResource = R.layout.preference_custom_seekbar }
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        seekBar = (holder.findViewById(R.id.seekbar) as? android.view.ViewGroup)?.let { container ->
            container.removeAllViews(); SeekBar(context).also { container.addView(it); it.max = (max - min) / interval; it.progress = (value - min) / interval; it.setOnSeekBarChangeListener(this) }
        }
    }
    override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
        val next = (min + progress * interval).coerceIn(min, max)
        if (next != value && callChangeListener(next)) { value = next; persistInt(next) }
    }
    override fun onStartTrackingTouch(bar: SeekBar) = Unit
    override fun onStopTrackingTouch(bar: SeekBar) = Unit
    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) { value = if (restoreValue) getPersistedInt(value) else (defaultValue as? Int ?: value) }
    fun setValue(newValue: Int) { value = newValue.coerceIn(min, max); seekBar?.progress = (value - min) / interval }
    fun refresh(newValue: Int) = setValue(newValue)
    fun getValue() = value
    companion object { private const val SETTINGS_NS = "http://schemas.android.com/apk/res/com.android.settings"; private const val ANDROID_NS = "http://schemas.android.com/apk/res/android" }
}
