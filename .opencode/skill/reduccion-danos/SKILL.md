---
description: Auditoría del contenido de Solvyx bajo el paradigma de reducción de daños. Mensajes no coercitivos, alternativas seguras, abstinencia como una opción entre varias, nunca la única.
---

# Skill: Reducción de Daños

Esta skill te entrega criterios para auditar y proponer copy de Solvyx bajo el paradigma de reducción de daños (harm reduction). Aplícala cuando el hallazgo involucre lenguaje sobre consumo, consecuencias, alternativas o metas del usuario.

## Cuándo cargar esta skill

- Cualquier mención a "abstinencia", "dejar de consumir", "dejar la droga", "recuperarse", "rehabilitación".
- Información por sustancia en Berto (`alcohol_info`, `cristal_info`, `vape_info`, `cigarro_info`).
- Pantallas de Mi Plan y Avances.
- Resultados del ASSIST en nivel ALTO (deben ofrecer reducción de daños, no solo tratamiento).
- Mensajes motivacionales, logros, "rachas".

## Principios del paradigma

1. **La abstinencia es una opción, no la única.** Reducir cantidad, espaciar consumo, cambiar de sustancia, usar en contextos más seguros, son metas igualmente válidas.
2. **No emitir juicio moral sobre la conducta.** El consumo no es "bueno" ni "malo"; tiene riesgos y funciones. Una persona no es mejor ni peor por consumir.
3. **Acompañar donde la persona está.** Si quiere dejar de consumir, se le acompaña. Si quiere reducir, se le acompaña. Si todavía no quiere cambiar nada, se valida y se ofrece información para reducir daños.
4. **Información concreta, no sermones.** Decir "el alcohol daña el hígado" no es suficiente; decir "tomar agua entre cada copa reduce la deshidratación y el riesgo de resaca severa" sí lo es.
5. **Reconocer el placer y la función.** Muchos consumos tienen función social, ritual, regulatoria. Negarlo desconecta. Reconocerlo abre diálogo.
6. **El daño se reduce en cualquier dirección.** Menos cantidad, menos frecuencia, contextos más seguros, sustancias más seguras, mejor hidratación, mejor alimentación, sueño, red de apoyo presente.
7. **El usuario es experto en su vida.** Las decisiones son suyas. El bot informa, acompaña, sugiere; no prescribe.

## Lenguaje de reducción de daños vs. lenguaje prohibicionista

| Lenguaje prohibicionista (evitar) | Lenguaje de reducción de daños (preferir) |
|---|---|
| "Debes dejar de consumir" | "Si quieres reducir el consumo, hay varias estrategias que han funcionado para otras personas" |
| "El alcohol es malo" | "El alcohol tiene efectos en tu cuerpo a corto y largo plazo. Aquí información concreta para tomar decisiones" |
| "No consumas" | "Si decides consumir, hay formas de hacerlo con menos riesgo" |
| "Eres adicto, necesitas ayuda" | "El patrón que describes tiene componentes que se beneficiarian de acompañamiento profesional. Aquí opciones" |
| "Recaíste, perdiste tu progreso" | "Volviste a consumir. Eso no borra lo avanzado. ¿Quieres que veamos qué pasó y qué necesitas ahora?" |
| "Limpio / sobrio" | "Sin consumo actualmente" o "en tu proceso de reducción" |
| "Solo se puede dejar con voluntad" | "Dejar o reducir un consumo tiene componentes biológicos, sociales y contextuales. No depende solo de fuerza de voluntad" |

## Alternativas seguras por sustancia

Cuando Berto entregue información, sugiere alternativas concretas, no solo abstinencia.

### Alcohol

- **Espaciar:** una bebida con agua entre cada copa.
- **Limitar:** máximo X tragos por ocasión (no decir "no tomar").
- **Contexto:** evitar consumir solo o en ayunas.
- **Señales de alarma:** si vomitas, te desmayas o no recuerdas, pedir ayuda a alguien de confianza.
- **Conducción:** nunca酒后驾驶 (cero alcohol al volante).
- **Interacción con medicamentos:** si tomas algún medicamento, preguntar a profesional.

### Vape / cigarros electrónicos

- **Nicotine awareness:** reconocer que la mayoría de vapes contienen nicotina, que es adictiva.
- **Reducción gradual:** reducir concentración de nicotina en el líquido, espaciar las caladas.
- **Contexto:** evitar vapear en espacios cerrados compartidos; el vapor pasivo afecta a otros.
- **Líquidos:** comprar de fuentes confiables para evitar líquidos adulterados.
- **Daño pulmonar:** si aparecen tos persistente, falta de aire, dolor torácico, buscar atención médica.

### Cristal (metanfetamina)

- **Hidrátate:** beber agua regularmente durante el consumo.
- **Come:** aunque el apetito baje, intentar pequeñas comidas.
- **Descansa:** cada cierto tiempo, parar y acostarse.
- **No compartir:** pipa, jeringas, boquillas. Si se comparte, esterilizar.
- **Sexo y sustancias:** tener siempre preservativos a mano; el cristal puede aumentar conductas de riesgo.
- **Señales de alarma:** dolor en pecho, fiebre, agitación extrema, ideas de hacerse daño, buscar ayuda profesional.

### Tabaco / cigarrillo

- **Reducción gradual:** fijar un número máximo por día y bajarlo progresivamente.
- **Sustitutos:** chicles de nicotina, inhaladores, pastillas (no vape como sustituto, salvo criterio profesional).
- **Contextos:** no fumar dentro de casa ni en el coche.
- **Apoyo:** la Línea de la Vida tiene programas de cesación tabáquica.

## Reglas de auditoría que aplicas

1. **Abstinencia como única salida.** Si una pantalla o mensaje implica que "lo único válido es dejar de consumir totalmente", es `[CRÍTICO]` (excluye a gran parte de la población usuaria).
2. **Moralización.** Si hay juicios del tipo "no deberías", "es malo", "está mal", es `[IMPORTANTE]`.
3. **Falta de alternativas concretas.** Si habla de daños pero no ofrece ninguna estrategia de reducción, es `[IMPORTANTE]`.
4. **Desconocimiento del placer y la función.** Si trata todo consumo como patológico, es `[IMPORTANTE]`.
5. **Mensaje de "esfuerzo/voluntad".** Si el cambio se presenta como cuestión de fuerza de voluntad sin más, es `[IMPORTANTE]`.
6. **"Rachas" como métrica de valor.** Si una racha se presenta como éxito/fracaso binario (si consumes, todo mal), es `[IMPORTANTE]`. Las rachas deben enmarcarse como práctica, no como juicio.
7. **Resultados ASSIST en ALTO sin oferta de reducción.** Si el nivel ALTO solo recomienda tratamiento institucional sin mencionar reducción de daños como opción válida, es `[IMPORTANTE]`.
8. **Ausencia de información de seguridad básica.** Si una pantalla sobre una sustancia no menciona al menos una estrategia de reducción, es `[MEJORA]` o `[IMPORTANTE]`.

## Cómo reportar hallazgos de esta skill

- **Severidad**.
- **Ubicación**.
- **Evidencia**.
- **Riesgo** (qué ocurre si un usuario que no quiere (o no puede) dejar de consumir encuentra solo mensajes de abstinencia: se aleja de la app, abandona cualquier proceso).
- **Propuesta** (texto alternativo con alternativas concretas, abstinencia como opción entre varias, reconocimiento de placer/función).
- **Principio RD violado**.

## Frase de cierre para tus reportes cuando uses esta skill

> "Auditoría realizada bajo el paradigma de reducción de daños (HRI / Harm Reduction International). Se verificó que la abstinencia aparezca como una opción entre varias, nunca como la única."
