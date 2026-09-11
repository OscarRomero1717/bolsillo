# Backend — Bolsillo de Ahorro

API REST + SSE para metas de ahorro. Un solo aggregate (`Goal`). La regla `current + amount ≤ target` vive en el dominio, no en el controller.

Más detalle de diseño: [docs/arquitectura.md](../../docs/arquitectura.md).

## Requisitos

| Pieza | Versión |
| --- | --- |
| JDK | 21 |
| Maven | 3.9+ (o el wrapper `mvnw` / `mvnw.cmd` de esta carpeta) |
| Puerto | `8080` |

No hace falta Docker ni Postgres. SQLite se crea solo en `./data/bolsillo.db` (relativo a esta carpeta).

## Arranque

Desde `apps/backend`:

```bash
mvn spring-boot:run
```

Listo cuando responde `GET http://localhost:8080/api/goals` con las dos metas seed (Viaje a Cartagena y Fondo emergencia).

CORS de demo: origen `http://localhost:4200`.

## Tests

```bash
mvn test
```

Los tests usan `src/test/resources/application.yml` → archivo `bolsillo-test.db`, no el de desarrollo.

## Contrato HTTP

Base: `http://localhost:8080/api`

| Método | Ruta | Qué hace |
| --- | --- | --- |
| `GET` | `/goals` | Lista metas |
| `GET` | `/goals/{id}` | Una meta |
| `POST` | `/goals` | Crea meta `{ "name", "targetAmount" }` → **201** |
| `POST` | `/goals/{id}/contributions` | Abono `{ "amount" }` |
| `GET` | `/goals/stream` | SSE (`text/event-stream`): `goal-updated`, `goal-completed` |

Errores: Problem Details (RFC 7807).

| Código | Cuándo |
| --- | --- |
| 400 | Validación de borde (monto ≤ 0, nombre vacío) |
| 404 | Meta inexistente |
| 409 | Concurrencia (`@Version`) |
| 422 | Regla de negocio (resta menos que el abono, meta ya `COMPLETED`) |

El porcentaje de la respuesta nunca pinta 100% si la meta sigue `OPEN` (redondeo hacia abajo). 100 solo si `COMPLETED`.

## Persistencia

- Esquema: `src/main/resources/schema.sql` (`ddl-auto: none`)
- Seed: `src/main/resources/data.sql`
- Archivo runtime: `apps/backend/data/bolsillo.db` (gitignored)

`CHECK` en SQL es red de seguridad. El invariante se decide en `Goal.contribute()`.

## Estructura

```
apps/backend/
├── pom.xml
├── data/                          # SQLite en runtime (no versionar)
└── src/
    ├── main/java/com/bolsillo/ahorro/
    │   ├── domain/                # Sin Spring ni JPA
    │   │   ├── model/             # Goal, Money, GoalId…
    │   │   ├── port/              # GoalRepository, GoalEventPublisher
    │   │   ├── event/             # GoalUpdated / GoalCompleted
    │   │   └── exception/
    │   ├── application/           # Use cases (orquestan, no tienen la regla)
    │   ├── infrastructure/        # Adapters
    │   │   ├── persistence/       # JPA + mapper + @Version
    │   │   ├── realtime/          # SseHub + listener
    │   │   └── config/
    │   └── interfaces/
    │       ├── rest/              # Controller + DTOs + Problem Details
    │       └── realtime/          # GET /api/goals/stream
    ├── main/resources/
    │   ├── application.yml
    │   ├── schema.sql
    │   └── data.sql
    └── test/java/…                # Dominio sin Spring, use cases con fakes, MockMvc
```

Dónde mirar primero: `domain/model/Goal.java` → `application/DepositContributionUseCase.java` → `interfaces/rest/GoalController.java`.
