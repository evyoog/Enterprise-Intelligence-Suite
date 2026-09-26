# TC-IAM-063: Oidc Federation — AC-9

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-063 |
| Requirement ID (required) | [REQ-IAM-006](../../../docs/02-requirements/FRD/oidc-federation/requirement.md) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/oidc-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-006](TESTPLAN-IAM-006.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
The OIDC section.

## Steps
1. Arrange: the OIDC section.
2. Act: it is shown.
3. Observe the response and the UI.

## Expected Result
Its text exists in `en.json` and `es.json`, it is keyboard operable, and jest-axe reports no violations.

## Automated coverage
- `frontend/src/components/federation/OidcProvidersSection.test.tsx` — "has no detectable a11y violations"

**Manual check:** Check the Spanish text and keyboard use in the dev environment.

## Actual Result
The automated tests below passed on 2026-09-26; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
