# Arquitectura — Bolsillo de Ahorro Programado

Documento de decisiones. Se completa durante el desarrollo; los títulos de la prueba quedan fijos.

## Contexto

Aplicación de un solo bounded context: metas de ahorro (`Goal`) y abonos. Un usuario de demo. Sin autenticación en el alcance.

## Arquitectura seleccionada

Modular monolith hexagonal lean (opción B).

Un aggregate `Goal`. El dominio no depende de Spring ni de JPA. Persistencia SQLite y SSE son adapters.

Diagrama: pendiente (fase de implementación).

## ¿Por qué elegí esta arquitectura?

Pendiente de redacción final. Eje: un invariante (`current + amount ≤ target`) y dos volatilidades (cómo se guarda, cómo se notifica).

## ¿Qué alternativas descarté?

- **A — Layered Spring clásico:** las reglas quedarían en un `@Service` acoplado al framework.
- **C — Microservicios / broker:** un solo flujo de abono no justifica un límite de proceso.

## ¿Qué trade-offs asumí?

- SQLite archivo vs Postgres (sin Docker; el puerto `GoalRepository` deja el swap).
- REST + SSE vs WebSocket (el cliente ya escribe por HTTP).
- Sin OAuth/JWT (no hay HU de identidad).
- SSE sin outbox (si falla el push, el abono ya persistió).

## ¿Cómo se aíslan las reglas de negocio del framework y la infraestructura?

Las reglas viven en `Goal.contribute()`. El paquete `domain` no importa Spring ni JPA. Los tests de dominio lo demuestran.

## Patrones

- Repository
- Domain Event / Observer
- Adapter

## Tiempo real

REST para comandos. SSE para `goal-updated` y `goal-completed`.

## Persistencia

SQLite. Esquema en `schema.sql` (incluye `CHECK`). JPA es adapter, no dueño del modelo.

## Testing

Dominio primero (sin Spring). Luego MockMvc y tests de componentes Angular.

## Seguridad (alcance de la prueba)

Bean Validation, excepciones Problem Details, CORS a localhost, sin stack traces en el body. Sin JWT.
