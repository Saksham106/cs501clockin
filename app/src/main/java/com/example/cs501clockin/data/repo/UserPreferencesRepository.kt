package com.example.cs501clockin.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cs501clockin.model.SessionTags
import org.json.JSONObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val MAX_NOTIFICATION_QUICK_TAGS = 3

data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val locationSuggestionsEnabled: Boolean = true,
    /** User-created tags (in addition to [SessionTags.defaults]). */
    val customTags: Set<String> = emptySet(),
    /** Tags shown on Home. */
    val homeVisibleTags: Set<String> = SessionTags.defaults.toSet(),
    /** Up to [MAX_NOTIFICATION_QUICK_TAGS] entries shown as notification actions. */
    val notificationQuickTags: Set<String> = SessionTags.defaults.take(MAX_NOTIFICATION_QUICK_TAGS).toSet(),
    /** User dismissed the welcome onboarding dialog. */
    val onboardingWelcomeCompleted: Boolean = false,
    /** First-visit tips for bottom-nav destinations (shown once each). */
    val onboardingTipHomeSeen: Boolean = false,
    val onboardingTipHistorySeen: Boolean = false,
    val onboardingTipDashboardSeen: Boolean = false,
    val onboardingTipSettingsSeen: Boolean = false,
    /** ARGB for user-created tags only (built-in tags use [com.example.cs501clockin.ui.util.TagPalette]). */
    val customTagColors: Map<String, Int> = emptyMap()
) {
    val allTags: List<String>
        get() = (SessionTags.defaults + customTags.sorted()).distinct()
}

/**
 * Same ordered tag list as [com.example.cs501clockin.viewmodel.HomeViewModel] chips (home visible tags).
 * Widgets and notifications should use this so UI stays consistent with Home.
 */
fun UserPreferences.homeScreenTagChips(): List<String> {
    val orderedVisible = allTags.filter { it in homeVisibleTags }
    return if (orderedVisible.isEmpty()) SessionTags.defaults else orderedVisible
}

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_prefs"
)

private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
private val KEY_LOCATION_SUGGESTIONS_ENABLED = booleanPreferencesKey("location_suggestions_enabled")
private val KEY_NOTIFICATION_QUICK_TAGS = stringSetPreferencesKey("notification_quick_tags")
private val KEY_CUSTOM_TAGS = stringSetPreferencesKey("custom_tags")
private val KEY_HOME_VISIBLE_TAGS = stringSetPreferencesKey("home_visible_tags")

private val KEY_ONBOARDING_WELCOME_COMPLETED = booleanPreferencesKey("onboarding_welcome_completed")
private val KEY_ONBOARDING_TIP_HOME_SEEN = booleanPreferencesKey("onboarding_tip_home_seen")
private val KEY_ONBOARDING_TIP_HISTORY_SEEN = booleanPreferencesKey("onboarding_tip_history_seen")
private val KEY_ONBOARDING_TIP_DASHBOARD_SEEN = booleanPreferencesKey("onboarding_tip_dashboard_seen")
private val KEY_ONBOARDING_TIP_SETTINGS_SEEN = booleanPreferencesKey("onboarding_tip_settings_seen")
private val KEY_CUSTOM_TAG_COLORS = stringPreferencesKey("custom_tag_colors")

private fun defaultNotificationQuickTags(): Set<String> =
    SessionTags.defaults.take(MAX_NOTIFICATION_QUICK_TAGS).toSet()

private fun defaultHomeVisibleTags(): Set<String> = SessionTags.defaults.toSet()

private fun normalizeCustomTags(raw: Set<String>?): Set<String> {
    val defaults = SessionTags.defaults.toSet()
    if (raw.isNullOrEmpty()) return emptySet()
    return raw
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .filter { it !in defaults }
        .toSet()
}

private fun normalizeHomeVisibleTags(raw: Set<String>?, allTags: Set<String>): Set<String> {
    if (raw.isNullOrEmpty()) {
        return defaultHomeVisibleTags().intersect(allTags).ifEmpty { allTags }
    }
    val filtered = raw.intersect(allTags)
    return filtered.ifEmpty { defaultHomeVisibleTags().intersect(allTags).ifEmpty { allTags } }
}

private fun normalizeNotificationQuickTagsFromRaw(raw: Set<String>?, allTags: List<String>): Set<String> {
    val allowed = allTags.toSet()
    val fallback = defaultNotificationQuickTags().intersect(allowed).ifEmpty { allowed.take(MAX_NOTIFICATION_QUICK_TAGS).toSet() }
    val base = when {
        raw.isNullOrEmpty() -> return fallback
        else -> raw.intersect(allowed).ifEmpty { return fallback }
    }
    if (base.size <= MAX_NOTIFICATION_QUICK_TAGS) return base
    return allTags.filter { it in base }.take(MAX_NOTIFICATION_QUICK_TAGS).toSet()
}

private fun parseCustomTagColorsJson(raw: String?, validCustomTags: Set<String>): Map<String, Int> {
    if (raw.isNullOrBlank() || validCustomTags.isEmpty()) return emptyMap()
    return try {
        val o = JSONObject(raw)
        val out = mutableMapOf<String, Int>()
        val keys = o.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            if (k in validCustomTags) {
                out[k] = o.getInt(k)
            }
        }
        out
    } catch (_: Exception) {
        emptyMap()
    }
}

private fun serializeCustomTagColors(map: Map<String, Int>): String {
    val o = JSONObject()
    map.entries.sortedBy { it.key }.forEach { (k, v) ->
        o.put(k, v)
    }
    return o.toString()
}

private fun normalizeNotificationQuickTags(tags: Set<String>, allTags: List<String>): Set<String> {
    val allowed = allTags.toSet()
    val fallback = defaultNotificationQuickTags().intersect(allowed).ifEmpty { allowed.take(MAX_NOTIFICATION_QUICK_TAGS).toSet() }
    val base = tags.intersect(allowed).ifEmpty { return fallback }
    if (base.size <= MAX_NOTIFICATION_QUICK_TAGS) return base
    return allTags.filter { it in base }.take(MAX_NOTIFICATION_QUICK_TAGS).toSet()
}

class UserPreferencesRepository(
    private val appContext: Context
) {
    val data: Flow<UserPreferences> = appContext.userPreferencesDataStore.data.map { prefs ->
        val customTags = normalizeCustomTags(prefs[KEY_CUSTOM_TAGS])
        val allTags = (SessionTags.defaults + customTags.sorted()).distinct()
        val allSet = allTags.toSet()

        val homeVisible = normalizeHomeVisibleTags(prefs[KEY_HOME_VISIBLE_TAGS], allSet)
        val quickTags = normalizeNotificationQuickTagsFromRaw(prefs[KEY_NOTIFICATION_QUICK_TAGS], allTags)
        val customTagColors = parseCustomTagColorsJson(prefs[KEY_CUSTOM_TAG_COLORS], customTags)

        UserPreferences(
            notificationsEnabled = prefs[KEY_NOTIFICATIONS_ENABLED] ?: true,
            locationSuggestionsEnabled = prefs[KEY_LOCATION_SUGGESTIONS_ENABLED] ?: true,
            customTags = customTags,
            homeVisibleTags = homeVisible,
            notificationQuickTags = quickTags,
            onboardingWelcomeCompleted = prefs[KEY_ONBOARDING_WELCOME_COMPLETED] ?: false,
            onboardingTipHomeSeen = prefs[KEY_ONBOARDING_TIP_HOME_SEEN] ?: false,
            onboardingTipHistorySeen = prefs[KEY_ONBOARDING_TIP_HISTORY_SEEN] ?: false,
            onboardingTipDashboardSeen = prefs[KEY_ONBOARDING_TIP_DASHBOARD_SEEN] ?: false,
            onboardingTipSettingsSeen = prefs[KEY_ONBOARDING_TIP_SETTINGS_SEEN] ?: false,
            customTagColors = customTagColors
        )
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        appContext.userPreferencesDataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setLocationSuggestionsEnabled(enabled: Boolean) {
        appContext.userPreferencesDataStore.edit { it[KEY_LOCATION_SUGGESTIONS_ENABLED] = enabled }
    }

    suspend fun setNotificationQuickTags(tags: Set<String>) {
        appContext.userPreferencesDataStore.edit { prefs ->
            val custom = normalizeCustomTags(prefs[KEY_CUSTOM_TAGS])
            val allTags = (SessionTags.defaults + custom.sorted()).distinct()
            prefs[KEY_NOTIFICATION_QUICK_TAGS] = normalizeNotificationQuickTags(tags, allTags)
        }
    }

    suspend fun addCustomTag(tag: String, colorArgb: Int) {
        val cleaned = tag.trim()
        if (cleaned.isEmpty()) return
        if (cleaned in SessionTags.defaults) return
        appContext.userPreferencesDataStore.edit { prefs ->
            val current = normalizeCustomTags(prefs[KEY_CUSTOM_TAGS])
            prefs[KEY_CUSTOM_TAGS] = (current + cleaned)
            val colorMap = parseCustomTagColorsJson(prefs[KEY_CUSTOM_TAG_COLORS], current + cleaned)
                .toMutableMap()
            colorMap[cleaned] = colorArgb
            prefs[KEY_CUSTOM_TAG_COLORS] = serializeCustomTagColors(colorMap)
        }
    }

    suspend fun deleteCustomTag(tag: String) {
        appContext.userPreferencesDataStore.edit { prefs ->
            val currentCustom = normalizeCustomTags(prefs[KEY_CUSTOM_TAGS])
            val nextCustom = currentCustom - tag
            val allTags = (SessionTags.defaults + nextCustom.sorted()).distinct()
            val allSet = allTags.toSet()

            prefs[KEY_CUSTOM_TAGS] = nextCustom
            prefs[KEY_HOME_VISIBLE_TAGS] = normalizeHomeVisibleTags(prefs[KEY_HOME_VISIBLE_TAGS], allSet)
            prefs[KEY_NOTIFICATION_QUICK_TAGS] = normalizeNotificationQuickTagsFromRaw(prefs[KEY_NOTIFICATION_QUICK_TAGS], allTags)
            val colorMap = parseCustomTagColorsJson(prefs[KEY_CUSTOM_TAG_COLORS], currentCustom).toMutableMap()
            colorMap.remove(tag)
            if (colorMap.isEmpty()) {
                prefs.remove(KEY_CUSTOM_TAG_COLORS)
            } else {
                prefs[KEY_CUSTOM_TAG_COLORS] = serializeCustomTagColors(colorMap)
            }
        }
    }

    suspend fun setHomeVisibleTags(tags: Set<String>) {
        appContext.userPreferencesDataStore.edit { prefs ->
            val custom = normalizeCustomTags(prefs[KEY_CUSTOM_TAGS])
            val allTags = (SessionTags.defaults + custom.sorted()).distinct()
            val allSet = allTags.toSet()
            prefs[KEY_HOME_VISIBLE_TAGS] = normalizeHomeVisibleTags(tags, allSet)
        }
    }

    suspend fun setOnboardingWelcomeCompleted(completed: Boolean) {
        appContext.userPreferencesDataStore.edit { it[KEY_ONBOARDING_WELCOME_COMPLETED] = completed }
    }

    suspend fun setOnboardingTipHomeSeen(seen: Boolean) {
        appContext.userPreferencesDataStore.edit { it[KEY_ONBOARDING_TIP_HOME_SEEN] = seen }
    }

    suspend fun setOnboardingTipHistorySeen(seen: Boolean) {
        appContext.userPreferencesDataStore.edit { it[KEY_ONBOARDING_TIP_HISTORY_SEEN] = seen }
    }

    suspend fun setOnboardingTipDashboardSeen(seen: Boolean) {
        appContext.userPreferencesDataStore.edit { it[KEY_ONBOARDING_TIP_DASHBOARD_SEEN] = seen }
    }

    suspend fun setOnboardingTipSettingsSeen(seen: Boolean) {
        appContext.userPreferencesDataStore.edit { it[KEY_ONBOARDING_TIP_SETTINGS_SEEN] = seen }
    }
}
