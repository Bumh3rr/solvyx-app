package com.solvyx.ui.screens.plan

import com.solvyx.ui.components.common.TrackedSubstances
import com.solvyx.ui.screens.chatbot.TopicIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubstanceInfoContentTest {

    private fun allTexts(info: SubstanceInfo): List<String> =
        listOf(info.essentials, info.careTitle, info.emergency) + info.bodyAndMind + info.careTips +
            info.riskyMixes.flatMap { listOf(it.with, it.why) } + info.helpTips

    @Test
    fun `the user's substances come first, the rest keep the catalog order`() {
        assertEquals(listOf("vape", "cigarro", "alcohol", "cristal"), orderedSubstanceInfos(listOf("cigarro", "vape")).map { it.id })
        assertEquals(listOf("alcohol", "cristal", "vape", "cigarro"), orderedSubstanceInfos(emptyList()).map { it.id })
    }

    @Test
    fun `there is content for every substance the app tracks and a chat guide for each`() {
        assertEquals(TrackedSubstances.map { it.id }.toSet(), SubstanceInfos.map { it.id }.toSet())
        SubstanceInfos.forEach { assertTrue(it.id, TopicIntent.INFO.treeIdFor(it.id) == "${it.id}_info") }
    }

    @Test
    fun `every substance fills its five sections`() {
        SubstanceInfos.forEach { info ->
            assertTrue(info.id, info.essentials.isNotBlank())
            assertTrue(info.id, info.bodyAndMind.isNotEmpty())
            assertTrue(info.id, info.careTips.isNotEmpty())
            assertTrue(info.id, info.riskyMixes.isNotEmpty())
            assertTrue(info.id, info.emergency.startsWith("Llama al 911"))
            assertTrue(info.id, info.helpTips.isNotEmpty())
        }
    }

    @Test
    fun `the old plan tips live on in every substance`() {
        SubstanceInfos.forEach { info ->
            val care = info.careTips.joinToString(" ")
            assertTrue(info.id, care.contains("15 minutos"))
            assertTrue(info.id, care.contains("alguien de confianza"))
            assertTrue(info.id, care.contains("agua"))
        }
    }

    @Test
    fun `no words that judge`() {
        val banned = listOf("limpio", "sobriedad", "recaída", "fallaste", "perdiste", "adicto", "drogadicto")
        SubstanceInfos.forEach { info ->
            allTexts(info).forEach { text ->
                banned.forEach { word -> assertTrue("${info.id}: \"$word\" en \"$text\"", !text.contains(word, ignoreCase = true)) }
            }
        }
    }
}
