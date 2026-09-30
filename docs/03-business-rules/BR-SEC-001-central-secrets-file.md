# BR-SEC-001 — One common file for API keys and passwords

| Field | Value |
|---|---|
| Area | Security (applies to every integration and service) |
| Decision | [C46](../01-business/roadmap/open-decisions.md#c46) |
| Template | [`config/secrets.env.example`](../../config/secrets.env.example) |

## Rule
1. Every API key, client secret, password and signing key used by EIS is kept in **one common file**: `config/secrets.env`.
2. `config/secrets.env` is **never committed** (it is in `.gitignore`). The committed template `config/secrets.env.example` lists every variable with an empty value and a comment.
3. The backend, AI service and scripts read these values as environment variables loaded from that file (for example by `scripts/dev.sh` and the Docker `env_file`). Deployed environments load the same variable names from the environment's secret store.
4. A new integration adds its variables to the template in the same change that introduces it. No key or password is written in source code or in a new configuration default.
5. Secrets are never returned by an API or shown on a screen. Admin screens may show only whether a value is set, and non-secret identifiers masked (for example a key ID).
6. The existing `spring.datasource` block in `backend/src/main/resources/application.yml` stays exactly as it is, by product-owner instruction. Its `DB_USER` and `DB_PASSWORD` variables are listed in the common file so the file's values take effect without editing that block.
