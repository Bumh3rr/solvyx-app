package com.solvyx.ui.screens.profile.legal

import com.solvyx.R
import com.solvyx.backend.repository.SUPPORT_EMAIL
import com.solvyx.ui.components.common.EMERGENCY_NUMBER
import com.solvyx.ui.components.common.HelpLine

private const val LAST_UPDATED = "29 de septiembre de 2026"
private const val FOOTER = "© 2026 Solvyx — Tecnologías para la Salud Humana. Desarrollado en InnovaTec 2026."

/**
 * What Solvyx really does with data, written to be read by someone of 13. Keep it true to the code:
 * if a data flow changes (a new Firestore field, a new service), this text changes with it.
 */
val PrivacyDocument = LegalDocumentContent(
    title = "Política de privacidad",
    lastUpdated = LAST_UPDATED,
    intro = "Aquí te explicamos, sin letras chiquitas, qué datos usa Solvyx, para qué, con quién se " +
        "comparten y cómo puedes borrarlos.",
    sections = listOf(
        LegalSection(
            title = "Qué guardamos si creas una cuenta",
            icon = R.drawable.ic_user,
            bullets = listOf(
                "Tu correo y tu contraseña. La contraseña la protege Google (Firebase); nosotros no podemos verla.",
                "Tu apodo y tu fecha de nacimiento.",
                "Las sustancias que elegiste y tus resultados del cuestionario ASSIST.",
                "Tu bitácora: cómo te sentiste, si hubo consumo, la sustancia, la cantidad aproximada y tus notas.",
                "Tu racha, tus logros y tu meta del día.",
                "Cada vez que usas el botón SOS: la fecha y cuántos contactos se avisaron (no sus números)."
            )
        ),
        LegalSection(
            title = "Qué se queda solo en tu teléfono",
            icon = R.drawable.ic_lock,
            bullets = listOf(
                "Tus contactos de emergencia: nombres y números nunca salen de tu teléfono.",
                "Tu ubicación, si activas \"Incluir mi ubicación en el SOS\": se toma solo al presionar " +
                    "SOS y va únicamente en el SMS a tus contactos. No se guarda ni se sube a ningún servidor.",
                "Si usas Solvyx como invitado, todo se queda en tu teléfono y se borra al cerrar sesión."
            )
        ),
        LegalSection(
            title = "Dónde se guarda",
            icon = R.drawable.ic_shield,
            paragraphs = listOf(
                "Los datos de tu cuenta se guardan en Firebase, un servicio de Google para cuentas y bases " +
                    "de datos. Google solo los guarda por nosotros.",
                "Si la app falla, Firebase Crashlytics envía un informe técnico (modelo de teléfono, versión " +
                    "de Android y dónde falló la app) para poder arreglarlo. No incluye tu bitácora ni tus notas."
            )
        ),
        LegalSection(
            title = "Con quién se comparte",
            icon = R.drawable.ic_people,
            bullets = listOf(
                "Berto con IA, solo si lo aceptas: lo que escribes o dictas en el chat se envía a nuestro " +
                    "servidor y a DeepSeek, un servicio de inteligencia artificial externo, junto con un " +
                    "identificador de tu cuenta (no tu nombre). El historial del chat se guarda en nuestro " +
                    "servidor. Si no aceptas, Berto te acompaña con temas guiados y no se envía nada.",
                "Botón SOS: envía un SMS desde tu línea a tus contactos de emergencia, con un enlace de " +
                    "Google Maps a tu ubicación si lo activaste. Para obtenerla se usa el servicio de " +
                    "ubicación de tu teléfono (Google).",
                "Dictado por voz: usa el reconocedor de voz de tu teléfono (Google).",
                "Botones de llamar: solo abren tu marcador; Solvyx no hace ni escucha llamadas.",
                "No vendemos tus datos ni los usamos para publicidad."
            )
        ),
        LegalSection(
            title = "Qué funciona sin internet",
            icon = R.drawable.ic_wifi_off,
            paragraphs = listOf(
                "Los temas guiados de Berto, primeros auxilios, los ejercicios de respiración, el directorio " +
                    "y el SMS del botón SOS funcionan sin internet. Tu cuenta, tu bitácora y tus logros " +
                    "necesitan conexión para guardarse en la nube; lo que registres sin internet se sube " +
                    "cuando vuelva la conexión."
            )
        ),
        LegalSection(
            title = "Si tienes menos de 18 años",
            icon = R.drawable.ic_heart,
            paragraphs = listOf(
                "Solvyx es para personas de 13 a 24 años. Si tienes menos de 18, te recomendamos usarla " +
                    "con el conocimiento de tu mamá, tu papá, tu tutor o una persona adulta de confianza."
            )
        ),
        LegalSection(
            title = "Tus derechos y cómo borrar tus datos",
            icon = R.drawable.ic_trash,
            bullets = listOf(
                "Puedes ver y corregir tu perfil en Mi perfil.",
                "Puedes borrar tu cuenta y todos sus datos cuando quieras en Mi perfil → Eliminar mi cuenta.",
                "Para borrar tu historial del chat con IA, pedir una copia de tus datos o cualquier duda, " +
                    "escríbenos a $SUPPORT_EMAIL."
            )
        )
    ),
    footer = FOOTER
)

val TermsDocument = LegalDocumentContent(
    title = "Términos y condiciones",
    lastUpdated = LAST_UPDATED,
    intro = "Al usar Solvyx aceptas estos términos. Léelos con calma: son cortos y están pensados para " +
        "cuidarte.",
    sections = listOf(
        LegalSection(
            title = "Qué es Solvyx",
            icon = R.drawable.ic_info_circle,
            paragraphs = listOf(
                "Solvyx es una herramienta de apoyo y reducción de daños para jóvenes de 13 a 24 años. " +
                    "No es un servicio médico y no sustituye la atención de profesionales de la salud."
            )
        ),
        LegalSection(
            title = "En una emergencia",
            icon = R.drawable.ic_alert_triangle,
            urgent = true,
            paragraphs = listOf("Si tu vida o la de alguien más corre peligro, no esperes:"),
            bullets = listOf(
                "Llama al $EMERGENCY_NUMBER.",
                "${HelpLine.LINEA_DE_LA_VIDA.displayName}: ${HelpLine.LINEA_DE_LA_VIDA.displayNumber} (gratis, 24 horas).",
                "${HelpLine.SAPTEL.displayName}: ${HelpLine.SAPTEL.displayNumber} (24 horas)."
            )
        ),
        LegalSection(
            title = "Berto y la inteligencia artificial",
            icon = R.drawable.ic_chat,
            paragraphs = listOf(
                "Berto responde con temas guiados o, si lo aceptas, con inteligencia artificial. La IA puede " +
                    "equivocarse o no entenderte: no es una persona ni un profesional. No tomes decisiones " +
                    "sobre tu salud solo con lo que te diga."
            )
        ),
        LegalSection(
            title = "Botón SOS",
            icon = R.drawable.ic_sos,
            paragraphs = listOf(
                "El botón SOS envía un SMS a tus contactos de emergencia desde tu línea. Depende de tu saldo, " +
                    "de la señal y de que los números estén bien escritos. No avisa a servicios de " +
                    "emergencia: para eso llama al $EMERGENCY_NUMBER.",
                "Si activas compartir tu ubicación, el SMS incluye un enlace con tu posición. Puede ser " +
                    "aproximada o faltar si el GPS está apagado o no hay señal."
            )
        ),
        LegalSection(
            title = "Información sobre sustancias",
            icon = R.drawable.ic_guide,
            paragraphs = listOf(
                "La información sobre alcohol, vape, cristal y tabaco busca reducir riesgos y daños. " +
                    "No promueve el consumo."
            )
        ),
        LegalSection(
            title = "Edad",
            icon = R.drawable.ic_birthday,
            paragraphs = listOf(
                "Solvyx está pensada para personas de 13 a 24 años. Si tienes menos de 18, úsala con el " +
                    "conocimiento de una persona adulta de confianza."
            )
        ),
        LegalSection(
            title = "Tu cuenta",
            icon = R.drawable.ic_lock,
            paragraphs = listOf(
                "Cuida tu contraseña y no la compartas. Puedes eliminar tu cuenta y tus datos cuando quieras " +
                    "desde Mi perfil. Lo que hacemos con tus datos está en la Política de privacidad."
            )
        ),
        LegalSection(
            title = "Responsabilidad",
            icon = R.drawable.ic_shield,
            paragraphs = listOf(
                "Solvyx no se hace responsable de decisiones tomadas solo con base en la información de la " +
                    "app. Ante cualquier duda sobre tu salud, consulta a un profesional."
            )
        ),
        LegalSection(
            title = "Cambios y contacto",
            icon = R.drawable.ic_email,
            paragraphs = listOf(
                "Podemos actualizar estos términos; la fecha de arriba indica la versión vigente. " +
                    "Si tienes dudas, escríbenos a $SUPPORT_EMAIL."
            )
        )
    ),
    footer = FOOTER
)
