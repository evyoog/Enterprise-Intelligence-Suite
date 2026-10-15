# TC-TEN-087: Member removes their own access without affecting the organization's subscription

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-087 |
| Requirement ID (required) | [REQ-TEN-010](../../../docs/02-requirements/FRD/application-access/requirement.md) (.9, BR-AAC-010) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/application-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-010](TESTPLAN-TEN-010.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessRequestServiceTest.aMemberCanRemoveTheirOwnAccessImmediatelyAndMustRequestAgainToGetItBack`) |

## Preconditions
A member with ACTIVE access to a product (their request was approved); the organization's subscription to that product is ACTIVE.

## Steps
1. Someone without access calls `removeMyAccess` for that product (negative check).
2. The member themself calls `removeMyAccess(memberId, productId)`.
3. Read the member's `organization_product_access` row and the organization's `product_subscription` row for that product.
4. The member calls `request` for the same product again.

## Expected Result
Step 1 is refused (`IllegalArgumentException`, no access to remove). Step 2 succeeds at once (no approval step) via `AccessSyncService#revokeProduct`, releasing the seat immediately. Step 3: the member's access row is INACTIVE; the organization's subscription to the product is unchanged (still ACTIVE) — self-removal never touches the org subscription. Step 4: a brand-new PENDING request is created — access is not silently reinstated from the old approval.

## Status
Passed (automated, 2026-10-15).

## Linked Defect (if failed)
None.
