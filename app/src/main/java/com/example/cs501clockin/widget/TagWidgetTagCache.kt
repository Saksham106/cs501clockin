package com.example.cs501clockin.widget

/**
 * Holds the latest home-screen tag list for [TagWidgetViewsFactory].
 * RemoteViewsService runs in the same app process; this must be updated before each widget refresh.
 */
internal object TagWidgetTagCache {
    @Volatile
    var tags: List<String> = emptyList()

    @Volatile
    var tagColorArgbByTag: Map<String, Int> = emptyMap()
}
