package com.example.cs501clockin.widget

import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.example.cs501clockin.ui.util.TagPalette

/**
 * Mirrors Home chip tint strength: translucent fill using the same hue as [TagPalette].
 */
internal object TagWidgetColors {
    private const val FILL_ALPHA = 0.40f

    fun chipBackgroundArgb(tag: String, customArgbByTag: Map<String, Int> = emptyMap()): Int {
        val base = TagPalette.colorFor(tag, customArgbByTag).toArgb()
        return ColorUtils.setAlphaComponent(base, (255 * FILL_ALPHA).toInt())
    }

    /** Near-black labels like Material onSurface on tinted chips (Home uses dark text on chips). */
    val LABEL_ARGB: Int = android.graphics.Color.argb(230, 28, 27, 31)
}
