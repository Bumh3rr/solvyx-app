package com.solvyx.ui.components.drawer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.solvyx.ui.components.drawer.model.NavigationItem
import com.solvyx.ui.theme.SolvyxappTheme
import org.junit.Rule
import org.junit.Test

/**
 * Tests de scroll del [CustomDrawer].
 *
 * Verifican que cuando el contenido del drawer excede la altura de
 * pantalla (por ejemplo, después de agregar más items en el rediseño
 * UI/UX), el usuario puede hacer scroll y acceder a TODOS los items,
 * incluyendo "Cerrar sesión" que está al final.
 */
class CustomDrawerScrollTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setDrawerContent() {
        composeTestRule.setContent {
            SolvyxappTheme {
                CustomDrawer(
                    selectedNavigationItem = NavigationItem.Inicio,
                    userNickname = "Alex",
                    onNavigationItemClick = {},
                    onCloseClick = {},
                    onProfileClick = {}
                )
            }
        }
    }

    @Test
    fun drawer_last_item_is_reachable_by_scrolling() {
        setDrawerContent()

        composeTestRule.onNodeWithText("Cerrar sesión")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun drawer_all_navigation_items_are_present() {
        setDrawerContent()

        val expectedItems = listOf(
            "Inicio",
            "Mi Plan",
            "Registro diario",
            "Mis Avances",
            "Rutinas",
            "Hablar con Berto",
            "Ejercicios",
            "Guías de primeros auxilios",
            "Psicoeducación",
            "Journaling",
            "Insights de Berto",
            "Directorio Profesional",
            "Descubrir Solvyx",
            "Mi Perfil"
        )

        expectedItems.forEach { item ->
            composeTestRule.onNodeWithText(item)
                .performScrollTo()
                .assertIsDisplayed()
        }
    }

    @Test
    fun drawer_guia_submenu_shows_extended_items_when_expanded() {
        composeTestRule.setContent {
            SolvyxappTheme {
                CustomDrawer(
                    selectedNavigationItem = NavigationItem.GuiasPrimerosAuxilios,
                    userNickname = "Alex",
                    onNavigationItemClick = {},
                    onCloseClick = {},
                    onProfileClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Desregulación / flashback")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Violencia sexual reciente")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
