<!--
REQUIREMENT TEMPLATE
Fill every field below. Fields marked (required) must not be left blank.
File naming convention: REQ-<APP-CODE>-<NNN>.md  (e.g. REQ-CAT-001.md)
This document becomes AI input for code generation — keep it precise and unambiguous.
-->

# REQ-<APP-CODE>-<NNN>: <Short Title>

| Field | Value |
|---|---|
| Requirement ID (required) | REQ-<APP-CODE>-<NNN> |
| Title (required) | |
| Application (required) | <e.g. APP-CATALOG> |
| Capability | <e.g. CAP-CATALOG-SEARCH> |
| Feature ID (required) | FTR-<APP-CODE>-<NNN> |
| Function ID | FUN-<APP-CODE>-<NNN> |
| Actor (required) | <Customer / AI Agent / Platform Admin / System, etc.> |
| Priority | P0 / P1 / P2 |
| MVP | Yes / No |
| Status | Draft / In Review / Approved / Implemented / Deprecated |
| Author | |
| Design Reference | DES-<APP-CODE>-<NNN> |
| Test Reference | TC-<APP-CODE>-<NNN> |
| Dependencies | <other REQ/FTR IDs this depends on> |

## Business Objective
<One or two sentences: why does this requirement exist, and what business outcome does it serve?>

## Description
<Full narrative description of the requirement.>

## Preconditions
- <Condition that must be true before this requirement's behavior applies>

## Functional Requirements
- FR1: <statement>
- FR2: <statement>

## Non-Functional Requirements
- <performance, scalability, availability targets specific to this requirement>

## Acceptance Criteria
- AC1: <Given/When/Then or plain statement>
- AC2:
- AC3:

## Security Requirements
<Tenant isolation, authN/authZ constraints, data classification, encryption needs, etc. State "None beyond platform baseline" if not applicable.>

## Performance Requirements
<e.g. "Results must return within 2 seconds for 95% of requests." State "None beyond platform baseline" if not applicable.>

## Data Requirements
<Entities read/written, data residency constraints, retention.>

## API Requirements
<Endpoint(s) this requirement implies, e.g. `GET /v1/products/search`. Reference the API catalog ID if one exists.>

## Localization / Multi-Region Notes
<Does this requirement behave differently by locale or region?>

## Out of Scope
<Explicitly state what this requirement does NOT cover, to prevent scope creep during implementation.>

---
### Example (for reference — delete before using this template)
> **REQ-CAT-001** — Search PaaS Products
> **Business Objective:** Allow customers to discover platform products.
> **Acceptance Criteria:** AC1: Customer can search by keyword. AC2: Customer can filter by region. AC3: Customer can filter by price. AC4: Results must return within 2 seconds for 95% of requests.
> **Security:** Tenant isolation required.
> **API:** `GET /v1/products/search`
