package com.solvyx.ui.screens.home.stage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HomeStageLogicTest {

    private val moods = listOf("triste", "ansioso", "neutral", "bien", "euforico")

    @Test
    fun `time of day covers the whole clock`() {
        assertEquals(TimeOfDay.NIGHT, timeOfDayFor(4))
        assertEquals(TimeOfDay.MORNING, timeOfDayFor(5))
        assertEquals(TimeOfDay.MORNING, timeOfDayFor(11))
        assertEquals(TimeOfDay.AFTERNOON, timeOfDayFor(12))
        assertEquals(TimeOfDay.AFTERNOON, timeOfDayFor(18))
        assertEquals(TimeOfDay.EVENING, timeOfDayFor(19))
        assertEquals(TimeOfDay.NIGHT, timeOfDayFor(21))
        assertEquals(TimeOfDay.NIGHT, timeOfDayFor(0))
    }

    @Test
    fun `greeting uses the name but never the placeholder`() {
        assertEquals("¡Buenos días, Emma!", greetingFor(9, "Emma"))
        assertEquals("¡Buenas tardes!", greetingFor(15, PLACEHOLDER_NICKNAME))
        assertEquals("¡Buenas noches!", greetingFor(23, "  "))
        assertEquals("¡Buenas noches!", greetingFor(23, null))
    }

    @Test
    fun `streak line matches the garden and says 1 dia in singular`() {
        assertEquals("Tu primer día ya brotó.", streakLineFor(1))
        assertTrue(streakLineFor(0).contains("sembrar"))
        assertTrue(streakLineFor(5).contains("capullos"))
        assertTrue(streakLineFor(20).contains("mariposas"))
    }

    @Test
    fun `every mood has its own reaction line`() {
        val lines = moods.map { reactionLineFor(it) }
        assertEquals(moods.size, lines.toSet().size)
    }

    @Test
    fun `only sad and anxious get an idea without asking`() {
        assertEquals(listOf("triste", "ansioso"), moods.filter { suggestsRightAway(it) })
        assertEquals(listOf("bien", "euforico"), moods.filter { celebrates(it) })
    }

    @Test
    fun `anxious starts with breathing and every mood has ideas`() {
        assertEquals(SuggestionAction.BREATHE, suggestionsFor("ansioso").first().action)
        assertEquals(SuggestionAction.TALK, suggestionsFor("triste").first().action)
        (moods + listOf<String?>(null)).forEach { mood ->
            val ideas = suggestionsFor(mood)
            assertTrue(ideas.size >= 3)
            assertEquals(ideas.size, ideas.map { it.id }.toSet().size)
        }
    }

    @Test
    fun `poke lines wrap around`() {
        assertEquals(
            lineText(StageLine.Poke(0), 9, null, 0),
            lineText(StageLine.Poke(PokeLineCount), 9, null, 0)
        )
    }

    @Test
    fun `daily note changes from one day to the next`() {
        val today = LocalDate.of(2026, 9, 30)
        assertNotEquals(dailyNoteFor(today), dailyNoteFor(today.plusDays(1)))
        assertEquals(dailyNoteFor(today), dailyNoteFor(LocalDate.of(2026, 9, 30)))
    }

    @Test
    fun `garden stages follow the streak milestones`() {
        assertEquals(GardenStage.SEEDS, gardenStageFor(0))
        assertEquals(GardenStage.SPROUTS, gardenStageFor(2))
        assertEquals(GardenStage.BUDS, gardenStageFor(3))
        assertEquals(GardenStage.BLOOM, gardenStageFor(7))
        assertEquals(GardenStage.BUTTERFLIES, gardenStageFor(15))
        assertEquals(GardenStage.FULL_BLOOM, gardenStageFor(30))
    }

    @Test
    fun `no streak shows seeds and each day adds a plant up to the cap`() {
        assertTrue(gardenFor(0).all { it.kind == PlantKind.SEED })
        assertEquals(5, gardenFor(5).size)
        assertEquals(MAX_PLANTS, gardenFor(90).size)
    }

    @Test
    fun `plants never grow in front of Berto`() {
        listOf(0, 1, 2, 5, 9, 14, 40).forEach { streak ->
            gardenFor(streak).forEach { plant ->
                assertFalse(
                    "streak $streak put a plant at ${plant.x}",
                    plant.x > GARDEN_CENTER_START && plant.x < GARDEN_CENTER_END
                )
                assertTrue(plant.x in 0f..1f)
            }
        }
    }

    @Test
    fun `garden is stable between draws`() {
        assertEquals(gardenFor(12), gardenFor(12))
    }

    @Test
    fun `sunflowers only appear once the butterflies arrive`() {
        assertFalse(gardenFor(14).any { it.kind == PlantKind.SUNFLOWER })
        assertTrue(gardenFor(15).any { it.kind == PlantKind.SUNFLOWER })
        assertEquals(0, butterfliesFor(14))
        assertEquals(2, butterfliesFor(15))
        assertEquals(3, butterfliesFor(30))
    }

    @Test
    fun `fire grows with the streak and is off without one`() {
        assertEquals(0, embersFor(0))
        assertEquals(0f, glowFor(0))
        assertTrue(embersFor(10) > embersFor(1))
        assertTrue(glowFor(10) > glowFor(1))
        assertEquals(1f, glowFor(365))
    }

    @Test
    fun `Berto introduces himself the first time and after a week away`() {
        val today = LocalDate.of(2026, 9, 30)
        assertTrue(needsIntroduction(lastVisit = null, today = today))
        assertFalse(needsIntroduction(lastVisit = today, today = today))
        assertFalse(needsIntroduction(lastVisit = today.minusDays(6), today = today))
        assertTrue(needsIntroduction(lastVisit = today.minusDays(REINTRODUCE_AFTER_DAYS), today = today))
    }
}
