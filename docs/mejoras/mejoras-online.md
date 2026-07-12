# Mejoras Online — Solvyx

> Qué agregar cuando hay red. **Online nunca debe romper la promesa offline** de Solvyx. Cada feature de este documento se construye como una capa opcional, con fallback al modo local y desactivable por el usuario.

---

## 0. Principio rector (léelo antes de implementar nada)

Solvyx ya hizo una promesa de marca: "100% privado. Sin internet." Si el equipo decide meter features online, debe hacerlo **sin traicionar esa promesa**. Esto significa:

- **Opt-in por feature**, nunca activado por defecto.
- **Anonimización de PII** antes de cualquier llamada a servidor o LLM.
- **Fallback local siempre funcional.** Si no hay red, la app sigue funcionando completa (Berto local, ASSIST, bitácora, guías).
- **Transparencia visible.** El usuario debe ver, en cada momento, si está interactuando con un modelo local o con un servidor remoto.
- **Desactivable globalmente.** Un switch "Modo privado total" en Mi Perfil apaga TODO lo online.

Si en algún momento una feature online no se puede diseñar bajo este principio, **no se implementa**.

---

## 1. Chatbot IA con DeepSeek (la pieza central)

### 1.1 Por qué DeepSeek

- **Costo.** El modelo `deepseek-chat` está entre los más baratos del mercado por token (≈$0.14 / 1M tokens entrada, ≈$0.28 / 1M tokens salida a la fecha de este documento). Permite sostener 1,000 conversaciones largas/mes por menos de USD $5.
- **Español.** Rinde bien en español mexicano y neutro. Comparable a GPT-4o-mini en tareas de contención, y superior en instrucciones en español.
- **Razonamiento clínico general.** Aceptable para validación, psicoeducación,陪伴 emocional. **No sustituye atención profesional**; el prompt debe reforzar esto constantemente.
- **API simple, OpenAI-compatible.** Integración rápida desde Kotlin con OkHttp o Ktor.
- **Riesgo.** DeepSeek es un proveedor con políticas menos robustas que OpenAI o Anthropic para temas sensibles. Hay que:
  - Cifrar en tránsito (HTTPS, ya de fábrica).
  - **No enviar PII** (nombre, email, teléfono, dirección).
  - **No enviar datos de bitácora** (consumos, ánimos, fechas) salvo agregado anónimo opt-in.

### 1.2 Arquitectura propuesta

```
[Usuario] → [Berto UI]
              ↓ (mensaje)
        [Capa de seguridad local] ← aquí corre la skill contencion-crisis
              ↓ (mensaje saneado, sin PII)
        [Cliente DeepSeek] (sólo si: red OK + usuario opt-in + modo no-crisis)
              ↓
        [DeepSeek API] → respuesta → [Post-procesado] (recorte, longitud, líneas de ayuda si detecta crisis)
              ↓
        [Berto UI]
```

### 1.3 Capas obligatorias

| Capa | Función | Tecnología sugerida |
|---|---|---|
| **Detector local de crisis** | Antes de cualquier llamada a DeepSeek, ejecutar las keywords ampliadas del hallazgo #1 (≥20 frases). Si hay crisis, NO se llama a DeepSeek; se enruta al árbol de crisis local con líneas de ayuda. | Regex local en Kotlin. |
| **Anonimizador de PII** | Reemplazar nombres propios, números telefónicos, direcciones, fechas exactas por placeholders (`[NOMBRE]`, `[TEL]`) antes de enviar. Regex local + lista de palabras del usuario. | Kotlin + `data class PiiPattern`. |
| **Prompt del sistema con guardarraíles** | Prompt fijo que carga Berto: persona, límites, lenguaje de reducción de daños, líneas de ayuda obligatorias, "no diagnosticar, no prescribir, no sustituir profesionales". | String en código, versionado. |
| **Post-procesado** | Validar longitud (≤ 400 caracteres por burbuja), verificar que la respuesta menciona líneas de ayuda si el input tuvo marcadores de riesgo, fallback a respuesta local si DeepSeek tarda >5s. | Kotlin. |
| **Rate limiting y costo** | Límite de N conversaciones por usuario por día. Mostrar al usuario cuántas lleva. | DataStore. |
| **Logging opt-in** | Guardar localmente las conversaciones para que el usuario las pueda revisar y borrar. Nunca se suben a servidor. | Room. |

### 1.4 Prompt del sistema (esqueleto)

```
Eres Berto, un asistente empático dentro de Solvyx, una app mexicana para jóvenes
de 15 a 25 años. Tu rol es acompañar, validar y psicoeducar. NO diagnosticas,
NO prescribes, NO sustituyes a un profesional. Si el usuario menciona suicidalidad,
autolesión, abuso o riesgo vital, tu primera acción es sugerir líneas de ayuda
(911, Línea de la Vida 800 911 2000, SAPTEL 555 259 8121) y ofrecer activar la
Red de Apoyo interna. Usa reducción de daños: la abstinencia no es la única meta
válida. Tono: cálido, directo, sin moralizar, sin infantilizar. Segunda persona
("tú"). Español neutro. Respuestas de máximo 400 caracteres por burbuja.
```

### 1.5 Modo degradado

- Sin red: Berto local (árbol de decisiones) sigue 100% funcional.
- DeepSeek caído o timeout >5s: respuesta local con frases prediseñadas.
- Usuario desactiva "Modo IA online": vuelve al modo local sin más cambios.

### 1.6 Costo estimado y plan B

| Concepto | Estimación |
|---|---|
| 1,000 conversaciones/mes, 4 turnos × 500 tokens | ≈ $1.40 USD |
| Plan de fallback | OpenAI GPT-4o-mini si DeepSeek cae. Mismo cliente, cambio de endpoint. |
| Proveedor alternativo para datos sensibles | Auto-alojado con Llama 3.1 8B o Mistral 7B en servidor propio. Mayor costo operativo, control total. |

---

## 2. Sincronización opcional cifrada (opt-in)

**Problema.** Hoy Solvyx es 100% local. Si el usuario cambia de teléfono o reinstala, pierde bitácora, ASSIST, Red de Apoyo, logros. En crisis, eso es perder semanas de progreso emocional.

**Propuesta.** Sincronización opcional con cifrado cliente→servidor (E2EE con clave derivada de la contraseña del usuario). El servidor solo ve blobs cifrados; ni Solvyx ni el proveedor puede leerlos.

**Implementación:**
- Cifrado: AES-256-GCM con clave derivada de PBKDF2 (contraseña del usuario) o clave aleatoria guardada en Keystore de Android.
- Backend: objeto storage barato (R2, S3, GCS) o servicio tipo Firebase con E2EE.
- UI: switch "Sincronizar entre dispositivos" en Mi Perfil. Si el usuario olvida la contraseña, los datos son irrecuperables (advertencia explícita).

**Riesgo:** si la implementación de cifrado falla, hay una filtración masiva de datos clínicos. **Solo implementar con auditoría de seguridad externa.**

---

## 3. Directorio profesional actualizable

**Problema.** El directorio actual tiene datos mock posiblemente ficticios. En release público esto es grave (ver hallazgo #12 del subagente).

**Propuesta online.**
- Cada entrada del directorio se sincroniza desde una fuente curada (un CMS simple tipo Strapi, o un Google Sheet moderado por el equipo).
- Las entradas tienen fecha de verificación visible ("Verificado por Solvyx el 2026-07-15").
- Si una entrada tiene más de 6 meses sin verificar, la app la marca como "Pendiente de verificación".
- Los usuarios pueden reportar información incorrecta desde la UI; eso llega al CMS.

**Privacidad.** El reporte del usuario puede ser anónimo o llevar email (opt-in). No se sube ningún dato del usuario que consulta el directorio.

---

## 4. Notificaciones inteligentes de Bitácora

**Problema.** Hoy la app no tiene notificaciones. El usuario puede olvidarse de registrar.

**Propuesta.**
- Notificaciones locales (sin servidor): "Es buen momento para registrar tu día. ¿Cómo te sientes?" a la hora que el usuario elija.
- Si el usuario lleva 3+ días sin registrar, una sola notificación más suave: "Aquí sigo cuando quieras".
- Nunca notificaciones alarmistas ("Llevas 3 días sin registrarte, ¿estás bien?").

**Sin internet.** Funcionan offline con WorkManager.

**Si en el futuro se quiere online:** notificaciones push con consentimiento explícito y granularidad por tipo ("solo recordatorios", "solo emergencias", "nada").

---

## 5. Telemetría anónima agregada

**Problema.** El equipo necesita saber si Solvyx está ayudando, pero no puede romper la promesa de privacidad.

**Propuesta.**
- Telemetría **agregada y anónima** (k-anonimato ≥10) enviada opt-in: número de usuarios activos por semana, distribución de niveles ASSIST, distribución de sustancias, frecuencia de uso de guías.
- **Nunca** datos individuales: nada de bitácora, ASSIST específico, ni mensajes a Berto.
- Publicar reportes públicos cada 6 meses. Esto construye reputación y permite estudios retrospectivos.

**Riesgo.** Cualquier intento de cruzar datos para "mejorar el producto" puede degenerar en vigilancia. Política clara en Mi Perfil → Privacidad: "Solvyx nunca vende datos".

---

## 6. Recursos psicoeducativos actualizables

**Problema.** Las guías están hardcoded en Kotlin. Actualizar contenido requiere un release.

**Propuesta.**
- Paquete de guías descargable, en formato Markdown + JSON, firmado por Solvyx (firma digital verificable en cliente).
- El usuario descarga una vez y queda offline.
- Actualizaciones verificadas cada 3 meses.
- Si una versión firmada está desactualizada, el usuario ve una nota: "Esta guía tiene más de 6 meses. Descarga la versión actualizada".

**Privacidad.** La descarga no expone qué usuario descargó qué guía.

---

## 7. Resumen de prioridades online

| # | Feature | Esfuerzo | Impacto clínico | Riesgo de privacidad | Prioridad |
|---|---------|----------|-----------------|----------------------|-----------|
| 1 | Chatbot IA DeepSeek con capas de seguridad | Alto | Alto | Alto (mitigable) | **1 (lanzamiento)** |
| 2 | Notificaciones locales de Bitácora | Bajo | Medio | Nulo | **2 (rápido)** |
| 3 | Directorio profesional con verificación visible | Medio | Alto | Bajo | **3 (antes de release)** |
| 4 | Recursos psicoeducáticos actualizables | Medio | Medio | Bajo | 4 |
| 5 | Sincronización E2EE | Alto | Medio | Muy alto | 5 (requiere auditoría) |
| 6 | Telemetría anónima agregada | Medio | Medio (estratégico) | Bajo | 6 |

**Regla final:** nada online se lanza sin antes pasar la auditoría de seguridad externa y sin haber publicado en Mi Perfil exactamente qué se envía y qué no.
