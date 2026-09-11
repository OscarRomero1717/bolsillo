# Frontend — Bolsillo de Ahorro

Angular 21 standalone, TypeScript `strict: true`. Dashboard de metas, alta, abonos y diálogo al completar (SSE, no `alert`).

El API vive en [apps/backend](../backend/README.md). Diseño: [docs/arquitectura.md](../../docs/arquitectura.md).

## Requisitos

| Pieza | Versión |
| --- | --- |
| Node | 20+ |
| npm | 10+ (el del proyecto) |
| Backend | `http://localhost:8080` en marcha |
| Puerto UI | `4200` |

`environment.apiUrl` apunta a `http://localhost:8080/api`. Si el backend no está arriba, el dashboard no carga las metas seed.

## Arranque

Desde `apps/frontend`:

```bash
npm install
npx ng serve
```

Abrir `http://localhost:4200/`. Recarga al guardar.

## Tests

```bash
npx ng test --watch=false
```

Vitest + jsdom. Los cuatro del enunciado están en `src/app/features/goals/enunciado.spec.ts` (dashboard, abono inválido sin POST, % de la card, diálogo al `goal-completed`).

## Cómo se mueve el dato

1. **Comandos** (crear / abonar): `HttpClient` → REST. El POST **no** abre el diálogo de 100%.
2. **Avisos**: `EventSource` → `GET /api/goals/stream`. Eventos `goal-updated` y `goal-completed`.
3. **Estado**: `GoalStore` (signals). El SSE actualiza la lista; `goal-completed` además llena `completedGoal` y el overlay.

Así dos pestañas se enteran del mismo cierre. CORS del backend admite este origen (`http://localhost:4200`).

400/422 llegan como Problem Details; el interceptor las convierte en `ApiError` para el formulario.

## Estructura

```
apps/frontend/
├── package.json
├── tsconfig.json                  # strict: true (sin any)
└── src/
    ├── environments/
    │   └── environment.ts         # apiUrl
    └── app/
        ├── app.config.ts          # providers, interceptor
        ├── app.routes.ts          # '' → dashboard
        ├── core/
        │   └── problem-detail.interceptor.ts
        └── features/goals/
            ├── models/            # Goal, Problem Detail
            ├── data/
            │   ├── goal.api.ts    # Observables HTTP
            │   └── goal.sse.ts    # EventSource + factory para tests
            ├── state/
            │   └── goal.store.ts  # signals: goals, error, completedGoal
            ├── pages/
            │   └── goal-dashboard.component.ts
            ├── components/
            │   ├── goal-card.component.ts
            │   ├── create-goal-form.component.ts
            │   ├── contribution-form.component.ts
            │   └── goal-completed-dialog.component.ts
            └── enunciado.spec.ts
```

Dónde mirar primero: `goal.store.ts` → `goal.sse.ts` → `goal-dashboard.component.ts`.

## Qué no hay (a propósito)

Sin NgRx: un store con signals alcanza. Sin JWT. Sin e2e. La UI no recalcula el invariante; si el API responde 422, el form lo muestra y no manda de más.
