# eis-platform

Backend API for the EIS Platform — product showcase, purchases, and Keycloak SSO.

## Structure

- `config/` — cross-cutting infrastructure (security, JPA auditing, caching).
- `common/` — shared exception handling, utilities, and annotations used across modules.
- `modules/` — one package per feature domain (`controller` -> `service` -> `repository`),
  enforced by the ArchUnit test in `src/test/.../architecture`. `modules/product` is the
  first module; add new features as sibling packages under `modules/`.
- `resources/config/application-{dev,uat,prod}.yml` — environment overrides. Spring Boot
  scans `classpath:/config/` by default, so these load automatically alongside
  `application.yml` for whichever profile is active.
- `resources/db/schema.sql` — the full schema, one hand-maintained file (not Flyway
  migrations — see CLAUDE.md decision 11). Run it by hand against any new database
  before first use; editing it later does **not** alter an already-existing table by
  itself, so a real schema change still needs the equivalent `ALTER TABLE` run by
  hand too.

## Running locally

Prerequisite (one-time): a local Postgres with a database named `vyoog`, containing
a schema named `eis_platform` (`CREATE SCHEMA eis_platform AUTHORIZATION postgres;`),
with `db/schema.sql` run against it to create the tables.

```bash
mvn spring-boot:run
```

The API is served under `http://localhost:8081/api`. Swagger UI is enabled in `dev`/`uat`
at `/api/swagger-ui.html`.

## SSO

JWTs are validated against the shared Vyoog Keycloak realm (`eVyoog`), the same identity
server used by `vyg-pms` and other products. Create a dedicated client-id for this app in
that realm before wiring up login on the frontend.
