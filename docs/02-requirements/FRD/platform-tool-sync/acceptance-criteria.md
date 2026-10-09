# Acceptance criteria — Platform ↔ tool synchronization

| ID | Criterion |
|---|---|
| AC-1 | **Given** an organization subscribed to tools A and B **When** an administrator changes the organization or moves an org node **Then** A and B each hold the same data and the same hierarchy shape, with the same ids as references, within the retry window. A tool the organization is not subscribed to receives nothing. |
| AC-2 | **Given** tool B is down **When** a change is made **Then** A is updated at once, B's delivery shows PENDING/RETRYING, then FAILED after the limit; **When** B returns and an administrator replays **Then** B holds the current state. |
| AC-3 | **Given** the same message delivered twice, or versions 3, 1, 2 delivered in that order **Then** the tool ends with version 3 applied once and no duplicate rows. |
| AC-4 | **Given** a person registers in tool A **When** their email already exists in Keycloak **Then** the same Keycloak user is reused and no second user is created; **When** it does not exist **Then** one user is created with the real email and reported to the platform. If the report fails, the created user is disabled and nothing remains in the tool. |
| AC-5 | **Given** a registration in a tool **Then** the person has no product access and no product business records until access is granted through the platform. |
| AC-6 | **Given** a user is edited in tool A **Then** the change is visible on the platform and in tool B; **Given** the platform is unreachable **Then** the edit is refused and nothing is written in A. |
| AC-7 | **Given** two accounts with the same unverified email **Then** they are not merged; with a verified email an existing unlinked row is linked and the link is recorded. |
| AC-8 | **Given** a subscription whose end date is 2026-10-31 **Then** it is stored and published as `2026-10-31T23:59:00.000+05:30`, access is allowed at 2026-10-31 23:58 IST and denied at 2026-11-01 00:00 IST in every tool. |
| AC-9 | **Given** product access is revoked on the platform **Then** the tool denies that user within 5 minutes for normal actions and at once for sensitive actions; a late older message cannot restore the access. |
| AC-10 | **Given** the platform is unavailable **Then** sensitive actions in a tool are denied; normal actions continue on the local copy until it is 5 minutes old. |
| AC-11 | **Given** a message with a database or schema name, or from a client not on the allow-list, or for an organization the caller does not own **Then** it is rejected and recorded. |
| AC-12 | **Given** a new subscription **Then** the tenant is provisioned (idempotently), shows READY with a schema version, and data is then synchronized; a failed provisioning shows FAILED with the error and resumes on retry. Cancelling or expiring a subscription deletes no schema. |
| AC-13 | **Given** tenant A and tenant B in the same tool **Then** a user of A cannot read B's data by changing a header, host or request value. |
| AC-14 | **Given** a deliberately deleted copy in a tool **Then** reconcile detects and repairs it. |
| AC-15 | The admin monitor shows status, last success, last error, attempts and lag per organization and tool; retry and replay need `MANAGE_INTEGRATIONS` and are audited. |
| AC-16 | No password, token, MFA secret, payment or bank detail appears in any message, log or tool table. |
