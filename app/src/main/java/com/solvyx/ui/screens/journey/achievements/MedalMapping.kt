package com.solvyx.ui.screens.journey.achievements

import com.solvyx.ui.screens.journey.UiAchievement
import com.solvyx.ui.screens.journey.achievements.components.DiaryBadgeColor
import com.solvyx.ui.screens.journey.achievements.components.MedalInfo
import com.solvyx.ui.screens.journey.achievements.components.streakTierColor

private const val DAYS_UNIT = "días"

/** [tierIndex] is the achievement's place among the milestones, which sets its medal color. */
fun UiAchievement.toMedalInfo(tierIndex: Int, streak: Int): MedalInfo = MedalInfo(
    title = title,
    description = description,
    icon = icon,
    color = streakTierColor(tierIndex),
    unlocked = unlocked,
    current = streak.coerceAtMost(threshold),
    target = threshold,
    unit = DAYS_UNIT,
    unlockedOn = unlockedOn
)

fun DiaryBadge.toMedalInfo(): MedalInfo = MedalInfo(
    title = title,
    description = description,
    icon = icon,
    color = DiaryBadgeColor,
    unlocked = unlocked,
    current = current,
    target = target,
    unit = ""
)

/** Tier of each achievement by its threshold, so colors stay the same wherever it is shown. */
fun tierIndexOf(achievement: UiAchievement, all: List<UiAchievement>): Int =
    all.sortedBy { it.threshold }.indexOfFirst { it.id == achievement.id }
