package com.example.cs501clockin.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces

/**
 * Pastel swatches for custom tag creation. Includes every built-in [TagPalette] hue plus
 * additional soft colors so users have more than the default set while staying consistent.
 */
object PastelTagColors {
    val CHOICES_ARGB: List<Int> = listOf(
        0xFF2E8DFF.toInt(),
        0xFF62C7F8.toInt(),
        0xFF3ECF56.toInt(),
        0xFFFFA31A.toInt(),
        0xFFFF3E6C.toInt(),
        0xFFB05AE5.toInt(),
        0xFF5C61E6.toInt(),
        0xFF8F8F9B.toInt(),
        0xFFFFB5A7.toInt(),
        0xFF7FD9BE.toInt(),
        0xFFFFF2A8.toInt(),
        0xFFB8C4FF.toInt(),
        0xFFFFB8D9.toInt(),
        0xFFE8D4B8.toInt(),
        0xFF9FE8E8.toInt(),
        0xFFD4B8F0.toInt(),
        0xFFB8E8C8.toInt(),
        0xFFFFD4A8.toInt()
    )

    fun fallbackArgbForTagName(tag: String): Int {
        val idx = (tag.hashCode() and Int.MAX_VALUE) % CHOICES_ARGB.size
        return CHOICES_ARGB[idx]
    }
}

/**
 * Converts a packed Android ARGB [Int] to Compose [Color].
 * Do not use [Color] `ULong`/`Int` constructors here — they expect Compose's packed encoding, not ARGB.
 */
fun Int.argbToColor(): Color {
    val v = this.toLong() and 0xFFFFFFFFL
    val a = ((v shr 24) and 0xFF).toInt() / 255f
    val r = ((v shr 16) and 0xFF).toInt() / 255f
    val g = ((v shr 8) and 0xFF).toInt() / 255f
    val b = (v and 0xFF).toInt() / 255f
    return Color(r, g, b, a, ColorSpaces.Srgb)
}
