package org.lineageos.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import android.widget.ImageView
import android.widget.TextView
import android.view.View
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
    private var defaultValue: Int? = attrs?.getAttributeValue(ANDROID_NS, "defaultValue")?.toIntOrNull()?.coerceIn(min, max)
    private var units = ""
    private var showSign = false
    private var seekBar: SeekBar? = null
    private var valueView: TextView? = null
    private var reset: ImageView? = null
    private var minus: ImageView? = null
    private var plus: ImageView? = null
    init {
        context.obtainStyledAttributes(attrs, R.styleable.CustomSeekBarPreference).let { a ->
            showSign = a.getBoolean(R.styleable.CustomSeekBarPreference_showSign, false)
            units = a.getString(R.styleable.CustomSeekBarPreference_units)?.let { " $it" } ?: ""
            a.recycle()
        }
        value = defaultValue ?: min
        layoutResource = R.layout.preference_custom_seekbar
    }
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        seekBar = (holder.findViewById(R.id.seekbar) as? android.view.ViewGroup)?.let { container ->
            container.removeAllViews(); SeekBar(context).also { container.addView(it); it.max = (max - min) / interval; it.progress = (value - min) / interval; it.setOnSeekBarChangeListener(this) }
        }
        valueView = holder.findViewById(R.id.value) as? TextView
        reset = holder.findViewById(R.id.reset) as? ImageView
        minus = holder.findViewById(R.id.minus) as? ImageView
        plus = holder.findViewById(R.id.plus) as? ImageView
        reset?.setOnClickListener { defaultValue?.let(::setValue) }
        minus?.setOnClickListener { setValue(value - interval) }
        plus?.setOnClickListener { setValue(value + interval) }
        updateViews()
    }
    override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
        val next = (min + progress * interval).coerceIn(min, max)
        if (next != value && callChangeListener(next)) { value = next; persistInt(next); updateViews() }
    }
    override fun onStartTrackingTouch(bar: SeekBar) = Unit
    override fun onStopTrackingTouch(bar: SeekBar) = Unit
    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) { value = if (restoreValue) getPersistedInt(value) else (defaultValue as? Int ?: value) }
    override fun onDependencyChanged(dependency: Preference, disableDependent: Boolean) { super.onDependencyChanged(dependency, disableDependent); seekBar?.isEnabled = !disableDependent; listOf(reset, minus, plus).forEach { it?.isEnabled = !disableDependent } }
    private fun updateViews() {
        val text = "${if (showSign && value > 0) "+" else ""}$value$units"
        valueView?.text = context.getString(R.string.custom_seekbar_value, text)
        reset?.visibility = if (defaultValue != null && value != defaultValue) View.VISIBLE else View.INVISIBLE
        minus?.isEnabled = value > min; plus?.isEnabled = value < max
    }
    fun setValue(newValue: Int) { value = newValue.coerceIn(min, max); seekBar?.progress = (value - min) / interval; persistInt(value); updateViews() }
    fun refresh(newValue: Int) = setValue(newValue)
    fun getValue() = value
    companion object { private const val SETTINGS_NS = "http://schemas.android.com/apk/res/com.android.settings"; private const val ANDROID_NS = "http://schemas.android.com/apk/res/android" }
}
