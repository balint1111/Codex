# AGENTS — Backend

## Testing
- Run unit tests:
  ```bash
  ./gradlew test --no-daemon
  ```
- Run tests against a local Postgres:
  ```bash
  DISABLE_TESTCONTAINERS=true ./gradlew test --no-daemon
  ```
  Ensure a PostgreSQL instance is available and, if necessary, set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` (defaults: `jdbc:postgresql://localhost:5432/postgresTest`, `postgres`, `postgres`).
