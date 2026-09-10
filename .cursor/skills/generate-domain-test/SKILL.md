---
name: generate-domain-test
description: Genera tests JUnit 5 de un aggregate de dominio (invariantes y eventos) sin Spring ni JPA.
---

# Generate domain test

Cuando el usuario pida tests de un caso de uso o aggregate de dominio, generar **solo** tests unitarios en el paquete `domain` (o `application` con fakes).

## Input que debes pedir o inferir

- Nombre del aggregate / método (ej. `Goal.contribute`)
- Invariantes (monto > 0, no superar objetivo, no abonar meta completada)
- Eventos esperados (ej. `GoalCompletedEvent` al 100%)
- Excepciones y códigos

## Output

- JUnit 5 + AssertJ
- Un test por regla (feliz, borde, evento)
- Cero imports `org.springframework` y `jakarta.persistence`
- Cero Mockito sobre el aggregate (se instancia de verdad)

## No hacer

- Tests MockMvc
- Poner reglas en el test en lugar de en el dominio
- Usar `@SpringBootTest`
