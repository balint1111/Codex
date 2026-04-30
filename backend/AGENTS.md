# AGENTS — Backend

## Architecture
- **Framework**: Spring Boot 3.1.1 with Webflux for reactive, non-blocking HTTP
- **Database**: PostgreSQL with R2DBC for reactive data access; jOOQ for type-safe SQL generation
- **Security**: Spring Security with OAuth2 resource server (JWT tokens from external IdP like Keycloak)
- **Code Generation**: jOOQ generates Kotlin data classes and query DSL from Liquibase DB schemas (output: `src/main/generated`)

## Project Structure
- `src/main/kotlin/com/example/codex/`
  - `config/`: Spring configuration, JWT auth converter
  - `controller/`: HTTP endpoints
  - `service/`: Business logic (returns reactive `Mono` and `Flux`)
  - `repository/`: Data access layer using R2DBC and jOOQ
  - `domain/`: Domain models and DTOs
- `src/main/resources/db/changelog/`: Liquibase database schema, YAML-based
- `buildSrc/`: Custom Gradle plugins (testcontainers and code generation)

## Code Style
- Kotlin with ktlint 1.7.1 (configured via Spotless)
- Format code: `./gradlew spotlessApply`
- Target exclusions: `src/main/generated/**` (auto-generated, don't edit)

## Build & Code Generation
- **jOOQ code generation**: `./gradlew generateJooq` → generates Kotlin records, queries, and DAOs
- **Post-processing**: jOOQ output is post-processed with `lowercaseJooqNames` task to normalize generated class names
- Generated code uses Kotlin data classes (immutable) with JPA annotations

## Testing
- **Integration tests** extend `AbstractIntegrationTest` (provides DynamicPropertySource for DB config)
- **Reactive testing**: Use `StepVerifier` from `reactor-test` for `Mono`/`Flux` assertions
- Run all tests:
  ```bash
  ./gradlew test --no-daemon
  ```
- Run tests with **Testcontainers** (auto-spins PostgreSQL 16-alpine, shared across test runs):
  ```bash
  ./gradlew test --no-daemon
  ```
- Run tests with **external PostgreSQL** (skip Testcontainers):
  ```bash
  DISABLE_TESTCONTAINERS=true ./gradlew test --no-daemon
  ```
  - Requires PostgreSQL at `jdbc:postgresql://localhost:5432/postgresTest` (or set `SPRING_DATASOURCE_URL`)
  - Defaults: user=`postgres`, password=`postgres` (or set `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`)
- Test database credentials (testcontainers): user=`codex`, password=`codex`, database=`codex`
- Example test: `src/test/kotlin/com/example/codex/service/UserServiceIT.kt`

## Environment Variables
- `DB_HOST`, `DB_PORT`: Database connection (production)
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`: Override DB credentials for tests
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`: OAuth2 JWT issuer for token validation
- `FRONTEND_URL`: Frontend base URL (CORS/configuration)
- `DISABLE_TESTCONTAINERS=true`: Use external PostgreSQL instead of Testcontainers
