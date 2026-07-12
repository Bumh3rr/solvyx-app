---
description: Protocolo de primeros auxilios psicológicos (PFA / ESCUCHAR-OMS) aplicado al bot Berto. Detecta crisis, escala a SOS y propone respuestas empáticas en español para jóvenes de 15-25 años.
---

# Skill: Contención en Crisis

Esta skill te entrega herramientas para auditar y proponer el manejo de crisis en Solvyx. Aplícala cuando el hallazgo involucre: ideación suicida, autolesión, pánico agudo, craving intenso con descontrol, o cualquier momento donde el usuario exprese que "no puede más".

## Cuándo cargar esta skill

- Cualquier mención en el código de palabras como: `suicidio`, `morir`, `hacerme daño`, `crisis`, `emergencia`, `socorro`, `no puedo más`, `no aguanto`, `me quiero morir`, `no vale la pena`.
- Pantallas que toquen `showSosDialog`, `BertoState.CRISIS`, `SOS_OVERLAY`, `GuiaEstoyEnCrisis`, `GuiaPánico`.
- Texto que en el árbol de decisiones derive a `alcohol_craving` por keyword matching (revisar `ChatViewModel.kt`).
- Cualquier lugar donde Berto diga "Lo que estás sintiendo..." o similar.

## Marco de referencia

Adoptas el protocolo **PFA-OMS (ESCUCHAR)** adaptado al contexto digital:

| Paso | Acción | Aplicación en Berto |
|---|---|---|
| **E**scuchar | Sin interrumpir, validar | "Estoy aquí contigo. Cuéntame más si quieres." |
| **S**aludar | Reconocer a la persona | "Te escucho. Lo que sientes importa." |
| **C**almar | Reducir activación | Invitar a respirar, anclar al presente. |
| **U**nir | Conectar con red de apoyo | "¿Quieres que avisemos a alguien de tu red?" |
| **H**acer necesidades concretas | Ofrecer opciones reales | "Tengo aquí ejercicios de respiración, o podemos hablar." |
| **A**yudar a informarse | Orientar sin diagnosticar | "Esto que describes tiene nombre y tiene salida." |
| **R**ecordar | Anclar al usuario como protagonista | "Tú decides el siguiente paso." |

## Plantillas de respuesta para Berto (en español)

Usa estas plantillas cuando el bot deba responder a crisis. Mantén primera persona del bot ("yo"), segunda persona respetuosa hacia el usuario ("tú"), y brevedad (≤ 400 caracteres por burbuja).

### Detección de ideación suicida o autolesión

```
Gracias por decirme esto. No tengo que resolverlo sola/solo contigo, pero sí
puedo acompañarte en este momento. En México, la Línea de la Vida está
disponible las 24 horas: 800 911 2000. También podemos abrir tu Red de
Apoyo aquí mismo, o hacer juntos un ejercicio de respiración. Tú decides
qué necesitas ahora.
```

(Adapta el género si implementas lenguaje más inclusivo; por ahora "sola/solo" sirve como transición neutra.)

### Pánico agudo (ataque de pánico en curso)

```
Respira conmigo. Inhala lento por la nariz en 4 tiempos... aguanta 4... y
suelta por la boca en 6. Vamos otra vez. No tienes que controlar nada más
ahora, solo esto. ¿Quieres que te guíe con un ejercicio paso a paso?
```

### Cravings intensos con descontrol inminente

```
El craving sube como una ola: tiene un pico y luego baja. Dura en promedio
entre 15 y 20 minutos. ¿Quieres que te acompañe a esperar la ola juntos?
Tengo ejercicios para respirar, caminar, o simplemente conversar mientras
pasa.
```

### "No puedo más" (sin suicidalidad explícita)

```
Te escucho. Cuando alguien dice "no puedo más" suele ser porque ha cargado
mucho y por mucho tiempo. No tengo que saber todo lo que pasa para estar
contigo. ¿Quieres contarme un poco más, o prefieres que vayamos directo a
algo que te ayude a sentirte un poco mejor ahora?
```

## Reglas de auditoría que aplicas

1. **Detección de crisis.** Verifica que el bot identifique riesgo más allá de las 4 palabras clave actuales (`suicidio`, `hacerme daño`, `quiero morir`, `no puedo más` en `ChatViewModel.kt`). Faltan al menos: `ya no quiero estar aquí`, `no vale la pena seguir`, `voy a hacerme`, `me lastimaré`, variantes con acentos y mayúsculas.
2. **Escalamiento explícito.** Cuando hay riesgo, Berto debe mencionar al menos una ruta concreta: SOS, Red de Apoyo, o Línea de la Vida 800 911 2000. Si solo dice "no estás solo" sin ofrecer acción, es hallazgo `[IMPORTANTE]`.
3. **No consejos peligrosos.** Nunca debe recomendar "no pienses en eso", "distráete", "bebe agua", "haz ejercicio" como primera respuesta a ideación suicida. Cualquiera de esos sin derivación es `[CRÍTICO]`.
4. **No prometer confidencialidad.** Si hay riesgo vital, el bot debe aclarar que es un asistente y que la ayuda profesional es prioritaria.
5. **Tono no alarmista.** Validar sin dramatizar. "Lo que sientes es importante" sí; "¡Esto es muy grave!" no.
6. **TTS en crisis.** Si Berto habla con voz (TTS) durante una crisis, debe haber un botón de mute visible y un canal escrito siempre disponible.
7. **Tiempo de respuesta.** Un delay > 5 segundos durante crisis es hallazgo `[IMPORTANTE]`. El delay actual de `ChatViewModel.kt:221` (1.2-2.4s) es aceptable; delays mayores no.
8. **Voz y silencio.** Si el usuario escribe "cállate", "no hables", el bot debe respetar y no seguir con TTS.

## Cómo reportar hallazgos de esta skill

Para cada hallazgo entrega:

- **Severidad** (CRÍTICO / IMPORTANTE / MEJORA).
- **Ubicación** (`archivo.kt:línea`).
- **Evidencia** (cita textual entre comillas).
- **Riesgo clínico** (qué puede pasarle al usuario).
- **Propuesta** (texto listo para pegar usando las plantillas de esta skill, o una variante justificada).
- **Referencia PFA** (qué paso del protocolo aplica).

## Frase de cierre para tus reportes cuando uses esta skill

> "Auditoría de contención en crisis realizada con base en PFA-OMS (protocolo ESCUCHAR) y directrices de la Línea de la Vida (México)."
