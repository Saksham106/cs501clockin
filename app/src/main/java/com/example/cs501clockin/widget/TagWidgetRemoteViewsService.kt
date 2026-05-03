package com.example.cs501clockin.widget

import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.view.View
import com.example.cs501clockin.R

/**
 * Supplies scrollable rows (two tags per row) for the app widget ListView.
 */
class TagWidgetRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        TagWidgetViewsFactory(applicationContext)
}

private class TagWidgetViewsFactory(
    private val context: android.content.Context
) : RemoteViewsService.RemoteViewsFactory {

    private var tags: List<String> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        tags = TagWidgetTagCache.tags
    }

    override fun onDestroy() {}

    override fun getCount(): Int {
        if (tags.isEmpty()) return 0
        return (tags.size + 1) / 2
    }

    override fun getViewAt(position: Int): RemoteViews {
        val row = RemoteViews(context.packageName, R.layout.widget_tag_row)
        val leftIdx = position * 2
        val rightIdx = leftIdx + 1
        bindSlot(
            row,
            R.id.tag_slot_left_container,
            R.id.tag_row_left_bg,
            R.id.tag_row_left,
            tags.getOrNull(leftIdx)
        )
        bindSlot(
            row,
            R.id.tag_slot_right_container,
            R.id.tag_row_right_bg,
            R.id.tag_row_right,
            tags.getOrNull(rightIdx)
        )
        return row
    }

    private fun bindSlot(
        rv: RemoteViews,
        containerId: Int,
        bgId: Int,
        textId: Int,
        tag: String?
    ) {
        if (tag.isNullOrBlank()) {
            rv.setViewVisibility(containerId, View.GONE)
            return
        }
        rv.setViewVisibility(containerId, View.VISIBLE)
        val fillArgb = TagWidgetColors.chipBackgroundArgb(tag, TagWidgetTagCache.tagColorArgbByTag)
        rv.setImageViewBitmap(bgId, TagWidgetChipBitmap.forColorArgb(context, fillArgb))
        rv.setTextViewText(textId, tag)
        rv.setTextColor(textId, TagWidgetColors.LABEL_ARGB)
        val fillIn = Intent().apply {
            putExtra(WidgetTagClickReceiver.EXTRA_TAG, tag)
        }
        rv.setOnClickFillInIntent(textId, fillIn)
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = false
}
