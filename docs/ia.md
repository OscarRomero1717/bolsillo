# Gobernanza de IA

La IA (Cursor) ayudó a escribir código y docs. **No decide arquitectura.** Cada bloque se revisa: se acepta, se cambia o se tira. Este archivo es la auditoría que pide la prueba. Los chats completos no se suben.

Herramienta: **Cursor**. Contratos versionados:

| Qué | Dónde | Para qué |
| --- | --- | --- |
| Skill | `.cursor/skills/generate-domain-test/SKILL.md` | Tests de dominio repetibles |
| Agente | `.cursor/agents/architecture-reviewer.md` | Que el hexágono no se rompa |
| Esta bitácora | `docs/ia.md` | Prompt resumido + decisión, no el log entero |

---

## Skill

Un **skill** es un prompt reutilizable con contrato: mismo input → mismo tipo de output. No es “el chat de ayer”.

`generate-domain-test`:

| | |
| --- | --- |
| **Input** | Aggregate (`Goal.contribute`), invariantes, eventos, códigos de excepción |
| **Output** | JUnit 5 + AssertJ, **una regla por test**, sin Spring ni JPA |
| **Qué evita** | Un prompt distinto cada vez que se añade una regla al abono |

Se usó para los tests de `GoalContributeTest` (monto ≤ 0, meta cerrada, superar objetivo, evento al 100%). El skill **no** genera MockMvc ni pone la regla en el test.

**Qué decir:** *Tengo un skill porque los invariantes se testean igual siempre. Si mañana hay otra regla, no improviso el prompt.*

---

## Agente

Un **agente** es un revisor con reglas fijas. No escribe el feature: **audita** el diff.

`architecture-reviewer` rechaza:

- Spring o JPA dentro de `domain/`
- `@Entity` en `Goal`
- Lógica de abono o del 100% en el controller
- `any` en TypeScript
- WebSocket, NgRx, Kafka, microservicios, JWT si nadie los pidió
- Usar el `CHECK` SQL como dueño de la regla

**Qué decir:** *El skill genera. El agente veta. Gobernanza es esa separación: producir con contrato y revisar con lista, no pegar lo que salga del chat.*

---

## Gobernanza (por qué no todo el log)

| Decisión | Por qué |
| --- | --- |
| Un skill, no 20 prompts | Se abre un archivo en la oral y se entiende |
| Bitácora **resumida** (estas entradas) | Cumple auditoría; no hay secretos ni 40 pasos de scaffolding |
| Chats fuera de Git | El clon público no es un dump de Cursor |
| Dominio revisado a mano | Puedo abrir `Goal.contribute()` y defender cada `if` |

Escalar carga o “subir todos los prompts” no demuestra criterio. Demuestra volumen.

---

## Código propio vs generado

| Pieza | Origen | Nota |
| --- | --- | --- |
| `Goal.contribute()` e invariantes | Propio / línea a línea | La IA no manda el `if` |
| Tests de dominio | Skill + revisión | Deben fallar si la regla se mueve al controller |
| Mapper JPA, DTOs, YAML, README | IA, revisado | Teatro si el dominio está mal |
| Store + SSE Angular | Mixto | Tipos explícitos; cero `any` |
| Diálogo 100% | Mixto, **corregido** | No se abre con el POST; se abre con `goal-completed` |
| `%` en API | Mixto, **corregido** | Nunca 100% si la meta sigue `OPEN` |

---

## Rechazos (los que cuento)

### 1. Microservicios + broker

La IA (y la opción C) encajan “evento” con Kafka y otro proceso. Un abono y un aviso no son dos sistemas. Quedó Observer local (`ApplicationEventPublisher` → `GoalEventSseListener` → SSE).

### 2. WebSocket / `any` en el stream

WebSocket es bidireccional; el cliente ya escribe por REST. SSE basta. En Angular no hay `any` en el payload: unión `GoalUpdated | GoalCompleted` y factory de `EventSource` para tests.

### 3. Regla en el controller o `@Entity` = dominio

Validar el restante solo en HTTP o poner `@Entity` en `Goal` acopla Spring/Hibernate al invariante. La regla está en `contribute()`. El `@Entity` es `GoalJpaEntity`; se reconstruye con `rehydrate`.

**Extra (bug, no estilo):** con objetivo `1_000_001` y acumulado `1_000_000` la UI pintaba 100% y el form exigía mínimo 1 → 422. Se corrigió: porcentaje hacia abajo (máx. 99 si `OPEN`) y el form usa el restante.

---

## Bitácora (solo lo que aporta)

No está cada `git add` ni cada paquete vacío. Están las decisiones que se defienden.

### 2026-09-10 — Arquitectura y gobernanza

**Contexto:** Tres alternativas; hay que versionar skill + agente + bitácora, no el chat.
**Prompt resumido:** Elegir arquitectura, crear skill de tests de dominio y agente reviewer.
**Output IA:** Hexagonal lean; esqueletos en `.cursor/` y este archivo.
**Validación:** Se aceptó B. Se rechazó C (microservicios). `cursor.md` y el playbook quedan locales.
**Decisión:** Aceptado

### 2026-09-10 — Spring Boot 4

**Contexto:** Scaffold del backend.
**Prompt resumido:** Java 21, Web + JPA + Validation, sin H2.
**Output IA:** Initializr en línea 4.x; `4.1.1.RELEASE` no resolvía en Maven Central.
**Validación:** Boot **4.1.1** (sin `RELEASE`), starter `webmvc`. No se forzó Boot 3 a mano contra el Initializr.
**Decisión:** Modificado

### 2026-09-10 — Dominio `contribute()`

**Contexto:** Invariantes del abono.
**Prompt resumido:** Una regla y un test cada vez; skill de dominio; sin Spring.
**Output IA:** `Goal.contribute()` + eventos.
**Validación:** Cero `org.springframework` en `domain`. Las reglas no están en el controller.
**Decisión:** Aceptado

### 2026-09-10 — JPA adapter

**Contexto:** Persistencia SQLite.
**Prompt resumido:** Entidad en infrastructure, no en `Goal`.
**Output IA:** `GoalJpaEntity` + mapper + `rehydrate`.
**Validación:** `Goal` sin `@Entity`. CHECK en SQL es red de seguridad.
**Decisión:** Aceptado

### 2026-09-10 — SSE, no WebSocket

**Contexto:** Aviso a otras pestañas.
**Prompt resumido:** REST escribe; stream avisa `goal-updated` / `goal-completed`.
**Output IA:** `SseHub`, listener, `GET /api/goals/stream`, `EventSource` en Angular.
**Validación:** El diálogo **no** se abre con el JSON del POST (eso dejaba ciega la otra pestaña). Se abre con el evento del stream.
**Decisión:** Modificado

### 2026-09-10 — 100% prematuro

**Contexto:** UI en 100% con meta `OPEN`; el último peso de 1 fallaba.
**Prompt resumido:** Entender el 422 y el porcentaje.
**Output IA / código:** HALF_UP pintaba 100% antes de completar.
**Validación:** `DOWN` y tope 99 si `OPEN`; restante visible; `max` del form = restante.
**Decisión:** Modificado

### 2026-09-10 — Tests del enunciado

**Contexto:** Contratos HTTP y los 4 de UI.
**Prompt resumido:** Un MockMvc por caso; dashboard, abono inválido sin POST, % de card, diálogo al `goal-completed`.
**Output IA:** `GoalControllerTest` + `enunciado.spec.ts`.
**Validación:** `mvn test` y `ng test --watch=false` verdes. Eso no sustituye los tests de dominio.
**Decisión:** Aceptado
