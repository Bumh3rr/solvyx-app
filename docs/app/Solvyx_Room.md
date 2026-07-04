# Solvyx — Base de Datos Local (Room)

> **Estado:** Borrador en evolución. Los atributos de las tablas pueden actualizarse, agregarse o eliminarse conforme avanza la implementación. Esta página refleja el último acuerdo, no un esquema cerrado.

---

## Contexto

En la Etapa Regional, la persistencia se divide en dos partes: **MySQL** (vía la API en Spring Boot 3) y **Room** (local, en Android). Esta página cubre solo Room.

Room almacena únicamente lo necesario para que la app funcione sin conexión a internet: SOS, Berto en modo árboles de decisión, y la sesión activa del usuario. Todo lo demás (bitácora, ASSIST detallado, metas, detonantes, logros, avances) vive en la API y no se replica localmente.

Room no tiene relaciones de llave foránea entre tablas. Cada dispositivo representa a un único usuario en sesión, por lo que tablas como `users` y `chat_session` son de fila única (el `id` siempre es 1), y no existe una columna `user_id` enlazando las demás tablas entre sí.

---

## Pendientes de nomenclatura

- [ ] `refresh_jwt` vs `refresh_token` — el diagrama actual usa `refresh_jwt`, el diccionario Word usa `refresh_token`. Definir cuál es el nombre final antes de implementar la entidad en Kotlin.
- [ ] `ultimo_assist` — se discutió renombrar esta tabla (alternativas: `assist_cache`, `contexto_riesgo`, `assist_snapshot`). Aún no se aplicó el cambio en el diagrama.

---

## Tablas

### `users`

Sesión activa del usuario en el dispositivo. Fila única (`id` siempre 1).

| Campo | Tipo | Notas |
| --- | --- | --- |
| `id` | INTEGER | PK, fijo en 1 |
| `server_id` | TEXT | ID asignado por la API. Nulo hasta el primer registro/login exitoso. |
| `apodo` | TEXT | |
| `email` | TEXT | Nulo si el usuario es anónimo |
| `es_anonimo` | BOOLEAN | Controla qué funciones están disponibles sin cuenta |
| `sustancias_json` | JSON | Array de sustancias seleccionadas: alcohol, vape, cristal, cigarro |

---

### `contactos_sos`

Contactos de confianza para el envío de SMS de emergencia. Máximo 3 filas.

| Campo | Tipo | Notas |
| --- | --- | --- |
| `id` | INTEGER | PK, autoincremental |
| `nombre` | VARCHAR(50) | |
| `telefono` | VARCHAR(10) | Formato nacional mexicano. La longitud es documental, no se impone a nivel de SQLite. |
| `orden` | INTEGER | Posición 0–2. El contacto en orden 0 es obligatorio para activar el SOS. |

---

### `chat_session`

Punto de avance del usuario dentro de los árboles de decisión de Berto. Fila única.

| Campo | Tipo | Notas |
| --- | --- | --- |
| `id` | INTEGER | PK, autoincremental |
| `tree_id` | TEXT | Árbol activo, ej. `alcohol_craving` |
| `nodo_actual_id` | TEXT | Nodo donde quedó la conversación |
| `timestamp` | INTEGER | epoch ms de la última interacción |

---

### `ultimo_assist`

Copia de solo lectura del resultado ASSIST más reciente, para que Berto tenga contexto de riesgo sin conexión. Fila única. Se sobrescribe por completo cada vez que la API confirma un nuevo resultado — no es origen de datos hacia la API, solo caché de lectura.

| Campo | Tipo | Notas |
| --- | --- | --- |
| `id` | INTEGER | PK, fijo en 1 |
| `sustancia_id` | TEXT | alcohol, vape, cristal, cigarro |
| `puntaje` | INTEGER | |
| `nivel` | TEXT | BAJO, MODERADO, ALTO |
| `fecha` | INTEGER | epoch ms |

---

## Resumen

| Tabla | Cardinalidad | Origen de los datos |
| --- | --- | --- |
| `users` | 0–1 fila | Respuesta de la API (login/registro) |
| `contactos_sos` | 0–3 filas | Ingreso directo del usuario |
| `chat_session` | 0–1 fila | Generado por la app |
| `ultimo_assist` | 0–1 fila | Respuesta de la API (resultado ASSIST) |
