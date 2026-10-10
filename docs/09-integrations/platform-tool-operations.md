# Platform ↔ tool synchronization — operations

[REQ-INT-003](../02-requirements/FRD/platform-tool-sync/requirement.md), phase 8. For the person who runs the platform and the tools: what to watch, what an alert means, what to do. Settings: [Keycloak clients and `SYNC_*`](../../deployment/keycloak-sync-clients.md). Screen: Admin → Integrations → Platform events → **Tool sync**.

## 1. Metrics

Both sides publish the same names (Micrometer; the platform at `/actuator/metrics/<name>`, the Macro Planner at `/api/actuator/metrics/<name>` and under the prefix `platform.metrics.prefix`, default `platformsync`).

| Metric | Side | Meaning |
|---|---|---|
| `platformsync.delivery{tool,status}` | platform | Sends by result: `delivered`, `retry`, `unreachable`, `failed` |
| `platformsync.delivery.failed{tool}` | platform | A message became FAILED (attempts used up, or the tool rejected it for good) |
| `platformsync.delivery.waiting{tool}`, `platformsync.delivery.failed.open{tool}` | platform (gauge) | Messages waiting now; messages FAILED now (until someone retries them) |
| `platformsync.lag.seconds{tool}` | platform | From the change to its delivery (summary with 0.5 and 0.95 percentiles) |
| `platformsync.lag.oldest.seconds{tool}` | platform (gauge) | Age of the oldest waiting message |
| `platformsync.drift.detected{tool,type}` | platform | Objects a tool lacked or held at an older version when reconcile compared |
| `platformsync.entitlement.denied{reason}` | platform | `get_entitlement` answers that said no, by reason |
| `platformsync.inbound{tool,status}`, `.inbound.duration{tool}` | tool | Messages from the platform by result |
| `platformsync.lag.seconds{tool}` | tool | How old a message was when it was applied (now minus its `occurredAt`) |
| `platformsync.drift.detected{type}` | tool | Objects the platform had sent that the tool no longer held (found when it answered a reconcile) |
| `platformsync.outbound{tool,status}` | tool | Calls to the platform by result (`applied`, `duplicate`, `rejected`, `unavailable`) |
| `platformsync.entitlement.denied{reason}` | tool | Requests refused by the access check, by reason |
| `platformsync.provisioning{status}` | tool | `provision_tenant` calls by result |

## 2. Alert thresholds (starting values; tune after two weeks of real traffic)

| Alert | Condition | Severity | First thing to check |
|---|---|---|---|
| Messages are failing | `platformsync.delivery.failed.open` > 0 for 10 minutes, or any increase of `platformsync.delivery.failed` | Page during office hours | Tool sync tab → Failed: the reason column. `INVALID_PAYLOAD` is a bug (platform or tool), `NOT_ALLOWED_CLIENT` a Keycloak setting, `TENANT_NOT_READY` a tenant not provisioned |
| A tool is behind | `platformsync.lag.oldest.seconds` > 300 for 5 minutes | Warn; page above 1800 | Is the tool up? `platformsync.delivery{status="unreachable"}` rising means no. Waiting count growing with the tool up: its `retry` answers (database busy) |
| A tool is down | `platformsync.delivery{status="unreachable"}` increasing for 5 minutes | Page | Nothing is lost; the platform retries (5 s to 15 min). After 8 attempts a message is FAILED; **Retry** it from the tab once the tool is back |
| Drift | `platformsync.drift.detected` increases (either side) | Warn | A row was lost on one side. Reconcile repairs it; investigate the cause if it repeats (someone editing the tool's database, a restore) |
| Sign-in problems | `platformsync.entitlement.denied{reason="NOT_PROVISIONED"}` or `…{reason="NO_PRODUCT_ACCESS"}` spikes | Warn | A subscription was sold and the tenant is not ready, or access was not granted: check the organization's tenant status in the tab |
| Slow delivery | `platformsync.lag.seconds` p95 > 30 s for 15 minutes | Warn | Load on the tool; the delivery job's batch size (`app.sync.batch-size`) |

Measured on the end-to-end run (one laptop, local PostgreSQL): a new organization is provisioned and completely synchronized in about 5 seconds; a change to an existing object reaches the tool within a second or two. A tool that was stopped catches up by itself within seconds of being restarted.

## 3. What to do

| Situation | Do |
|---|---|
| A message is FAILED | Read its reason in the tab. If it is the tool's temporary problem, **Retry**. If the platform's data was wrong, fix the data (the next change is a new message) or **Replay** the object |
| A tool was down for hours | Nothing to do: PENDING messages continue when it returns. Messages that reached the attempt limit are FAILED: select Failed, **Retry** each (or **Resync** the organization, which queues everything again) |
| A tool lost or damaged data | **Reconcile** the organization (tab). It lists what differs and resends only that. A second reconcile reports "in step" |
| A new organization has no tenant | Check it has an ACTIVE subscription to the tool's product and an active ORG_ADMIN with a Keycloak id; then **Start** (the tab) |
| Stop sending without losing anything | **Pause** the tool (the tab) or `TOOL_DELIVERY_ENABLED=false`; changes are kept as waiting messages |
| Secret rotated | Update `SYNC_CLIENT_SECRET` (platform) / `PLATFORM_SYNC_CLIENT_SECRET` (tool) in the secrets manager and restart; tokens are fetched again |

## 4. Reconcile in production

`app.sync.reconcile.enabled=true` (environment `SYNC_RECONCILE_ENABLED`) runs a reconcile of every READY tenant every night at 02:30 (`app.sync.reconcile.cron`). It is safe to run at any time: it compares `get_state_digest` first and only lists and resends where a count or hash differs. The tool computes its answer from its own rows; for an object whose row is missing it also forgets the recorded version, so that the resend is applied instead of being answered `duplicate`.

## 5. Running the end-to-end test

The real platform, the real Macro Planner (as a separate process) and a fake Keycloak that signs real tokens:

```bash
(cd <macro repo>/backend && mvn -DskipTests package)                       # the jar
createdb e2e_macro                                                          # a scratch PostgreSQL database
cd <this repo>/backend
E2E_MACRO_JAR=<macro repo>/backend/target/vyoog-pms-1.0.0-SNAPSHOT.jar \
E2E_PG_URL=jdbc:postgresql://localhost:5432/e2e_macro E2E_PG_USER=postgres E2E_PG_PASSWORD=... \
  mvn -B test -Dtest=PlatformMacroEndToEndTest
```
It drops and recreates the schemas `platform_control`, `pms_*` and `t_e2e_default` in that database and writes the Macro's log to `backend/target/e2e-macro.log`. Without `E2E_MACRO_JAR` it is skipped.
