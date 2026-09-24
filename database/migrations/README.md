# Database migrations

Versioned, forward-only schema changes, Flyway naming:

```
V<NNN>__<description>.sql      e.g. V025__create_shift_change_request.sql
```

**Current state:** the backend does not yet run migrations (Flyway is disabled in `backend/src/main/resources/application.yml`). The canonical schema today is `backend/src/main/resources/db/schema.sql`, applied by hand. Until migrations are wired into the backend, every schema change must be made in that file **and** added here as a migration so existing databases can be upgraded.
