# TESTPLAN-INT-001: API management

| Field | Value |
|---|---|
| Feature ID (required) | FTR-INT-001 |
| Requirement(s) covered | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) |
| Decision | [C61](../../../docs/01-business/roadmap/open-decisions.md#c61) |
| Author | Not specified |
| Status | Draft (the FRD is Draft; built 2026-10-03 at the product owner's request with the engineering defaults recorded in the FRD) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md): TC-INT-001 to TC-INT-007.

## Approach
Backend: `ApiKeyAuthenticationTest` (MockMvc through the real security chain), `ApiKeyAuthenticationFilterTest`, `RateLimitFilterTest` (fixed clock, small limits). Frontend: `ApiKeysSection.test.tsx`, `AdminApiKeysPage.test.tsx` with jest-axe.

## Test cases
| Test case | Title | Acceptance criteria | Automated |
|---|---|---|---|
| [TC-INT-001](TC-INT-001.md) | A key is shown once, stored only as a hash, and its creation is audited | AC-1, AC-8 | Yes |
| [TC-INT-002](TC-INT-002.md) | A key acts as its owner | AC-2 | Yes |
| [TC-INT-003](TC-INT-003.md) | Revoked, expired and unknown keys are refused; users manage only their own keys | AC-3, AC-4 | Yes |
| [TC-INT-004](TC-INT-004.md) | Rate limits answer 429 with Retry-After | AC-5 | Yes |
| [TC-INT-005](TC-INT-005.md) | Every endpoint is also served under /v1 | AC-6 | Yes |
| [TC-INT-006](TC-INT-006.md) | Administrators see key usage; keys never reach platform administration | AC-7 | Yes |
| [TC-INT-007](TC-INT-007.md) | API key screens are accessible | AC-9 | Yes |

## Blocked or partial
- Rate limits are counted per backend instance (engineering default); a multi-instance check is not possible here.
