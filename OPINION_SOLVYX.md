# OPINION_SOLVYX.md — Opinión honesta del proyecto

> Documento generado por el agente principal con insumos del subagente `psicologo-solvyx`.
> No es un reporte de auditoría (eso está en el subagente). Es **una opinión**: lo que veo bien, lo que veo mal, y si vale la pena seguir.

---

## 1. Visión general

Solvyx es una app Android pensada como acompañante para jóvenes de 15 a 25 años en contextos de consumo de sustancias, con un robot empático llamado Berto como punto de contacto, un ASSIST-OMS propio, una bitácora emocional, guías de primeros auxilios y un botón SOS. Está construida sobre MVVM + Hilt + Room, todo offline por defecto.

**Categoría correcta.** La oportunidad de mercado existe: no hay en español una app de acompañamiento a reducción de daños para esa población. La mayoría son o demasiado clínicas (fechadas), o demasiado gamificadas (no son seguras), o están llenas de moral religiosa. Solvyx tiene el espacio para ser la categoría.

---

## 2. Lo que está bien (hay que protegerlo)

1. **Privacidad por defecto y 100% offline.** Decisión arquitectónica valiente y correcta. La promesa "lo que le dices a Berto se queda en tu teléfono" es, en este nicho, una ventaja competitiva enorme. **No la rompas por meter telemetría, IA en la nube o auth con servidor sin repensarlo.**
2. **Berto como objeto transicional.** El robot funciona como un "espacio seguro" sin pretender ser terapeuta. Esa es la lección que apps tipo Woebot o Wysa aprendieron después de años. Solvyx llega con esa intuición ya.
3. **Contenido clínico de las guías está sorprendentemente bien para una beta.** La diferenciación entre pánico e infarto cardíaco, los "si decides consumir, hazlo más seguro" en la guía de craving, las señales de alerta por sustancia, las llamadas al 911 cuando hay riesgo vital — todo eso es material que apps comerciales publican todavía con errores graves. **Buen trabajo del equipo de contenido.**
4. **ASSIST-OMS bien implementado.** Es un instrumento validado, no un invento casero. Da credibilidad clínica a la app.
5. **Accesibilidad visual.** Nunito, paleta Teal, contraste, animaciones Lottie pensadas para reducir ansiedad. El diseño visual no chilla. Berto refleja estados emocionales. Esto baja la barrera de entrada para jóvenes que no quieren abrir una app que parezca "seria" o médica.

---

## 3. Lo que NO me convence (crítica directa)

1. **El bot NO es seguro en crisis. Punto.** La detección por 7 keywords (`ChatViewModel.kt:38-41`) más un `contains` que matchea "crisis" en "tengo una crisis de pareja" más derivar a `alcohol_craving` cuando alguien escribe "me quiero morir" sin consumir alcohol es **un riesgo clínico mayor**. Esto solo no se puede mandar a release. Antes de cualquier feature nueva, hay que arreglar esto.
2. **Asumir abstinencia como única meta válida.** El árbol de Alcohol (`AlcoholCravingTree.kt`) habla de "salvar tu sobriedad", "tu enemigo", "Tira el alcohol si lo tienes en la mano", "No lo pienses, solo hazlo". Eso es lenguaje de Alcohólicos Anónimos, no de reducción de daños. **Solvyx pierde a todos los usuarios que quieren reducir sin dejar, que son la mayoría.** Esta contradicción entre el contenido de las guías (excelente) y el árbol de Alcohol (pésimo) es el peor mensaje que la app manda.
3. **La racha como gamificación iatrogénica.** El día que consumes se rompe la racha; ese día probablemente NO lo registras. Esto pervierte el propósito del diario. Es el mismo error que cometieron apps de fitness hace 10 años; ya hay literatura al respecto. **Hay que cambiar el sistema de rachas antes de que se grabe en el comportamiento del usuario.**
4. **"Compartir progreso" filtra datos clínicos a WhatsApp.** Hay un `Intent.ACTION_SEND` que comparte "Racha actual: X días sin consumo" a cualquier app. Esto rompe la promesa de privacidad y expone a personas en contextos de estigma familiar, laboral o escolar. Es un bug con consecuencias legales además de clínicas.
5. **El directorio profesional tiene datos posiblemente ficticios.** Cuatro psicólogos con nombres, direcciones, teléfonos y costos exactos. Si son inventados y alguien llama y no son esos números, la confianza en la app se rompe para siempre. Si son reales, no hay forma de que esos profesionales hayan consentido en aparecer. Cualquiera de las dos opciones es grave.
6. **El onboarding se muestra en cada apertura** (`SplashViewModel.kt:36`, `val done = false` hardcodeado). Esto erosiona la percepción de app terminada y, en una emergencia, retrasa el acceso al SOS. Es un bug que probablemente nadie reportó porque cada tester lo salta como si fuera la primera vez.
7. **El bot asume heterosexualidad normativa y género binario.** "Solo o sola", "juntas", "lista". En una app para 15-25 años esto se nota y aleja a una parte del público objetivo. No es un tema de corrección política, es de diseño de producto.
8. **El copy de TTS está hardcodeado con voz femenina específica.** El pitch 1.15 y rate 0.85 hacen que Berto suene como una mujer adulta. Para chicos de 16 años, escuchar una voz femenina "cálida" diciendo "respira conmigo" puede ser leído como maternal, no como pares. La voz de un bot importa más de lo que parece.

---

## 4. Riesgos clínicos top (5 hallazgos del subagente)

| # | Severidad | Qué pasa | Dónde |
|---|-----------|----------|-------|
| 1 | CRÍTICO | Detección de crisis solo con 7 keywords; un adolescente que escribe "ya no aguanto más" pasa inadvertido. | `chatbot/ChatViewModel.kt:38-41` |
| 2 | CRÍTICO | Ante crisis, Berto carga el árbol de craving de alcohol y no menciona 911 ni Línea de la Vida. | `chatbot/ChatViewModel.kt:315-325` |
| 3 | CRÍTICO | Árbol de Alcohol es bélico, asume abstinencia y **no tiene reducción de daños** (los otros 3 árboles sí). | `decisiontree/trees/AlcoholCravingTree.kt` |
| 4 | CRÍTICO | Compartir progreso expone "días sin consumo" por WhatsApp a la red del usuario. | `avances/MisAvancesScreen.kt:628-636` |
| 5 | CRÍTICO | La racha se rompe con cualquier consumo registrado. Incentiva no registrar. | `home/InicioViewModel.kt:48-65` |

Hay **10 hallazgos adicionales IMPORTANTES** y **4 MEJORA** en el reporte del subagente. No los detallo aquí; están en el flujo de trabajo y se priorizan abajo.

---

## 5. Prioridades de continuidad

### Corto plazo (antes del próximo release)
- Reescribir la detección de crisis: ≥20 keywords, matching por palabra/frase, sin falsos positivos.
- Crear un árbol de crisis dedicado que mencione **siempre** 911, Línea de la Vida 800 911 2000 y SAPTEL 555 259 8121.
- Reescribir `AlcoholCravingTree.kt`: lenguaje no bélico, abstinencia como opción entre varias, ruta paralela "si vas a consumir, hazlo más seguro", añadir ambas líneas nacionales.
- Cambiar el sistema de rachas a "días registrando" (no "días sin consumo").
- Eliminar o redactar drásticamente el botón "Compartir" en Mis Avances.
- Arreglar el bug del onboarding persistente.

### Mediano plazo (próximos 3 sprints)
- Validar el directorio profesional (teléfono por teléfono, consentimiento por escrito de cada profesional).
- Reescribir las guías para incluir 911, Línea de la Vida y SAPTEL como llamadas a la acción explícitas en TODAS las pantallas de riesgo (ya están en algunas; faltan en InfoSustanciaScreen y GuiaPanicoScreen).
- Reducir el lenguaje moralizador en árboles de decisión (reemplazar "sobriedad", "recaída", "batalla").
- Repensar la voz TTS: ofrecer opción de voz masculina o neutra.
- Lenguaje inclusivo en todas las strings (no solo TTS).
- Diseñar la arquitectura para una IA en línea sin romper la promesa offline (ver `mejoras-online.md`).

### Largo plazo (post-MVP público)
- Convertir Solvyx en una plataforma de acompañamiento con cohorte anonimizada y estudios clínicos retrospectivos con universidades.
- Bilingual (inglés/español) para expandir a LATAM.

---

## 6. Recomendación final

**Continuar. Vale la pena. Pero no liberar tal cual está.**

Solvyx tiene el ADN correcto: privacidad, empatía, contenido clínico serio, mascot bien diseñada. El problema no es de visión, es de ejecución en las piezas críticas. Las primeras 5 prioridades del corto plazo son obligatorias antes de cualquier release público. Si se arreglan, **Solvyx puede convertirse en la app de referencia en español para acompañamiento a jóvenes en contextos de consumo**.

La apuesta online (chatbot IA con DeepSeek) está bien elegida — económica, capaz en español — pero **debe construirse como una capa opcional que se apaga cuando no hay red o el usuario lo desactiva**, no como reemplazo del bot local. El alma de Solvyx es offline y la promesa de privacidad es su activo más valioso. Cualquier feature online debe diseñarse para no erosionarla.

---

## 7. Nota metodológica

- **Qué se revisó:** contenido clínico y psicoeducativo de guías, ASSIST, árboles de decisión, Berto, bitácora, home, SOS, directorio, onboarding, recursos en strings.
- **Qué NO se revisó:** arquitectura MVVM/Hilt/Room, animaciones Lottie, paleta Teal, Nunito, decisiones de Material 3, dependencias, build, tests. Esos son temas para otro documento (de producto/técnico).
- **Subagente utilizado:** `psicologo-solvyx` con 4 skills (`contencion-crisis`, `psicoeducacion-adolescente`, `reduccion-danos`, `lineas-ayuda-mx`). El reporte completo está en `docs/auditoria-psicologica/` (no entregado en esta entrega; consultar al subagente con `@psicologo-solvyx audita <ruta>`).
- **Líneas de ayuda México usadas como referencia:** Línea de la Vida 800 911 2000 (24/7), SAPTEL 555 259 8121, CIJ, 911.
- **Audiencia:** equipo fundador Solvyx. Si se comparte fuera, mantener la sección 3 (crítica directa) con su contexto; sacada de contexto se lee como ataque.
