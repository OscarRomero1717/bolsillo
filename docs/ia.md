# Gobernanza de IA

Herramienta: **Cursor**. Toda la auditoría de IA de esta prueba está en este archivo. Los chats no se versionan.

| Artefacto | Ruta |
| --- | --- |
| Skills | `.cursor/skills/<nombre>/SKILL.md` |
| Agente | `.cursor/agents/architecture-reviewer.md` |

Los skills de **este repo** (no los personales de `~/.cursor/skills/`) van versionados para que el evaluador los abra en el clon.

---

## 1. Skills / prompts automatizados

Hay **tres**, todos en `.cursor/skills/`. Un skill es un prompt reutilizable con contrato (input → output → prohibiciones). No es el chat de ayer.

| Skill | Archivo | Qué automatiza |
| --- | --- | --- |
| `generate-domain-test` | `.cursor/skills/generate-domain-test/SKILL.md` | Tests JUnit del invariante, sin Spring |
| `map-problem-detail` | `.cursor/skills/map-problem-detail/SKILL.md` | Excepción de dominio → RFC 7807 + MockMvc |
| `strict-goal-sse-client` | `.cursor/skills/strict-goal-sse-client/SKILL.md` | SSE tipado, diálogo por stream, cero `any` |

### `generate-domain-test`

| Campo | Contrato |
| --- | --- |
| Input | Aggregate (`Goal.contribute`), invariantes, eventos, códigos |
| Output | JUnit 5 + AssertJ; **un test por regla** |
| Restricciones | Cero Spring/JPA; cero Mockito sobre el aggregate |

**Uso real:** `GoalContributeTest`. Si el test reimplementa el `if` y no llama a `contribute()`, se descarta.

### `map-problem-detail`

| Campo | Contrato |
| --- | --- |
| Input | Excepción / regla (no encontrada, restante, `@Version`) |
| Output | `ApiExceptionHandler` + un MockMvc por status (400/404/409/422) |
| Restricciones | Sin recalcular el abono en el controller |

**Uso real:** abono 0 → 400; superar restante → 422; id inexistente → 404.

### `strict-goal-sse-client`

| Campo | Contrato |
| --- | --- |
| Input | Eventos `goal-updated` / `goal-completed` |
| Output | `GoalSse` + `GoalStore` (`completedGoal` solo desde el stream) |
| Restricciones | Cero `any`, cero diálogo en el POST, cero WebSocket |

**Uso real:** dos pestañas; el overlay sale con `goal-completed`.


---

## 2. Agents / sub-agentes

**Nombre:** `architecture-reviewer`  
**Archivo:** `.cursor/agents/architecture-reviewer.md`  
**Rol:** auditor de arquitectura hexagonal lean (no implementa features).

Se usa al cerrar un bloque (dominio, REST, SSE, Angular). Salida esperada: violaciones con archivo y qué mover.

**Rechaza**

- `org.springframework` o `jakarta.persistence` en `domain/`
- `@Entity` en `Goal` (el persistido es `GoalJpaEntity`)
- Lógica de abono, restante o 100% en controllers o DTOs
- `any` en TypeScript
- WebSocket, NgRx, Kafka, microservicios o JWT sin requisito nuevo
- Tomar el `CHECK` de SQL como dueño del invariante

**Acepta**

- Use cases que solo orquestan (load → contribute → save → publish)
- Adapters JPA y SSE fuera de `domain`
- Tests de dominio sin contenedor Spring

---

## 3. Bitácora de co-creación

### 3.1 Generado por IA vs código propio

| Componente | Origen | Qué se hizo a mano / se revisó |
| --- | --- | --- |
| `Goal.contribute()`, invariantes, `rehydrate` | Propio / línea a línea | Cada `if` y cada evento |
| Tests de dominio | Skill, luego edición | Una aserción por regla; sin Spring |
| `GoalJpaEntity`, mapper, `schema.sql`, DTOs, `application.yml` | IA | Confirmar que `Goal` no lleva `@Entity` |
| REST + `ApiExceptionHandler` | IA | Sin `if` de restante en el controller |
| `SseHub`, listener, `GoalStreamController` | IA | Publish **después** de `save`; el push no tumba el POST |
| `GoalStore`, `GoalSse`, forms, diálogo | Mixto | Tipos `GoalUpdated \| GoalCompleted`; cero `any` |
| Docs (`arquitectura.md`, README) | IA, recortado | Alineado al código, no al chat |

### 3.2 Criterio crítico y correcciones

Mínimo dos casos donde la IA se rechazó o se cambió. Razón técnica, no de estilo.

**1. Microservicios + broker (rechazado)**  
La IA encajó “evento de meta completada” con otro proceso y Kafka. Un abono muta **un** aggregate (`Goal`) y el aviso es el mismo bounded context. Extraer “notifications” añade red, contrato y fallo parcial sin ganancia. Quedó Observer in-process: `GoalEventPublisher` → `ApplicationEventPublisher` → `GoalEventSseListener` → SSE.

**2. WebSocket + diálogo disparado por el POST (modificado)**  
La IA propuso tubo bidireccional (WS/STOMP) y abrir el diálogo cuando el `POST .../contributions` devolvía `COMPLETED`. WS no aporta: el comando ya va por REST. Abrir el diálogo en el HTTP deja ciega la otra pestaña. Quedó REST para escribir y SSE (`goal-updated` / `goal-completed`) para avisar; `completedGoal` solo se setea desde el stream.

**3. Porcentaje 100% con meta `OPEN` (modificado)**  
Con objetivo `1_000_001` y acumulado `1_000_000`, el mapper redondeaba HALF_UP a 100% y el form exigía abono mínimo 1 → 422 `CONTRIBUTION_EXCEEDS_REMAINING`. Tipado laxo del % (mentir el estado). Se cambió a redondeo `DOWN`, tope 99 si `OPEN`, 100 solo si `COMPLETED`, y `max` del form = restante.

No se aceptó `@Entity` en `Goal` ni `any` en el cliente SSE (factory tipada de `EventSource`). Eso lo veta el agente; no se llegó a merge.

### 3.3 Entradas (AI_LOG)

No está cada scaffolding. Estas entradas muestran prompt acotado → output → validación humana → decisión.

**[2026-09-10] — Docs**  
**Contexto:** Inicio del repo de entrega (fuera de Facilities Nexus): esqueleto público y gobernanza de IA.  
**Prompt resumido:** Crear `apps/backend`, `apps/frontend`, `docs/`, `.gitignore`, README, `arquitectura.md` y `ia.md` con plantilla de bitácora; versionar un skill de tests de dominio y un agente reviewer. No volcar chats.  
**Output IA:** Árbol de carpetas, skill `generate-domain-test`, agente `architecture-reviewer`, este archivo.  
**Validación humana:** Skill y agente hablan de `Goal`/SSE, no de JWT. `cursor.md` y el playbook quedaron locales (`.gitignore`). Sin código de negocio aún.  
**Decisión:** Aceptado

**[2026-09-10] — Domain**  
**Contexto:** Invariantes del abono. Skill `generate-domain-test`.  
**Prompt resumido:** Implementar `Goal.contribute()` una regla por vez (monto > 0, no `COMPLETED`, no superar objetivo, evento al 100%). Tests JUnit + AssertJ, un test por regla, cero Spring/JPA.  
**Output IA:** `contribute()` muta `current`/`status` y devuelve `GoalUpdatedEvent` / `GoalCompletedEvent`; `GoalContributeTest`.  
**Validación humana:** `mvn test` verde. Cero `org.springframework` en `domain`. Las aserciones llaman al aggregate, no reimplementan el `if`.  
**Decisión:** Aceptado

**[2026-09-10] — Infrastructure**  
**Contexto:** Persistencia SQLite sin acoplar Hibernate al dominio.  
**Prompt resumido:** `GoalJpaEntity` en `infrastructure`, mapper, `Goal.rehydrate`. `Goal` no lleva `@Entity`. `schema.sql` con CHECK; `ddl-auto: none`.  
**Output IA:** Adapter JPA + seed Viaje a Cartagena / Fondo emergencia.  
**Validación humana:** Dominio sin `jakarta.persistence`. CHECK es red de seguridad, no dueño de la regla.  
**Decisión:** Aceptado

**[2026-09-10] — Api / Angular**  
**Contexto:** Aviso a otras pestañas al completar una meta.  
**Prompt resumido:** REST escribe; SSE (`GET /api/goals/stream`) avisa `goal-updated` y `goal-completed`. El diálogo no se abre con el JSON del POST. Cero `any` en el payload.  
**Output IA:** `SseHub`, listener, `GoalSse` + `EventSource`; propuesta de WebSocket y de abrir el overlay en el 200 del abono.  
**Validación humana:** Se rechazó WS (el comando ya es HTTP). El diálogo solo se setea con `goal-completed` del stream para que la segunda pestaña se entere.  
**Decisión:** Modificado

**[2026-09-10] — Api / Frontend**  
**Contexto:** Objetivo `1_000_001`, acumulado `1_000_000`, abono `1` → 422; la card pintaba 100% con status `OPEN`.  
**Prompt resumido:** Entender el 422 y el porcentaje; no mentir el estado en el DTO.  
**Output IA / código:** `progressPercent` con HALF_UP llegaba a 100% antes de `COMPLETED`; el form exigía mínimo 1.  
**Validación humana:** Redondeo `DOWN`, tope 99 si `OPEN`, 100 solo si `COMPLETED`; restante en la card; `max` del form = restante. Tests de dominio, MockMvc y UI verdes.  
**Decisión:** Modificado

