# Keycloak service clients for the platform ↔ tool synchronization

[REQ-INT-003](../docs/02-requirements/FRD/platform-tool-sync/requirement.md), [contract v1 section 7](../docs/09-integrations/platform-tool-contract-v1.md). Realm `eVyoog` at `https://user.evyoog.com`.

Platform and tools call each other's MCP servers (`/api/mcp`) with a token from **client credentials**: each side has its **own service client**. No person's token is ever used, no user row is created for a service client, and no secret is stored in this repository.

| Client id | Used by | Calls | The receiver accepts it because |
|---|---|---|---|
| `eis-sync` | the platform (EIS) | every tool's `/api/mcp` (`upsert_*`, `set_*`, `provision_tenant`, `get_state_digest`, …) | the tool lists `eis-sync` in `platform.inbound.allowed-clients` |
| `thittam-sync` | Thittam Macro Planner | the platform's `/api/mcp` (`report_user_created`, `get_entitlement`, …) | a `tool_connector` row with `client_id = 'thittam-sync'` and `status = 'ACTIVE'` exists, and the organization has an ACTIVE subscription to the tool |
| `<tool>-sync` | each future tool | same as `thittam-sync` | same, with its own `tool_connector` row |

## Create a client (once per client, per environment)

1. Realm `eVyoog` → **Clients** → **Create client**: client id as above, *Client authentication* **On**, *Standard flow* off, *Direct access grants* off, *Service accounts roles* **On**.
2. **Credentials** tab: copy the client secret **into the secrets manager** (never into a file, a chat or this repository). Rotate it by *Regenerate* and updating the secret.
3. **Service account roles**: grant **no roles**. The receivers decide by client id and tenant, not by role.
4. **Client scopes → `<client>-dedicated` → Add mapper → Audience**: *Included client audience* = the **receiver's own client id** (for `eis-sync`: the tool's client, for example the one the Macro Planner validates; for `thittam-sync`: the platform's client `eVyoog`), *Add to access token* On.
   Why: both applications accept a token only if the receiver's own client id is in `aud` or is the `azp` (`ClientAudienceValidator` in the platform). A service-client token has its own client as `azp`, so without this mapper every call is answered `401`.
5. Token lifespan: the default (5 minutes) is fine; the platform caches the token until 30 seconds before it expires and fetches a new one after a `401`.

## Configure the platform (EIS)

| Setting | Where it comes from |
|---|---|
| `SYNC_CLIENT_ID` (default `eis-sync`) | environment |
| `SYNC_CLIENT_SECRET` | **secrets manager → environment variable, no default**. Without it the platform sends nothing: deliveries wait as PENDING and the Tool sync tab shows the reason. |
| `SYNC_TOKEN_URL` (default `KEYCLOAK_TOKEN_URI`) | environment |
| `SYNC_DEFAULT_DATASOURCE_REF` (default `default`) | environment; a **name the tool has in its own configuration**, never a URL or a database name |
| `TOOL_DELIVERY_ENABLED` (default `true`) | environment; `false` stops sending (messages are still prepared and kept), the rollback switch of phase 7 |
| `SYNC_RECONCILE_ENABLED` (default `false`) | environment; turn on only when the tools answer `get_state_digest` (phase 8) |

Retry defaults (`app.sync.retry.*`: 8 attempts, 5 s doubling to 15 min) and the rest of `app.sync.*` are in `backend/src/main/resources/application.yml`.

## Connect a tool (platform side)

A tool is connected by one row in `tool_connector` (there is no screen for this yet). Run it once per environment, with the product's catalog id:

```sql
INSERT INTO eis_platform.tool_connector (product_id, product_code, base_mcp_url, client_id, contract_version, status)
VALUES (<catalog product id>, 'thittam', 'https://<tool host>/api/mcp', 'thittam-sync', '1', 'ACTIVE');
```

From that moment, changes to organizations that subscribe to the product are turned into messages. An organization with an **ACTIVE** subscription to the product and no tenant gets one provisioned (`provision_tenant`), then everything is sent. Pause a tool in the Tool sync tab (messages are kept); set `status = 'PAUSED'`/`'ACTIVE'` by SQL only if the tab is unavailable.

## Checks after setting up a client

```bash
# a token for the platform's client (secret from the environment, never typed into the shell history)
curl -s -X POST "$SYNC_TOKEN_URL" -d grant_type=client_credentials -d client_id=eis-sync --data-urlencode client_secret@<(printf %s "$SYNC_CLIENT_SECRET") | jq '{azp, aud}' 
```
Decode the access token: `azp` is the client id and `aud` contains the receiver's client id. If `aud` is missing the mapper of step 4 is missing.

## Known limitation

`INTERNAL_SSO_SHARED_SECRET` (the sign-in bridge, [plan](../docs/09-integrations/platform-tool-sync-plan.md) Q13) and a few other secrets still have **committed development defaults** in `application.yml`. They are not part of this synchronization and are listed for retirement in phase 8. Rotate any value that was ever used outside a developer machine.
