package com.solvyx.ui.screens.profile

import com.solvyx.ui.screens.profile.model.SafetyItem
import com.solvyx.ui.screens.profile.model.SafetyStep

/**
 * The "Tu red de seguridad" checklist, most important first. The account step only exists for
 * guests: a registered user has nothing to do there, so listing it would be noise.
 */
fun safetyItems(
    contactCount: Int,
    hasAssessment: Boolean,
    hasSubstances: Boolean,
    isAnonymous: Boolean
): List<SafetyItem> = buildList {
    add(SafetyItem(SafetyStep.SUPPORT_CONTACTS, done = contactCount > 0))
    add(SafetyItem(SafetyStep.ASSESSMENT, done = hasAssessment))
    add(SafetyItem(SafetyStep.SUBSTANCES, done = hasSubstances))
    if (isAnonymous) add(SafetyItem(SafetyStep.ACCOUNT, done = false))
}

/** Share of the checklist already done, 0..1. */
fun List<SafetyItem>.completion(): Float = if (isEmpty()) 1f else count { it.done }.toFloat() / size
