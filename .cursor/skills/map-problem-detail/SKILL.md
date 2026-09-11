---
name: map-problem-detail
description: Mapea excepciones de dominio de metas de ahorro a Problem Details RFC 7807 (400/422/404/409) sin lógica de abono en el controller. Usar al añadir un error HTTP, un code nuevo o al tocar ApiExceptionHandler.
---

# Map problem detail

Cuando el usuario pida un error de API para metas/abonos, mapear **solo** en `ApiExceptionHandler`. El controller no tiene `if` de restante ni de 100%.

## Códigos de este bounded context

| HTTP | Cuándo | Origen |
| --- | --- | --- |
| 400 | Bean Validation / UUID inválido | borde HTTP |
| 404 | Meta inexistente | `GoalNotFoundException` |
| 409 | Choque `@Version` | optimistic lock |
| 422 | Regla de `Goal.contribute()` | `ContributionNotAllowedException` (`code` del dominio) |

## Output

- `ProblemDetail` con `code` en properties
- Mensaje de negocio, **sin** stack trace en el body
- Un test MockMvc **por** status (no mezclar 400 y 422 en el mismo método)

## No hacer

- Recalcular `current + amount` en el handler
- Devolver 500 para abono 0 o restante insuficiente
- Usar `alert` o string libre en el JSON
