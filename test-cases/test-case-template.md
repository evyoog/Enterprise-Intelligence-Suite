<!--
TEST CASE TEMPLATE
File naming convention: TC-<APP-CODE>-<NNN>.md
Every test case must trace to exactly one Requirement (or Acceptance Criterion within one).
-->

# TC-<APP-CODE>-<NNN>: <Short Title>

| Field | Value |
|---|---|
| Test Case ID (required) | TC-<APP-CODE>-<NNN> |
| Requirement ID (required) | REQ-<APP-CODE>-<NNN> |
| Acceptance Criterion | AC<N> |
| Test Plan | TESTPLAN-<APP-CODE>-<NNN> |
| Priority | P0 / P1 / P2 |
| Type | Functional / Security / Performance / Regression |
| Automated | Yes / No |

## Preconditions
<System/data state required before running this test.>

## Steps
1. <Action>
2. <Action>
3. <Action>

## Expected Result
<What must be true for this test to pass — be specific and measurable, not "it works".>

## Actual Result
<Filled in during execution.>

## Status
Not Run / Passed / Failed / Blocked

## Linked Defect (if failed)
<Defect ID, if applicable.>

---
### Example (for reference — delete before using this template)
> **TC-CAT-001** — Search returns results within SLA
> **Requirement:** REQ-CAT-001, AC4
> **Steps:** 1. Issue `GET /v1/products/search?keyword=database` with a warm cache. 2. Measure response time across 100 requests.
> **Expected Result:** p95 latency < 2000ms.
