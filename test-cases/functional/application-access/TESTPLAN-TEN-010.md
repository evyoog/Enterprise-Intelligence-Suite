# TESTPLAN-TEN-010: Application Access Requests Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-TEN-010 |
| Requirement(s) covered | [REQ-TEN-010](../../../docs/02-requirements/FRD/application-access/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-10-15, [C103](../../../docs/01-business/roadmap/open-decisions.md#c103)) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/application-access/acceptance-criteria.md): self-service access requests (submit, approve, reject, cancel, duplicate/eligibility guards, cross-organization isolation), self-removal of access, and the once-only 3-day order reminder. Out of scope: anything the FRD lists as out of scope (multi-level approval, a workflow engine, requesting a brand-new organization subscription, auto-deciding a reminded order).

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Spring Boot tests against H2 in `backend/` (`AccessRequestServiceTest`, the extended `OrderServiceTest`) |
| Integration | Same backend test classes, exercising the real service/repository layers, including the grant/revoke path shared with [access-management](../access-management/TESTPLAN-TEN-005.md) (`AccessSyncService`) |
| Security (SAST/DAST) | Permission and cross-organization cases below (TC-TEN-088) |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-TEN-080](TC-TEN-080.md) | AC-1 Member submits an access request | P0 | Yes |
| [TC-TEN-081](TC-TEN-081.md) | AC-5 Admin approves a request, granting access | P0 | Yes |
| [TC-TEN-082](TC-TEN-082.md) | AC-6 Admin rejects a request with a required note | P0 | Yes |
| [TC-TEN-083](TC-TEN-083.md) | AC-4 Duplicate pending request is refused | P0 | Yes |
| [TC-TEN-084](TC-TEN-084.md) | AC-2 Cannot request a product the organization does not subscribe to | P0 | Yes |
| [TC-TEN-085](TC-TEN-085.md) | AC-3 Cannot request a product the member already has access to | P0 | Yes |
| [TC-TEN-086](TC-TEN-086.md) | AC-8 Requester cancels their own pending request | P0 | Yes |
| [TC-TEN-087](TC-TEN-087.md) | AC-9 Member removes their own access without affecting the organization's subscription | P0 | Yes |
| [TC-TEN-088](TC-TEN-088.md) | AC-10 Cross-organization isolation: another organization's admin cannot act on the request | P0 | Yes |
| [TC-TEN-089](TC-TEN-089.md) | AC-12 Order reminder fires once after 3 days and never auto-decides | P0 | Yes |
