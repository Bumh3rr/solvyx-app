# Seed v1 — Solvyx

Seed inicial del contenido clínico y psicoeducativo offline de Solvyx.

## Archivos

| Archivo | Items | Descripción |
|---|---|---|
| `ejercicios.json` | 6 | Ejercicios de regulación emocional (respiración, body scan, grounding, lugar seguro, activación). |
| `guias_extendidas.json` | 8 | Guías de primeros auxilios para crisis, craving, noches difíciles, conflicto familiar, violencia sexual reciente, post-consumo y recaída. |
| `lecciones.json` | 24 | Psicoeducación por sustancia y tema (alcohol, vape, cristal, tabaco × efectos, enganche, craving, reducir, señales de ayuda, mitos). |
| `prompts_journaling.json` | 36 | Prompts de escritura reflexiva en 6 categorías (gratitud, dificultad, curiosidad, emociones, cravings, planes). |
| `rutinas.json` | 2 | Rutinas matutina y nocturna. |
| `rutina_pasos.json` | 8 | Pasos de las rutinas (4 por cada una). |

Total estimado: **~74 KB**.

## Estado de validación clínica

Todos los archivos están marcados con `_reviewed_by: "pendiente de revisión por psicologo-solvyx"`.

Antes de mergear, el subagente `psicologo-solvyx` debe auditar el copy siguiendo la skill `clinical-content-validation` (procedimiento completo en `.opencode/skill/clinical-content-validation/SKILL.md`).

### Pre-validación local aplicada

- Sin lenguaje patologizante ("adicto", "recaída", "limpio", "sucio", "batalla contra").
- Sin diminutivos ("poquito", "ratito", "problemita", "consentido", "pobrecito", "tesoro").
- Sin mayúsculas enfáticas.
- Sin asumir abstinencia como única meta válida; reducción como opción legítima.
- Líneas de ayuda (Línea de la Vida 800 911 2000, SAPTEL 55 5259 8121, 911, UNICEF, DIF) presentes en todas las guías de crisis.
- Segunda persona "tú" consistente.
- Inclusividad de género (formas neutras: "persona que consume", "si eres menor", "preparadx").
- Datos concretos (rangos, duraciones, cantidades) en las lecciones.
- Límite 200 chars respetado en pasos de guías.
- Límite 100 chars respetado en títulos de sección de lecciones.

## Convenciones

- Cada archivo lleva metadata: `_format`, `_schema_version`, `_seed_version`, `_created_at`, `_reviewed_by`.
- UTF-8 sin caracteres escapados innecesarios.
- Slugs en kebab-case, archivos en snake_case.
- `_seed_version` se bumpeará solo si cambia estructura o se agregan items; correcciones in-place se documentan aquí.

## Próximos pasos

1. Invocar `@psicologo-solvyx` para auditoría clínica formal.
2. Aplicar hallazgos `[CRÍTICO]` y `[IMPORTANTE]`.
3. Actualizar `_reviewed_by` con sello de fecha y versión.
4. Si se modifica ≥ 30% del copy, re-validar.
5. Coordinar con `backend-data-architect` y `backend-viewmodel-repository` para que el seeder (`AssetsSeeder.kt`) mapee estos JSON a las entidades Room.