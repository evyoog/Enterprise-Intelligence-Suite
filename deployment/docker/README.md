# Docker

Container build assets.

- `Dockerfile-template` — standard multi-stage Dockerfile with required traceability labels.
- `postgres-init/` — init script used by the root `docker-compose.yml` to create the `eis_platform` schema and load the backend schema.

The backend's own Dockerfile lives at `backend/docker/Dockerfile`.
