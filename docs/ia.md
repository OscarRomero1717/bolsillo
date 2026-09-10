# Gobernanza de IA — Bolsillo de Ahorro Programado

Registro del uso de herramientas de IA durante el desarrollo (enfoque AI-First).

Este archivo es el **único** entregable de auditoría de IA que pide la prueba (`docs/ia.md`). Aquí están el skill, el agente, los trade-offs, el criterio propio vs. IA y el AI_LOG. Los chats completos no se versionan.

Herramienta principal: **Cursor**. El skill y el agente versionados están en `.cursor/`. Notas internas de flujo de trabajo no se publican.

---

## 1. Skill / prompt reutilizable

No se guardan todos los prompts de cada sesión. Se versiona **un** skill con contrato estable.

| Campo | Valor |
|---|---|
| Nombre | `generate-domain-test` |
| Archivo | `.cursor/skills/generate-domain-test/SKILL.md` |
| Para qué | Automatizar tests de invariantes de un aggregate |
| Input | Aggregate, reglas, eventos, códigos de excepción |
| Output | JUnit 5 + AssertJ, sin Spring ni JPA |
| Qué evita | Copiar un prompt ad hoc distinto en cada regla de `contribute()` |

**Prompt resumido (el que se reutiliza):** generar tests de dominio dado un caso de uso, una regla por test, incluyendo el evento `GoalCompleted`.

---

## 2. Agente

| Campo | Valor |
|---|---|
| Nombre | `architecture-reviewer` |
| Archivo | `.cursor/agents/architecture-reviewer.md` |
| Rol | Auditar que las reglas no se filtren a Spring/Angular |
| Rechaza | Spring/JPA en `domain`, `any`, lógica de abono en controller, WS/NgRx/Kafka no pedidos |
| Cuándo | Tras cada bloque de capa (dominio, REST, SSE, UI) |

---

## 3. Trade-offs de gobernanza (por qué no un dump de prompts)

| Decisión | Qué se gana | Qué se pierde |
|---|---|---|
| Un skill, no 20 prompts sueltos | La prueba pide ≥1 skill demostrable; se puede abrir el archivo en la oral | Menos “histórico de ingeniería de prompt” |
| AI_LOG con prompt **resumido** | Cumple bitácora sin secretos ni ruido | No se reproduce el chat literal |
| Notas de Cursor/plan locales, no en Git | La entrega solo lleva `ia.md` + skill/agente | El flujo interno no es visible en el clon público |
| Código de dominio a mano; boilerplate con IA | Se puede defender `Goal.contribute()` como criterio propio | Más lento al inicio |

---

## 4. Código propio vs. generado (se actualiza al implementar)

| Pieza | Origen previsto | Nota |
|---|---|---|
| `Goal.contribute()` e invariantes | Propio / revisado línea a línea | No aceptar ifs de negocio en el controller |
| Tests de dominio | Skill `generate-domain-test`, luego revisión humana | Deben fallar si se mueve la regla |
| Mapper JPA, DTOs, YAML | IA, con revisión | Teatro si el dominio queda mal |
| Store SSE Angular | Mixto | Tipado estricto; cero `any` |
| `arquitectura.md` / este log | Mixto | Las 4 preguntas de arquitectura se defienden en oral |

---

## 5. Rechazos previstos (elegir 2–3 con diff real al cerrar)

Plantilla para la oral; se concreta cuando exista código:

1. Microservicios + broker → un solo bounded context, 10 h.
2. `any` en `EventSource` → rompe TypeScript strict.
3. Validación solo en el controller → el dominio quedaría bypasseable.
4. WebSocket + STOMP → el write ya es REST; SSE basta.
5. NgRx → un store de signals cubre listado + diálogo.
6. `@Entity` = `Goal` de dominio → acopla Hibernate al invariante.

---

## Plantilla de entrada

```markdown
## [YYYY-MM-DD HH:mm] — {Área: Domain|Application|Infrastructure|Api|Angular|DevOps|Docs}

**Contexto:** Qué problema se resolvía.
**Prompt resumido:** Intención de la solicitud a la IA (sin secretos ni credenciales).
**Output IA:** Qué generó o propuso la IA.
**Validación humana:** Qué se revisó, cambió o rechazó y por qué.
**Decisión:** Aceptado | Rechazado | Modificado
```

---

## Entradas

## [2026-09-10 10:48] — Docs

**Contexto:** Paso 0.2 del playbook — repo fuera de Facilities Nexus, docs de la prueba y gobernanza de IA sin volcar todos los prompts.
**Prompt resumido:** Crear estructura pública con `arquitectura.md`, `ia.md` tipo AI_LOG (skill, agente, trade-offs, plantilla, entradas), `cursor.md` solo como mapa del IDE, y un skill + agente versionados. Cumplir que la auditoría viva en `ia.md`.
**Output IA:** Árbol `apps/`, `docs/`, `.cursor/skills/generate-domain-test`, `.cursor/agents/architecture-reviewer`; README y `.gitignore`; esqueleto de arquitectura opción B; este log con una entrada.
**Validación humana:** Skill y agente son de este dominio (Goal/SSE), no Identity/JWT. Aún no hay código de aplicación. `cursor.md` y el plan de ejecución se quedaron locales (no van en el clon público).
**Decisión:** Aceptado

## [2026-09-10 10:58] — Docs

**Contexto:** Quitar del README (y de Git) el plan de ejecución y la guía de Cursor.
**Prompt resumido:** Manejar esos MD en local; no subirlos. La entrega pública de docs queda en arquitectura + ia.
**Output IA:** Enlaces quitados del README; ambos archivos en `.gitignore`; `ia.md` ya no apunta a `cursor.md`.
**Validación humana:** La prueba sigue cubierta: `docs/ia.md` + skill + agente sí se versionan.
**Decisión:** Aceptado
