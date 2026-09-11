---
name: strict-goal-sse-client
description: Genera o corrige el cliente Angular del stream de metas (EventSource, tipos GoalUpdated/GoalCompleted, signals) sin any y sin abrir el diálogo 100% desde el POST. Usar al tocar GoalSse, GoalStore, SSE o el overlay de meta cumplida.
---

# Strict goal SSE client

Comandos por REST. Avisos por SSE. TypeScript `strict`.

## Contrato

- URL: `{apiUrl}/goals/stream`
- Eventos: `goal-updated`, `goal-completed`
- Unión: `GoalUpdated | GoalCompleted` (campo `type` + `goal`)
- Factory `EVENT_SOURCE_FACTORY` para tests (no `new EventSource` suelto en el spec)

## Store

- `goal-updated` → reemplaza la meta en `goals`
- `goal-completed` → reemplaza **y** setea `completedGoal` (el diálogo lee ese signal)
- `contribute()` HTTP **no** asigna `completedGoal`

## No hacer

- `any` en el payload del stream
- WebSocket / NgRx
- `alert()` al 100%
- Abrir el overlay con el JSON del POST (deja ciega la otra pestaña)
