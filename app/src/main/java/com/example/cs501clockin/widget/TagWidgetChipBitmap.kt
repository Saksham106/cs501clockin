package com.example.cs501clockin.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import android.util.LruCache

/**
 * Rounded-rect chip bitmaps for RemoteViews (cannot use [GradientDrawable] directly in RemoteViews).
 */
internal object TagWidgetChipBitmap {
    private const val CORNER_DP = 12f
    private const val BITMAP_W_DP = 240f
    private const val BITMAP_H_DP = 34f

    private val cache = LruCache<Int, Bitmap>(32)

    fun forColorArgb(context: android.content.Context, fillArgb: Int): Bitmap {
        cache.get(fillArgb)?.let { return it }
        val density = context.resources.displayMetrics.density
        val w = (BITMAP_W_DP * density).toInt().coerceAtLeast(48)
        val h = (BITMAP_H_DP * density).toInt().coerceAtLeast(32)
        val gd = GradientDrawable()
        gd.shape = GradientDrawable.RECTANGLE
        gd.cornerRadius = CORNER_DP * density
        gd.setColor(fillArgb)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        gd.setBounds(0, 0, w, h)
        gd.draw(canvas)
        cache.put(fillArgb, bmp)
        return bmp
    }
}
