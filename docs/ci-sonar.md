# CI y SonarCloud

No hay CD: el evaluador clona y arranca en local. Lo que sí hay es **CI** (tests en cada push) y un **esqueleto de análisis estático** del backend.

## GitHub Actions

Archivo: `.github/workflows/ci.yml`. Se dispara en `push` y `pull_request` a `main`.

| Job | Qué corre |
| --- | --- |
| `backend` | JDK 21 + `mvn -B test` |
| `frontend` | Node 20 + `npm install` + `ng test --watch=false` |
| `sonar` | Tras `backend`: análisis SonarCloud si hay token |

`npm install` (no `npm ci`) porque el lock se genera en Windows y Linux pedía paquetes `@emnapi` que no estaban. Los tests de Maven no tocan `bolsillo.db` de desarrollo.

No se publica a un servidor. Un Action rojo significa tests rotos, no un deploy fallido.

## SonarCloud

Solo Java (`apps/backend`). No hay scanner de Angular. El quality gate **no espera**: un smell no pone rojo el CI.

Claves en `pom.xml`: `sonar.organization`, `sonar.projectKey`, `sonar.host.url` (`https://sonarcloud.io`). No son secretos; son el nombre del proyecto.

**Activar**

1. [sonarcloud.io](https://sonarcloud.io) → importar `OscarRomero1717/bolsillo`.
2. Alinear organization y project key con el `pom.xml`.
3. Token: avatar → **My Account** → **Access Tokens** → Personal Token.
4. GitHub → **Settings** → **Secrets** → `SONAR_TOKEN`. El valor no se commitea.

Sin secret, el paso de Sonar imprime un mensaje y sale 0. GitHub no permite `if: secrets.*` a nivel de job; por eso el skip va **dentro** del paso.

El token no aparece en el clon público. En logs de Actions se enmascara (`***`).
