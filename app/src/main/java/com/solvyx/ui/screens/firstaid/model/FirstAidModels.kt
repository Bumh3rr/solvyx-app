package com.solvyx.ui.screens.firstaid.model

import androidx.annotation.DrawableRes

/** Visual urgency of a card or section. Drives its colors, never its layout. */
enum class GuideTone { CALM, WARNING, URGENT }

/**
 * What Berto should convey on a first-aid screen. Mapped to a concrete pose in one place
 * (`FirstAidBerto.kt`), so new Rive poses only need to be wired there.
 */
enum class BertoMood { WELCOMING, CALMING, WORRIED, POINTING, PROUD }

/** Berto's reassurance at the top of a guide. */
data class GuideHero(
    val mood: BertoMood,
    val message: String,
    val supporting: String
)

/** Interactive "do this now" checklist: the user ticks each step as they go. */
data class ActionPlan(
    val title: String,
    val steps: List<String>,
    val completionMessage: String
)

/** A block of supporting information, shown as a card (collapsible or not). */
data class InfoSection(
    @DrawableRes val icon: Int,
    val title: String,
    val tone: GuideTone = GuideTone.CALM,
    val paragraph: String? = null,
    val bullets: List<String> = emptyList()
)

/** Hub entry for one situation the user can be going through. */
data class FirstAidSituation(
    val route: FirstAidRoute,
    @DrawableRes val icon: Int,
    val title: String,
    val subtitle: String,
    val tone: GuideTone = GuideTone.CALM
)

/** How loud the "notify my support network" action is on a given guide. */
enum class SosEmphasis { FILLED, OUTLINED }

/** Destinations of the first-aid module's own NavHost. */
enum class FirstAidRoute(val path: String) {
    HUB("firstAidHub"),
    CRISIS("crisis"),
    PANIC("panic"),
    CRAVING("craving"),
    OVERUSE("overuse"),
    CRISIS_CHECK("crisisCheck"),
    GROUNDING("grounding")
}

/** Valid substances only (business rule): alcohol · vape · cristal · cigarro (shown as "Tabaco"). */
enum class GuideSubstance(val label: String) {
    ALCOHOL("Alcohol"),
    CRISTAL("Cristal"),
    VAPE("Vape"),
    CIGARRO("Tabaco")
}
