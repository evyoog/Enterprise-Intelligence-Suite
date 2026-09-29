# Business rules — Provider Onboarding

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-PTR-001 | Applying to become a provider requires no Vyoog account or Keycloak identity — a `Provider` row carries its own contact details directly. | backend | REQ-PTR-001.1 |
| BR-PTR-002 | A new provider always starts REGISTERED. | backend | REQ-PTR-001.2 |
| BR-PTR-003 | Verify/Approve/Activate each require the provider's current status to be exactly the preceding stage (REGISTERED, VERIFIED, APPROVED respectively) — skipping a stage is refused (400). | backend | REQ-PTR-001.2 |
| BR-PTR-004 | Reject is refused once a provider is ACTIVE or already REJECTED; every other stage (REGISTERED, VERIFIED, APPROVED) may still be rejected. | backend | REQ-PTR-001.3 |
| BR-PTR-005 | At most one contract row per provider — creating a contract for a provider that already has one edits the existing row rather than creating a second, enforced additionally by a unique DB index on `provider_id`. | backend | REQ-PTR-001.4 |
| BR-PTR-006 | A contract's end date must be strictly after its start date (400 otherwise). | backend | REQ-PTR-001.4 |
| BR-PTR-007 | Every contract save sets `status = ACTIVE` unconditionally, regardless of the row's previous status — editing an EXPIRED contract's terms reactivates it. | backend | REQ-PTR-001.5 |
| BR-PTR-008 | A scheduled job (`ContractExpiryJob`, hourly, same pattern as `SubscriptionExpiryJob`) flips any ACTIVE contract whose end date has passed to EXPIRED; this is never computed ad hoc on read. | backend | REQ-PTR-001.5 |
| BR-PTR-009 | Every lifecycle transition and contract save is recorded in the audit log (`PROVIDER_REGISTERED`/`_VERIFIED`/`_APPROVED`/`_ACTIVATED`/`_REJECTED`, `PARTNER_CONTRACT_SAVED`/`_EXPIRED`). | backend | REQ-PTR-001.2–.5 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
