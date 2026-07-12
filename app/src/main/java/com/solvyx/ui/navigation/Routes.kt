package com.solvyx.ui.navigation

/**
 * Rutas de navegación de Solvyx.
 *
 * Se modelan como `sealed class SolvyxRoutes` para tener **tipado fuerte**
 * en las definiciones de rutas y `helpers` (`build(...)`) para evitar errores
 * al construir rutas con argumentos. Esto reemplaza al antiguo `object Routes`
 * con strings sueltos.
 *
 * ## Compatibilidad
 *
 * El `object Routes` legacy (con constantes `String`) se conserva al final
 * de este archivo para no romper callers existentes (`Routes.SPLASH`,
 * `Routes.HOME`, etc.). El código nuevo debería preferir `SolvxRoutes`.
 *
 * ## Convenciones
 *
 * - `route` es la cadena literal que el grafo de navegación entiende.
 *   Usa `{argumento}` para los placeholders.
 * - `build(...)` siempre devuelve una ruta completa y válida.
 * - `const val ARG_X` se usa desde el `NavGraph` con `navArgument(ARG_X)`.
 */
sealed class SolvyxRoutes(val route: String) {

    // ── Rutas existentes (pre-Fase 1) ────────────────────────────────────────

    /** Splash inicial (~1.5s). Decide a dónde ir. */
    object Splash : SolvyxRoutes("splash")
    object Onboarding : SolvyxRoutes("onboarding")
    object AuthChoice : SolvyxRoutes("auth_choice")
    object Login : SolvyxRoutes("login")
    object ForgotPassword : SolvyxRoutes("forgot_password")
    object Register : SolvyxRoutes("register")

    /** Envuelve `MainScreen` (drawer + bottom nav). */
    object Home : SolvyxRoutes("home")

    /**
     * Chat de Berto. Acepta `source` opcional (`drawer`, ...) para que al
     * volver se reabra el drawer si fue el origen.
     */
    object Chat : SolvyxRoutes("chat?source={source}") {
        const val ARG_SOURCE = "source"
        fun build(source: String = ""): String =
            if (source.isBlank()) "chat?source=" else "chat?source=$source"
    }

    object Diagnostico : SolvyxRoutes("diagnostico")
    object RedApoyoSetup : SolvyxRoutes("red_apoyo_setup")
    object SosOverlay : SolvyxRoutes("sos_overlay")

    /** Ejercicio 5-4-3-2-1 guiado (técnica de anclaje). */
    object EjercicioGuiado : SolvyxRoutes("ejercicio_guiado")

    // ── Rutas nuevas (Fase 1) ────────────────────────────────────────────────

    /** Listado de ejercicios de regulación emocional. */
    object Ejercicios : SolvyxRoutes("ejercicios")

    /** Detalle de un ejercicio específico (por slug). */
    object EjercicioDetalle : SolvyxRoutes("ejercicios/{slug}") {
        const val ARG_SLUG = "slug"
        fun build(slug: String): String = "ejercicios/$slug"
    }

    /**
     * Versión activa (TTS-guided) de un ejercicio. La pantalla reproduce los
     * pasos con voz y un círculo respiratorio animado.
     */
    object EjercicioActivo : SolvyxRoutes("ejercicios/{slug}/activo") {
        const val ARG_SLUG = "slug"
        fun build(slug: String): String = "ejercicios/$slug/activo"
    }

    /** Listado de guías extendidas (crisis, craving, consumo, etc.). */
    object GuiasExtendidas : SolvyxRoutes("guias-extendidas")

    /** Detalle de una guía extendida (por slug). */
    object GuiaDetalle : SolvyxRoutes("guias-extendidas/{slug}") {
        const val ARG_SLUG = "slug"
        fun build(slug: String): String = "guias-extendidas/$slug"
    }

    /** Catálogo de lecciones de psicoeducación por sustancia. */
    object Lecciones : SolvyxRoutes("lecciones")

    /** Detalle de una lección concreta (por sustancia + slug). */
    object LeccionDetalle : SolvyxRoutes("lecciones/{sustancia}/{slug}") {
        const val ARG_SUSTANCIA = "sustancia"
        const val ARG_SLUG = "slug"
        fun build(sustancia: String, slug: String): String =
            "lecciones/$sustancia/$slug"
    }

    /** Banco de prompts + entradas recientes del usuario. */
    object Journaling : SolvyxRoutes("journaling")

    /**
     * Editor de entrada de journaling.
     *
     * Acepta dos query params opcionales:
     * - `promptSlug`  → para deep-linking futuro y para identificar
     *   programáticamente el prompt desde la UI (Listado → Editor).
     * - `promptTexto` → el texto literal del prompt, que es el que el
     *   `JournalingEditorViewModel` lee de `SavedStateHandle` para
     *   precargar el estado inicial de la entrada.
     *
     * Si ambos son `null`, se abre como entrada libre.
     */
    object JournalingEditor : SolvyxRoutes("journaling/editor?promptSlug={promptSlug}&promptTexto={promptTexto}") {
        const val ARG_PROMPT_SLUG = "promptSlug"
        const val ARG_PROMPT_TEXTO = "promptTexto"
        fun build(promptSlug: String? = null, promptTexto: String? = null): String {
            val s = android.net.Uri.encode(promptSlug.orEmpty())
            val t = android.net.Uri.encode(promptTexto.orEmpty())
            return "journaling/editor?promptSlug=$s&promptTexto=$t"
        }
    }

    /** Listado de rutinas (matutina, nocturna, etc.). */
    object Rutinas : SolvyxRoutes("rutinas")

    /** Detalle de una rutina (por slug). */
    object RutinaDetalle : SolvyxRoutes("rutinas/{slug}") {
        const val ARG_SLUG = "slug"
        fun build(slug: String): String = "rutinas/$slug"
    }

    /** Pantalla de insights del motor de Berto. */
    object Insights : SolvyxRoutes("insights")

    /** Hub "Descubre Solvyx" con 4 categorías de recursos nuevos. */
    object Descubrir : SolvyxRoutes("descubrir")
}

/**
 * **DEPRECATED**. Conservado por compatibilidad con código que ya
 * referencia `Routes.SPLASH`, `Routes.HOME`, etc.
 *
 * Para navegar a una ruta con argumentos, usa `SolvxRoutes.X.build(...)`.
 */
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val AUTH_CHOICE = "auth_choice"
    const val LOGIN = "login"
    const val FORGOT_PASSWORD = "forgot_password"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CHAT = "chat"
    const val DIAGNOSTICO = "diagnostico"
    const val RED_APOYO_SETUP = "red_apoyo_setup"
    const val SOS_OVERLAY = "sos_overlay"
    const val EJERCICIO_GUIADO = "ejercicio_guiado"
}
