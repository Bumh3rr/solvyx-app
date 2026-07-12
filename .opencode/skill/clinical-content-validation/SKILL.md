---
description: Procedimiento para que backend-content-curator invoque al subagente psicologo-solvyx y aplique los hallazgos antes de mergear copy clínico.
---

# Skill: Clinical Content Validation

Esta skill te entrega el procedimiento exacto para que cualquier agente que escriba copy clínico en Solvyx (especialmente `backend-content-curator`) lo someta a validación clínica antes de mergear. **Esta skill no es opcional.**

## Por qué existe

Solvyx es una app de salud. Copy clínico mal redactado puede:
- Patologizar a un usuario en consumo activo.
- Dar indicaciones incorrectas en crisis.
- Asumir abstinencia como única meta válida.
- Provocar daño en usuarios menores de edad.

Por eso **TODO copy clínico pasa por `psicologo-solvyx` antes de mergear.**

## Procedimiento obligatorio

### Paso 1: Identifica si el copy es "clínico"

Usa estos criterios. Si **cualquiera** aplica, el copy es clínico y debe validarse:

- Guía de primeros auxilios.
- Ejercicio de regulación emocional (incluye instrucciones).
- Lección por sustancia.
- Texto de rutina matutina/nocturna con contenido emocional.
- Insight de Berto.
- Notificación local con copy emocional ("aquí sigo cuando quieras").
- Prompt de journaling sobre temas sensibles (consumo, autolesión, suicidalidad).
- Cualquier copy que use las palabras: "crisis", "craving", "abstinencia", "sobredosis", "ataque", "pánico", "ansiedad", "depresión", "trauma", "suicidio".

### Paso 2: Pre-validación local

Antes de invocar al subagente, revisa tú mismo:

1. **Lenguaje patologizante:** "adicto", "recaída", "limpio", "sucio", "batalla contra". Reemplazar por: "persona que consume", "consumió de nuevo", "sin consumo actualmente", "proceso", "cuidado".
2. **Moralización:** "no deberías", "está mal", "es malo", "con sabiduría".
3. **Infantilización:** diminutivos, "tesoro", "consentido", "pobrecito".
4. **Asunción de abstinencia:** "salvar tu sobriedad", "decidiste no beber", "tu sobriedad".
5. **Género binario forzado:** "solo o sola", "presiona", "juntas", "listo".
6. **Falsa dicotomía:** "o dejas o sigues igual".
7. **Promesas no terapéuticas:** "te lo prometo", "te garantizo".
8. **Ausencia de líneas de ayuda:** si el copy trata crisis y no menciona 911 / Línea de la Vida 800 911 2000 / SAPTEL 555 259 8121, agrégale una referencia antes de validar.

Si detectas alguno, corrígelo antes de pasar al subagente. **Eso acelera la validación.**

### Paso 3: Invoca a `psicologo-solvyx`

Usa este formato (es parte de la skill del subagente):

```
@psicologo-solvyx revisa el archivo [ruta] y dime si hay riesgos clínicos,
lenguaje patologizante, falta de líneas de ayuda en crisis, o tono inapropiado
para 15-25 años. Reporte priorizado por severidad.
```

Si el copy está dividido en varios archivos:

```
@psicologo-solvyx audita el copy de los 6 ejercicios nuevos en
assets/seed/v2/ejercicios.json y produce reporte priorizado.
```

### Paso 4: Aplica los hallazgos

| Severidad | Acción |
|---|---|
| `[CRÍTICO]` | **Bloqueante.** Aplica antes de mergear. Sin excepciones. |
| `[IMPORTANTE]` | **Bloqueante** salvo justificación documentada. |
| `[MEJORA]` | Aplica si el cambio no rompe el flujo; documenta si decides no aplicar. |

### Paso 5: Re-valida si hubo cambios grandes

Si modificas más del 30% del copy como resultado de la auditoría, vuelve a invocar al subagente:

```
@psicologo-solvyx los cambios aplicados a assets/seed/v2/ejercicios.json fueron
grandes. ¿Puedes confirmar que ya no hay hallazgos CRÍTICOS ni IMPORTANTES?
```

### Paso 6: Documenta en el JSON

Actualiza `_reviewed_by` con la versión de la auditoría:

```json
"_reviewed_by": "psicologo-solvyx 2026-07-15 v1"
```

Si hubo cambios posteriores:

```json
"_reviewed_by": "psicologo-solvyx 2026-07-15 v1 (post-corrección)"
```

## Qué **NO** debe hacer el subagente

`psicologo-solvyx` es solo lectura. NO le pidas que:
- Modifique archivos.
- Escriba copy alternativo completo (solo propone fragmentos).
- Sugiera cambios de arquitectura.

Si necesitas una reescritura completa del copy, **tú la haces** siguiendo los hallazgos del subagente.

## Plantilla para invocar

```
@psicologo-solvyx revisa:

Ruta: [ruta del archivo]
Contexto: [breve descripción del feature]
Audiencia: jóvenes mexicanos 15-25 años
Marco: ASSIST-OMS + reducción de daños + PFA

Produce reporte priorizado por severidad con:
- Cita textual entre comillas
- Ruta y línea
- Riesgo clínico
- Propuesta concreta de reescritura (≤ 200 chars si es botón, ≤ 500 si es mensaje)
- Skills consultadas (contencion-crisis / psicoeducacion-adolescente / reduccion-danos / lineas-ayuda-mx)
```

## Frecuencia

| Situación | Frecuencia |
|---|---|
| Nuevo copy clínico | **Cada vez**, sin excepción. |
| Copy existente modificado ≥ 30% | Re-validar. |
| Copy existente modificado < 30% | Re-validar solo si afecta tono, no si es typo. |
| Bugfix técnico (referencia rota, link) | No requiere re-validación clínica. |
| Cambio de versión de seed | Re-validar los items nuevos o modificados. |

## Plantilla de log de auditoría

Si quieres dejar rastro en el repo, crea `docs/auditorias-contenido/YYYY-MM-DD.md`:

```markdown
# Auditoría de contenido — 2026-07-15

## Cambios auditados
- `assets/seed/v2/ejercicios.json` — nuevo (6 ejercicios).
- `assets/seed/v2/guias.json` — actualizado (8 guías).

## Resultado
- 0 hallazgos CRÍTICOS
- 2 hallazgos IMPORTANTES aplicados
- 5 hallazgos MEJORA, 4 aplicados

## Cambios aplicados
1. Reemplazo "adicto" → "persona con consumo problemático" en 3 lugares.
2. Adición de línea "Línea de la Vida 800 911 2000" en guía de craving.

## Pendientes
- [ ] Revisar copy de notificaciones locales (próxima iteración).
```

## Anti-patrones prohibidos

1. **Saltarse la validación** por urgencia.
2. **Aplicar solo hallazgos CRÍTICOS y dejar IMPORTANTES** sin justificación.
3. **Marcar como "revisado por psicologo-solvyx" sin haberlo invocado.**
4. **Modificar copy después de la validación sin re-validar.**
5. **Asumir que "es copy de marketing, no clínico"**. Si toca usuarios en momentos vulnerables, es clínico.