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

## [2026-09-10 11:10] — Infrastructure

**Contexto:** Paso 1.1 — generar Spring Boot (Web, Data JPA, Validation, sin H2) en `apps/backend`.
**Prompt resumido:** Scaffold desde start.spring.io, Java 21, paquete `com.bolsillo.ahorro`, sin H2.
**Output IA:** Initializr por defecto ofreció Boot `4.1.1.RELEASE` (no está en Maven Central) y solo línea 4.x (ya no genera Boot 3). El starter `web` en Boot 4 se llama `spring-boot-starter-webmvc`.
**Validación humana:** Se aceptó Boot **4.1.1** (sin sufijo RELEASE) porque 3.5.6 responde 400 en start.spring.io. Java 21 en el `pom` (JDK local 25; 21 es más portable). Sin H2. `mvn -q -DskipTests compile` verde. No se corrió `spring-boot:run` (sin driver SQL; eso es 1.2).
**Decisión:** Modificado

## [2026-09-10 11:14] — Infrastructure

**Contexto:** Paso 1.2 — SQLite en el `pom.xml`.
**Prompt resumido:** Añadir `sqlite-jdbc` y `hibernate-community-dialects` y que compile.
**Output IA:** Las dos dependencias sin versión explícita (el BOM de Boot las alinea).
**Decisión:** Aceptado

## [2026-09-10 11:21] — Infrastructure

**Contexto:** Paso 1.3 — configuración SQL (`application.yml`, `schema.sql`, tests).
**Prompt resumido:** YAML a SQLite, `ddl-auto: none`, scripts SQL, DB de test aparte.
**Output IA:** `application.properties` sustituido por YAML; `schema.sql` solo con comentarios.
**Decisión:** Modificado

## [2026-09-10 11:26] — Docs

**Contexto:** Paso 1.4 — paquetes Java vacíos del hexagonal.
**Prompt resumido:** Crear domain/application/infrastructure/interfaces sin clases de negocio.
**Output IA:** Un `package-info.java` por paquete (Git no versiona carpetas vacías).
**Decisión:** Aceptado

## [2026-09-10 11:28] — Infrastructure

**Contexto:** Paso 1.5 — CORS mínimo para Angular.
**Prompt resumido:** Permitir solo `http://localhost:4200`, GET/POST/OPTIONS, header Content-Type, rutas `/api/**`.
**Output IA:** `WebConfig` con `WebMvcConfigurer.addCorsMappings`.
**Decisión:** Aceptado

## [2026-09-10 11:45] — Angular

**Contexto:** Paso 1.6 — scaffold Angular standalone + strict.
**Prompt resumido:** `ng new` en `apps/frontend`, routing, CSS, sin SSR, carpetas feature-first, `environment.apiUrl`.
**Output IA:** Angular 21 (estilo de archivos 2016). `npm install` falló (`edgesOut`) porque el registry de usuario apunta a Artifactory corporativo y hay un bug de npm 10.9 con peers de Vitest.
**Decisión:** Modificado

## [2026-09-10 13:13] — Domain

**Contexto:** Paso 2.1 — value objects uno a uno.
**Prompt resumido:** Money, GoalId, GoalName, GoalStatus, Contribution; tests sin Spring.
**Output IA:** Records Java 21; Money con `BigDecimal` escala 2.
**Decisión:** Aceptado

## [2026-09-10 13:18] — Domain

**Contexto:** Paso 2.2 — excepciones y eventos, sin lógica de abono.
**Prompt resumido:** DomainException, ContributionNotAllowedException, DomainEvent, GoalUpdated, GoalCompleted.
**Output IA:** Jerarquía de excepciones con códigos; records de eventos.
**Validación humana:** Todavía no existe `Goal.contribute()`. Cero Spring en `domain`. `mvn compile` verde.
**Decisión:** Aceptado

