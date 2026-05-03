package com.example.cs501clockin.ui.util

import androidx.compose.ui.graphics.Color
import com.example.cs501clockin.model.SessionTags

object TagPalette {
    private val orderedTags = listOf(
        SessionTags.SCHOOL,
        SessionTags.WORK,
        SessionTags.TRAINING,
        SessionTags.FOOD,
        SessionTags.PERSONAL_CARE,
        SessionTags.RECOVERY_MIND,
        SessionTags.SOCIAL_ADMIN,
        SessionTags.IDLE
    )

    private val tagColors = mapOf(
        SessionTags.SCHOOL to Color(0xFF2E8DFF),
        SessionTags.WORK to Color(0xFF62C7F8),
        SessionTags.TRAINING to Color(0xFF3ECF56),
        SessionTags.FOOD to Color(0xFFFFA31A),
        SessionTags.PERSONAL_CARE to Color(0xFFFF3E6C),
        SessionTags.RECOVERY_MIND to Color(0xFFB05AE5),
        SessionTags.SOCIAL_ADMIN to Color(0xFF5C61E6),
        SessionTags.IDLE to Color(0xFF8F8F9B)
    )

    fun colorFor(tag: String, customArgbByTag: Map<String, Int> = emptyMap()): Color {
        customArgbByTag[tag]?.let { return it.argbToColor() }
        tagColors[tag]?.let { return it }
        return PastelTagColors.fallbackArgbForTagName(tag).argbToColor()
    }

    fun sortIndex(tag: String): Int = orderedTags.indexOf(tag).takeIf { it >= 0 } ?: Int.MAX_VALUE

    fun orderedWithFallback(tags: Collection<String>): List<String> {
        val known = orderedTags.filter { it in tags }
        val unknown = tags.filter { it !in orderedTags }.sorted()
        return known + unknown
    }
}
