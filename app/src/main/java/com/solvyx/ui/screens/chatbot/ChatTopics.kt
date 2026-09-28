package com.solvyx.ui.screens.chatbot

import androidx.annotation.DrawableRes
import com.solvyx.R
import com.solvyx.ui.components.common.TrackedSubstances

/**
 * First step of the guided menu: what is going on. [treeSuffix] joins the substance id to build
 * the decision tree id ("alcohol_craving"); FEELING_BAD has no tree, it opens the support actions.
 */
enum class TopicIntent(
    val label: String,
    val description: String,
    @DrawableRes val icon: Int,
    val treeSuffix: String?
) {
    CRAVING("Tengo ganas de consumir", "Te ayudo a que las ganas pasen", R.drawable.ic_flame, "craving"),
    INFO("Quiero información", "Efectos y cómo reducir riesgos", R.drawable.ic_info_circle, "info"),
    FEELING_BAD("Me siento mal", "Respiramos o buscamos ayuda juntos", R.drawable.ic_heart_pulse, null);

    fun treeIdFor(substanceId: String): String? = treeSuffix?.let { "${substanceId}_$it" }
}

/** What an offline free-text message seems to be about; either part can be unknown. */
data class TopicMatch(val intent: TopicIntent?, val substanceId: String?) {
    val isEmpty: Boolean get() = intent == null && substanceId == null
}

/**
 * Without the AI, free text used to get "choose an option". This recognizes the common ways young
 * people name a substance or a craving so Berto can jump straight to the right guided topic.
 */
object OfflineTopicMatcher {

    private val substanceWords: Map<String, List<Regex>> = mapOf(
        "alcohol" to phrases("alcohol\\w*", "cerveza\\w*", "chela\\w*", "pisto", "chupe", "chupar", "beber", "tomar", "borrach\\w*", "tequila", "mezcal"),
        "vape" to phrases("vape\\w*", "vapear", "vapeo", "vapeador\\w*", "pods?", "cigarro electronico"),
        "cristal" to phrases("cristal", "foco", "meth\\w*", "metanfetamina\\w*", "ice", "vidrio"),
        "cigarro" to phrases("cigarr\\w*", "fumar", "tabaco", "cajetilla", "nicotina")
    )

    private val cravingWords = phrases(
        "ganas", "antojo\\w*", "craving", "se me antoja\\w*", "(quiero|necesito) (consumir|tomar|beber|fumar|vapear)",
        "no aguanto", "recaer", "recaida"
    )

    private val infoWords = phrases(
        "informacion", "info", "que es", "que hace", "efectos?", "riesgos?", "danos?", "como afecta", "es malo"
    )

    fun match(text: String): TopicMatch {
        val normalized = normalizeForMatching(text)
        val substance = TrackedSubstances.firstOrNull { option ->
            substanceWords[option.id].orEmpty().anyIn(normalized)
        }?.id
        val intent = when {
            cravingWords.anyIn(normalized) -> TopicIntent.CRAVING
            infoWords.anyIn(normalized) -> TopicIntent.INFO
            else -> null
        }
        return TopicMatch(intent, substance)
    }
}
