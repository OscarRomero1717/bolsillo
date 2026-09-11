# Gobernanza de IA

Herramienta: **Cursor**. Toda la auditoría de IA de esta prueba está en este archivo. Los chats no se versionan.

| Artefacto | Ruta |
| --- | --- |
| Skill | `.cursor/skills/generate-domain-test/SKILL.md` |
| Agente | `.cursor/agents/architecture-reviewer.md` |

---

## 1. Skills / prompts automatizados

**Nombre:** `generate-domain-test`  
**Archivo:** `.cursor/skills/generate-domain-test/SKILL.md`

Prompt estructurado para no reescribir a mano el mismo tipo de test cada vez que se añade una regla al abono.

| Campo | Contrato |
| --- | --- |
| Input | Aggregate y método (`Goal.contribute`), invariantes, eventos (`GoalCompletedEvent`), códigos de excepción |
| Output | Clase JUnit 5 + AssertJ; **un test por regla** (feliz, borde, evento) |
| Restricciones | Cero `org.springframework` y `jakarta.persistence`. Cero Mockito sobre el aggregate. No generar MockMvc ni `@SpringBootTest`. |

**Uso real:** `GoalContributeTest` — monto ≤ 0, meta `COMPLETED`, `current + amount > target`, cierre exacto al objetivo y emisión de `GoalCompletedEvent` + `GoalUpdatedEvent`.

El skill no sustituye revisión: si un test afirma la regla en el propio test y no llama a `contribute()`, se descarta.

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

### 3.3 Sesiones relevantes (resumen)

No está el scaffolding paso a paso.

| Fecha | Tema | IA | Humano | Decisión |
| --- | --- | --- | --- | --- |
| 2026-09-10 | Arquitectura | Opción C distribuida | Un módulo hexagonal | Rechazado C |
| 2026-09-10 | Initializr | `4.1.1.RELEASE` (no resolvía) | Boot **4.1.1** + `webmvc` | Modificado |
| 2026-09-10 | Dominio | `contribute()` + eventos | Cero Spring en `domain` | Aceptado |
| 2026-09-10 | JPA | Entidad + mapper | `Goal` ≠ `@Entity` | Aceptado |
| 2026-09-10 | Tiempo real | Hub + Angular `EventSource` | Diálogo solo por SSE | Modificado |
| 2026-09-10 | Bug 100% | HALF_UP en el DTO | `DOWN` + restante | Modificado |
| 2026-09-10 | Tests enunciado | MockMvc + `enunciado.spec.ts` | Verdes; no sustituyen dominio | Aceptado |
