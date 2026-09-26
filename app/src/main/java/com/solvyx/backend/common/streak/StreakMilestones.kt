package com.solvyx.backend.common.streak

import com.solvyx.backend.data.model.Achievement

/**
 * Where a streak stands relative to the milestone ladder: the next milestone to reach and how far
 * (0..1) the streak is between the previous milestone and that one. Past the last milestone,
 * [next] stays at the last one and [progress] is 1.
 */
data class MilestoneProgress(val next: Int, val progress: Float)

fun milestoneProgress(
    streak: Int,
    milestones: List<Int> = Achievement.MILESTONE_DAYS
): MilestoneProgress {
    val next = milestones.firstOrNull { it > streak } ?: return MilestoneProgress(milestones.last(), 1f)
    val previous = milestones.lastOrNull { it <= streak } ?: 0
    val progress = (streak - previous).toFloat() / (next - previous).coerceAtLeast(1)
    return MilestoneProgress(next, progress)
}
