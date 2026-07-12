---
description: Marco de desarrollo adolescente y joven adulto (15-25 años) para auditar contenido psicoeducativo de Solvyx. Vocabulario validado, tono respetuoso, explicaciones sin moralizar, sin infantilizar.
---

# Skill: Psicoeducación para Adolescentes y Jóvenes Adultos

Esta skill te entrega los criterios para evaluar y proponer contenido psicoeducativo en Solvyx, dirigido a personas de 15 a 25 años. Aplícala cuando el hallazgo involucre explicaciones sobre consumo, craving, tolerancia, abstinencia, ansiedad, regulación emocional, o cualquier copy que pretenda enseñar algo.

## Cuándo cargar esta skill

- Guías de primeros auxilios (`ui/screens/guias/`).
- Berto cuando entrega información por sustancia (`alcohol_info`, `cristal_info`, `vape_info`, `cigarro_info`).
- Cualquier texto que use las palabras: "craving", "tolerancia", "abstinencia", "consumo", "sustancia", "adicción", "dependencia".
- Onboarding y pantallas de resultado del ASSIST.
- Insights de Berto en `Mis Avances`.
- Mensajes de la Bitácora.

## Marco del desarrollo (15-25 años)

| Aspecto | Implicación para el copy |
|---|---|
| **Identidad en construcción** | Evitar mensajes que definan al usuario ("eres adicto"). Usar siempre verbos de comportamiento ("consumes", "usas"), no sustantivos de identidad ("adict@", "dependiente"). |
| **Presión de pares** | Reconocer explícitamente que muchas decisiones suceden en grupo. No culpar al individuo por el contexto. |
| **Búsqueda de autonomía** | Ofrecer opciones, nunca imponer. "Puedes...", "Una opción es...", "Tú decides si...". |
| **Neurodesarrollo prefrontal en curso** | Explicar que la impulsividad y la búsqueda de sensaciones son esperables, no defectos. Sin condescendencia. |
| **Regulación emocional** | Los consumos suelen ser intentos legítimos (aunque riesgosos) de autorregulación. Reconocer la función antes de evaluar la conducta. |
| **Límites imprecisos riesgo/beneficio** | A esta edad el cerebro pesa menos las consecuencias a largo plazo. Las decisiones informadas requieren info concreta y contextualizada, no sermones. |
| **Vínculo con la tecnología** | El bot puede ser un primer punto de contacto válido. No sustituir, pero sí acercar. |

## Vocabulario validado vs. vocabulario a evitar

| Mejor | Evitar | Por qué |
|---|---|---|
| Persona que consume alcohol | Alcohólico, borracho, ebrio | "Alcohólico" es identidad y estigma. "Borracho/ebrio" es juicio moral. |
| Consumo problemático / uso problemático | Adicción, drogadicción | "Adicción" patologiza; "consumo problemático" describe el patrón y permite espectro. |
| Persona con dependencia a... | Adicto a... | Mismo motivo. |
| Consumió de nuevo / volvió a consumir | Recaída | "Recaída" asume abstinencia como línea base; muchas personas en Solvyx no están en abstinencia. |
| Sin consumo actualmente | Limpio / sucio | "Limpio/sucio" moraliza. |
| Craving / antojo intenso | Ansia, desesperación | "Craving" es término técnico normalizado; "antojo" lo humaniza. Evita "desesperación" en clínico. |
| Reducción de daños / uso más seguro | Abuso, mal uso | No emitir juicio moral sobre la conducta. |
| Espacio seguro | Refugio | "Refugio" puede connotar evitación. "Espacio seguro" es estructural. |
| Lo que estás sintiendo | Lo que te pasa | Evitar lejanía. Lo segundo suena a evento externo, no a experiencia interna. |
| Vamos paso a paso | Anímate, esfuérzate | Evita imperativos de voluntad. |

## Reglas de segunda persona

- **Tutéo respetuoso** ("tú") por defecto. "Usted" solo si el usuario lo pide o si el contexto Berto se dirige a un adulto formal.
- **Evitar diminutivos** ("consentidito", "pobrecito", "tesoro") — infantilizan y pueden incomodar.
- **Evitar diminutivos también en sustantivos** ("problemita", "ratito") — minimizan.
- **Evitar imperativos de voluntad** ("tú puedes", "tienes que querer", "esfuérzate"). Mejor: "es posible que...", "hay evidencia de que...", "muchas personas encuentran útil...".
- **Reconocer el esfuerzo** sin sobreexaltarlo. "Has avanzado" sí; "Eres increíble, lo estás logrando" no (genera presión).
- **Evitar lenguaje de premio/castigo.** "Si consumes, malas consecuencias; si no consumes, premio" es `[IMPORTANTE]`.

## Cómo explicar conceptos técnicos sin perder calidez

Usa la fórmula **definición + función + espectro + opción**:

### Craving

> "El craving es la urgencia intensa de consumir una sustancia. Aparece como una ola: sube rápido, tiene un pico, y baja en unos 15-20 minutos. Tiene una función: tu cerebro asocia la sustancia con alivio rápido. Reconocer la ola es el primer paso para decidir qué hacer con ella. ¿Quieres que practiquemos cómo esperar la ola juntos?"

### Tolerancia

> "La tolerancia es cuando tu cuerpo se acostumbra a una sustancia y necesitas más para sentir el mismo efecto. No significa que seas 'más fuerte' ni 'más débil'; es un cambio fisiológico esperable. Si quieres reducir el consumo, hacerlo gradualmente es más seguro que hacerlo de golpe."

### Consumo problemático

> "Un consumo se vuelve problemático cuando empieza a interferir con cosas que te importan: sueño, estudio, relaciones, ánimo. No todas las personas que consumen tienen un consumo problemático, y la línea se mueve con el tiempo. Si quieres, podemos revisar juntos qué tan problemático es para ti ahora."

### Regulación emocional

> "Muchas personas usan sustancias para manejar emociones difíciles: ansiedad, tristeza, aburrimiento, presión. Eso no las hace malas personas; las hace personas buscando alivio. Hay otras formas de conseguir ese alivio, y conocerlas es una herramienta más."

## Reglas de auditoría que aplicas

1. **Patologización.** Si algún texto define al usuario por su consumo ("eres adicto", "tienes una enfermedad"), es `[IMPORTANTE]`.
2. **Moralización.** Si hay juicios de valor ("está mal", "no deberías", "cómo se te ocurre"), es `[IMPORTANTE]`.
3. **Infantilización.** Si usa diminutivos o un tono de "sé que es difícil pero sé fuerte" sin contenido, es `[MEJORA]` o `[IMPORTANTE]` si es reiterado.
4. **Falsa dicotomía.** Si presenta solo "abstinencia total o seguir igual", sin mencionar reducción gradual o reducción de daños, es `[IMPORTANTE]`.
5. **Incomprensibilidad.** Si un término técnico no se explica al menos la primera vez, es `[MEJORA]`.
6. **Heteronormatividad o generización forzada.** Si usa solo femenino o solo masculino asumiendo género, es `[MEJORA]`.
7. **Ausencia de espectro.** Si dice "siempre" o "nunca" sobre efectos del consumo, es `[MEJORA]` (mejor usar probabilidades y rangos).
8. **Sobreexigencia.** Si pide "esfuerzo", "voluntad", "compromiso" como eje del cambio, es `[MEJORA]` (sustituir por acompañamiento).

## Cómo reportar hallazgos de esta skill

- **Severidad**.
- **Ubicación**.
- **Evidencia**.
- **Riesgo** (qué puede generar en un joven de 15-25: rechazo, abandono de la app, estigma internalizado, deserción).
- **Propuesta** (texto alternativo usando la fórmula o las plantillas de esta skill).
- **Principio violado** (patologización, moralización, infantilización, falsa dicotomía, etc.).

## Frase de cierre para tus reportes cuando uses esta skill

> "Auditoría realizada con marco de desarrollo adolescente y joven adulto (15-25 años) y principios de lenguaje validado en lugar de lenguaje estigmatizante."
