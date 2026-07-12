# Mejoras Offline — Solvyx

> Qué agregar para que Solvyx sea más útil sin depender de internet. **El alma de Solvyx es offline.** Estas features refuerzan esa identidad: si la app es mejor sin red, gana terreno contra cualquier competidor online.

---

## 0. Principio rector

Todo lo que sigue funciona **sin red**, sin servidor, sin actualizaciones. Si una feature no cumple esto, va a `mejoras-online.md`.

Reglas:
- **Cero dependencias externas** en runtime (no OpenAI, no Firebase, no Crashlytics).
- **Persistencia local** en Room o DataStore.
- **Rendimiento fluido** en gama baja (Android 7.0+).
- **Privacidad absoluta**: nada sale del teléfono.

---

## 1. Más ejercicios de regulación emocional

Hoy Solvyx tiene el ejercicio 5-4-3-2-1 (GuiaPanicoScreen) y la respiración 4-6. **Bien**, pero insuficiente. La evidencia muestra que un arsenal de 4-6 técnicas distintas funciona mejor que dominar una sola. Propuesta:

| Ejercicio | Para qué sirve | Duración | Referencia breve en UI |
|---|---|---|---|
| **Respiración 4-7-8** | Inducir calma profunda, conciliar sueño | 2-4 min | "Inhala 4, sostén 7, exhala 8" |
| **Body scan guiado** | Reconectar con el cuerpo, útil en disociación | 8-10 min | TTS paso a paso |
| **Grounding alternativo (sentidos inversos)** | Variante del 5-4-3-2-1 cuando ya se domina | 5 min | "Saborea 1, huele 2, escucha 3, toca 4, mira 5" |
| **Respiración cuadrada** | Ansiedad media, fácil de recordar | 3 min | "Inhala 4, sostén 4, exhala 4, sostén 4" |
| **Técnica del lugar seguro** | Trauma, disociación, antes de dormir | 10 min | "Cierra los ojos. Imagina un lugar donde estés seguro/a..." |
| **Activación conductual (5 cosas)** | Depresión, baja motivación | 5 min | "Haz estas 5 cosas pequeñas en los próximos 10 min" |

**Implementación:** nueva pantalla `ui/screens/ejercicios/` con grid de tarjetas, cada una con TTS opcional (reusar la infra de Berto).

**Por qué importa:** la app pasa de "una técnica" a "un kit". Eso baja la probabilidad de abandono.

---

## 2. Más guías de primeros auxilios

Hoy hay 5 guías. Faltan escenarios frecuentes para 15-25 años:

| Guía nueva | Cuándo la buscas |
|---|---|
| **Desregulación emocional / flashback** | Disociación, imágenes intrusivas, pánico sin causa clara. |
| **Intoxicación por alcohol (esperando que pase)** | Acompañamiento mientras se baja la borrachera (no es lo mismo que Consumí de Más, que es post-consumo). |
| **Día de craving extremo (cuando ya fallaste el plan)** | El usuario consumió y necesita volver a cuidarse sin culparse. |
| **Noches difíciles / insomnio** | Rutina nocturna, qué hacer a las 3am cuando no puedes dormir. |
| **Conflicto con familia por consumo** | Cómo hablar con padres/hermanos cuando hay tensión. |
| **Acoso o violencia sexual reciente** | Primeros auxilios psicológicos, líneas específicas (línea de violencia de género). |
| **Volver a casa después de una fiesta intensa** | Hidratación, comida, seguridad, no quedarse dormido en la calle. |
| **Después de una recaída (consumiste después de un periodo sin consumo)** | Lo más clínico posible: sin culpas, con plan. |

**Cada guía nueva debe incluir** Líneas de ayuda concretas (911, Línea de la Vida, SAPTEL) cuando aplique.

---

## 3. Bitácora extendida (campos opcionales)

Hoy la bitácora tiene: fecha, ánimo, nota (≤100), consumo (sí/no), sustancia. Propuesta de **campos opcionales** que el usuario puede activar o no:

- **Sueño** (horas dormidas + calidad 1-5).
- **Comida** (¿comiste hoy? sí/no/parcial).
- **Actividad física** (nada / poca / moderada / intensa).
- **Contexto social** (solo / con familia / con amigos / en fiesta).
- **Detonante principal** (eti­queta libre: estrés, tristeza, aburrimiento, presión social,celebración, etc.).
- **Nivel de ansiedad** (0-10) — eliminado en la versión actual; reintroducir como opcional y bien implementado.
- **Mini-nota privada** (cifrada opcionalmente) que solo el usuario ve.

**Por qué importa:** estos campos permiten correlaciones locales (Insights de Berto) sin necesidad de servidor.

---

## 4. Insights de Berto offline

Con los campos opcionales, Berto puede generar **insights locales** (todo en el teléfono, nada sube):

- "Esta semana dormiste menos y registraste más craving. No es causalidad, pero conviene mirar."
- "Tus registros de ánimo 'ansioso' suelen ir los viernes. ¿Algo relacionado?"
- "Llevas 5 días consecutivos registrando. Eso importa, incluso si la semana fue difícil."

**Reglas:**
- Insights son **observaciones**, no diagnósticos.
- Lenguaje no alarmista, no moralizador.
- Frecuencia: máximo 1 insight cada 3 días, salvo que el usuario pida más.
- Desactivables desde Mi Perfil.

---

## 5. Módulo de psicoeducación por sustancia

Hoy `InfoSustanciaScreen.kt` tiene 4 tabs con texto duro. Propuesta: convertirlo en un **módulo navegable** estilo "lecciones cortas":

| Lección | Alcohol | Vape | Cristal | Tabaco |
|---|---|---|---|---|
| ¿Qué le hace a tu cuerpo? | sí | sí | sí | sí |
| ¿Por qué engancha? | sí | sí | sí | sí |
| Cómo se nota el craving | sí | sí | sí | sí |
| Cómo reducir si quieres | sí | sí | sí | sí |
| Señales de que necesitas ayuda profesional | sí | sí | sí | sí |
| Mitos comunes | sí | sí | sí | sí |

Cada lección: 1 pantalla, 3-5 minutos de lectura, sin scroll infinito. Lenguaje validado, sin moralizar, sin infantilizar.

**Persistencia:** marcar "leída" por el usuario en DataStore. Badge "X de Y completadas" opcional.

---

## 6. Rutinas diaria y nocturna

Hoy no hay estructura de día. Propuesta: dos rutinas opcionales con pasos locales.

### Rutina matutina (5 min)
1. Una respiración.
2. Registrar ánimo (si quiere).
3. Un mini-objetivo del día (libre, "hoy quiero...").
4. Frase del día (curada localmente, sin moralizar).

### Rutina nocturna (10 min)
1. Una respiración o body scan corto.
2. Registrar día (ánimo, consumo, sueño).
3. Tres logros del día (pueden ser mínimos: "me levanté", "dormí 5h", "no consumí", "aguanté el craving").
4. Una frase de cierre ("Mañana será otro día. Lo importante es que sigues aquí").

**Sin notificaciones push obligatorias.** El usuario activa recordatorios locales en Mi Perfil.

---

## 7. Journaling con prompts (sin IA)

Hoy no hay journaling. Propuesta: lista rotativa de **prompts abiertos** guardados localmente. El usuario elige uno o escribe en blanco.

| Categoría | Prompts ejemplo |
|---|---|
| Gratitud | "Hoy lo mejor fue..." |
| Dificultad | "Algo que me costó hoy fue..." |
| Curiosidad | "Algo que aprendí hoy fue..." |
| Emociones | "La emoción que más sentí fue... y mi cuerpo la sintió como..." |
| Cravings | "Si tuve ganas de consumir hoy, pasaron por... Hice... y al final..." |
| Planes | "Mañana me gustaría..." |

**Implementación:** entrada libre en Room, sin NLP, sin "análisis". Lo importante es el acto de escribir, no la app.

---

## 8. Modo "sin red" explícito

Hoy la app asume conectividad. Propuesta:

- **Indicador visible** cuando el dispositivo está sin red: Berto cambia a `berto_sin_internet` (ya existe como asset).
- **Mensaje contextual** en Home: "Estás sin internet. Solvyx funciona completo. Solo Berto online y el directorio profesional requieren red."
- **Banner sutil** en pantallas que sí dependen de red (futuras): "Esto requiere conexión. ¿Quieres ver la versión local?"

**Por qué importa:** la promesa offline deja de ser invisible y se vuelve una **característica de marca**.

---

## 9. Resumen de prioridades offline

| # | Feature | Esfuerzo | Impacto clínico | Dependencias | Prioridad |
|---|---------|----------|-----------------|--------------|-----------|
| 1 | Más guías de primeros auxilios | Medio | Alto | Componentes existentes | **1 (corto plazo)** |
| 2 | Modo "sin red" explícito | Bajo | Medio (marca) | Asset Berto sin internet | **2 (rápido)** |
| 3 | Más ejercicios de regulación | Medio | Alto | TTS de Berto | **3** |
| 4 | Bitácora extendida opcional | Medio | Alto | Room + UI | 4 |
| 5 | Módulo psicoeducación por sustancia | Alto | Alto | InfoSustanciaScreen | 5 |
| 6 | Journaling con prompts | Bajo | Medio | Room | 6 |
| 7 | Insights de Berto offline | Alto | Alto | Bitácora extendida | 7 |
| 8 | Rutinas diaria/nocturna | Medio | Medio | WorkManager | 8 |

---

## 10. Cierre

Solvyx ya hizo la apuesta correcta: offline por defecto. **Las features offline son el producto**. Las features online son un complemento que nunca debe erosionar la privacidad ni el alma local.

Si tuviéramos que elegir un solo incremento offline para el próximo release, sería **Más guías de primeros auxilios** (#1) y **Modo sin red explícito** (#2). El resto se construye sobre esa base.
