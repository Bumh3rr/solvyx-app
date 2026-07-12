package com.solvyx.backend.insights

/**
 * Insight generado por el motor offline a partir de la bitácora del usuario.
 *
 * Un [Insight] NO es un diagnóstico. Es una **observación** sobre patrones
 * que el motor encontró correlacionando entradas recientes. El texto final
 * lo diseña `backend-content-curator` con revisión de `psicologo-solvyx`; el
 * motor solo produce la estructura y los datos que el copy va a rellenar.
 *
 * Diseño:
 * - [id] es estable por regla (ej. `"sueno_bajo_esta_semana"`). Permite
 *   deduplicación y "visto por el usuario".
 * - [datos] lleva los valores que el copy necesita para rellenar el texto
 *   (ej. `promedio = 5.4`, `dias_con_datos = 5`). NO incluye contenido
 *   sensible (notas, sustancia completa).
 * - [accion] es opcional: indica qué pantalla/ruta abrir si el usuario
 *   toca el banner.
 */
data class Insight(
    val id: String,
    val tipo: TipoInsight,
    val severidad: Severidad,
    val ventanaTexto: String,
    val datos: Map<String, Any> = emptyMap(),
    val accion: AccionInsight? = null
)

/**
 * Naturaleza del insight.
 *
 * - [OBSERVACION]: el motor vio un patrón y lo reporta ("esta semana
 *   dormiste menos"). Sin consejo.
 * - [SUGERENCIA]: el motor detectó algo que amerita una invitación
 *   a la acción ("tus cravings se agrupan los martes, ¿quieres ver
 *   ejercicios de regulación?"). El texto lo diseña content-curator.
 * - [RECONOCIMIENTO]: refuerza positivamente una conducta del usuario
 *   ("llevas 7 días registrando"). Nunca combinado con severidad ALTA.
 */
enum class TipoInsight {
    OBSERVACION,
    SUGERENCIA,
    RECONOCIMIENTO
}

/**
 * Severidad / peso del insight para ordenar y filtrar.
 *
 * - [BAJA]: informativo, no urge. Nunca negativo.
 * - [MEDIA]: amerita atención del usuario pero sin alarma.
 * - [ALTA]: requiere palabra clave clínica validada por psicología.
 *   El motor no redacta copy de severidad alta; lo delega a
 *   `backend-content-curator`.
 *
 * El campo [peso] es la prioridad al ordenar resultados del motor
 * (mayor = más relevante). Mantener una jerarquía discreta permite
 * cambiar el ordenamiento sin tocar reglas.
 */
enum class Severidad(val peso: Int) {
    BAJA(1),
    MEDIA(2),
    ALTA(3)
}

/**
 * Acción sugerida cuando el usuario toca el banner del insight.
 *
 * - [destino] es la ruta de navegación o identificador de pantalla.
 *   Si es `null`, el motor solo recomienda mostrar el insight sin
 *   acción concreta.
 */
data class AccionInsight(
    val tipo: TipoAccion,
    val destino: String? = null
)

/**
 * Tipo de acción que el insight sugiere al usuario.
 *
 * - [VER_BITACORA]: abrir la pantalla de bitácora.
 * - [VER_EJERCICIO]: abrir el detalle de un ejercicio (usar [destino]
 *   con el id del ejercicio).
 * - [HABLAR_BERTO]: abrir el chat con Berto (asistente del chatbot).
 * - [AGENDAR_RUTINA]: abrir la pantalla de rutinas para que el usuario
 *   programe una.
 * - [NINGUNA]: el insight es solo informativo, sin llamada a la acción.
 */
enum class TipoAccion {
    VER_BITACORA,
    VER_EJERCICIO,
    HABLAR_BERTO,
    AGENDAR_RUTINA,
    NINGUNA
}