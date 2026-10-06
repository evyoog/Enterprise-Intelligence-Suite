# TC-PRT-037: Intermediate roles get exactly their permitted items

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-037 |
| Requirement ID (required) | [C80](../../../docs/01-business/roadmap/open-decisions.md#c80) — [application layout](../../../docs/05-ui/screen-requirements/application-layout.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Users holding only MANAGE_BILLING, only MANAGE_SUPPORT_TICKETS, only VIEW_AUDIT_LOG, only KNOWLEDGE_CONTRIBUTE.

## Steps
1. Sign in as each and read the sidebar.
2. Open /admin/integrations/api-keys directly as someone holding MANAGE_INTEGRATIONS (deep link) and refresh.

## Expected Result
Billing manager: Billing with its three children under Operations plus Service status. Support agent: Support under Operations. Auditor: Audit log. Contributor: Knowledge Center with Articles, Drafts, Manage (no Categories). No empty group is shown. On the deep link only Integrations is open, API keys is current, and a refresh keeps it so.

## Automated coverage
- `appNavigation.test.ts` — `intermediate roles get exactly what their permissions allow`
- `AppShell.test.tsx` — `shows an intermediate role (billing manager)…`, `opens only the group holding the current page…`

## Actual Result
The automated tests above passed on 2026-10-06. Looking at it in a running browser is manual.

## Status
Passed (automated run 2026-10-06)

## Linked Defect (if failed)
