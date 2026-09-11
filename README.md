# Bolsillo de Ahorro Programado

App de metas de ahorro: alta, abonos y aviso cuando una meta llega al 100%. Un solo usuario de demo. La regla `current + amount ≤ target` vive en el dominio; el cierre se notifica por SSE (diálogo, no `alert`).

Stack: Java 21 + Spring Boot 4, Angular 21 (`strict`), SQLite, REST + SSE. Sin Docker, sin JWT.

## Requisitos

| Pieza | Versión |
| --- | --- |
| JDK | 21 |
| Maven | 3.9+ (o `mvnw` en `apps/backend`) |
| Node | 20+ |
| npm | 10+ |

Dos terminales. El frontend espera el API en `http://localhost:8080`.

## Cómo correr

**Backend** (puerto `8080`):

```bash
cd apps/backend
mvn spring-boot:run
```

Listo cuando `GET http://localhost:8080/api/goals` devuelve las dos metas seed (Viaje a Cartagena y Fondo emergencia).

**Frontend** (puerto `4200`):

```bash
cd apps/frontend
npm install
npx ng serve
```

Abrir `http://localhost:4200/`. CORS del API admite ese origen.

## Tests

```bash
cd apps/backend
mvn test
```

```bash
cd apps/frontend
npx ng test --watch=false
```

Los tests del backend no tocan el SQLite de desarrollo: usan `bolsillo-test.db`.

## Persistencia

SQLite en archivo: `apps/backend/data/bolsillo.db` (se crea al arrancar; el `.db` no se versiona). La carpeta `data/` sí va en Git (vacía). Esquema y seed: `apps/backend/src/main/resources/schema.sql` y `data.sql`.

## Demo (8 pasos)

Con backend y frontend ya arriba.

1. Dashboard: se ven las dos metas seed.
2. Crear una meta con objetivo `1000000` (o usar **Viaje a Cartagena**, que empieza en 0).
3. Abonar `200000` → la card queda en 20%. En una segunda pestaña el mismo % (SSE).
4. Abonar `0` → error 400, no persiste.
5. Abonar `900000` con restante `800000` → 422, no se pasa de 100%.
6. Abonar `800000` → 100%, evento `goal-completed`, diálogo (no `alert`).
7. Otro abono sobre esa meta → 422 (ya `COMPLETED`).
8. Si hay tiempo: `SELECT * FROM goals;` sobre `apps/backend/data/bolsillo.db`.

## Documentación adicional

| Documento | Qué cubre |
| --- | --- |
| [Arquitectura](docs/arquitectura.md) | Cómo está armado el hexágono, SSE, SQLite y testing |
| [Gobernanza de IA](docs/ia.md) | Skill, agente reviewer, bitácora y rechazos |
| [README del backend](apps/backend/README.md) | Contrato HTTP, códigos de error, paquetes `domain` / `application` / `infrastructure` / `interfaces` |
| [README del frontend](apps/frontend/README.md) | Signals, REST vs SSE, árbol de `features/goals` |

El skill y el agente versionados están en `.cursor/skills/generate-domain-test/` y `.cursor/agents/architecture-reviewer.md`. El detalle de uso está en `docs/ia.md`.
