# TESTPLAN-SUB-003: Seats and quantity

| Field | Value |
|---|---|
| Feature ID (required) | FTR-SUB-003 |
| Requirement(s) covered | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) |
| Decision | [C63](../../../docs/01-business/roadmap/open-decisions.md#c63), [C90](../../../docs/01-business/roadmap/open-decisions.md#c90), [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) (SE-1 to SE-4) |
| Author | Not specified |
| Status | Draft (the FRD is Draft; built 2026-10-03 at the product owner's request, changed 2026-10-14 for C100; per-seat pricing is an engineering default to confirm) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md): TC-SUB-013 to TC-SUB-017 (the C63 build, adjusted on 2026-10-14) and TC-SUB-045 to TC-SUB-050 (C100: per-seat pricing, an increase charged now, a decrease at renewal, permissions).

## Approach
Backend: `SubscriptionSeatServiceTest` and `SeatsGraceAndRecipientsTest` (the C100 seat, grace and recipient cases) on the H2-backed Spring context; `CouponAndPurchaseTest` for the seats on a purchase. Frontend: `OrganizationSubscriptionsSection.test.tsx`, `CartPage.test.tsx` and `MySubscriptionsPage.test.tsx` with jest-axe. Manual: the seat-increase invoice in Billing (TC-SUB-050, Not Run).

## Test cases
| Test case | Title | Acceptance criteria | Automated |
|---|---|---|---|
| [TC-SUB-013](TC-SUB-013.md) | Organization subscriptions show seats; individual subscriptions have none | AC-1 | Yes |
| [TC-SUB-014](TC-SUB-014.md) | Increasing seats takes effect at once and is audited and published | AC-2, AC-5 | Yes |
| [TC-SUB-015](TC-SUB-015.md) | Seats cannot go below the seats in use | AC-3 | Yes |
| [TC-SUB-016](TC-SUB-016.md) | Adding a member beyond a subscription's seats is refused | AC-4 | Yes |
| [TC-SUB-017](TC-SUB-017.md) | Only organization managers of the same organization change seats; screens are accessible | AC-6, AC-7 | Yes |
| [TC-SUB-045](TC-SUB-045.md) | The plan price is per seat: invoices bill price × seats | AC-8 | Yes |
| [TC-SUB-046](TC-SUB-046.md) | An increase takes effect now and is charged now for the remaining days | AC-9 | Yes |
| [TC-SUB-047](TC-SUB-047.md) | A decrease waits for the next renewal, with no credit, and can be withdrawn | AC-10 | Yes |
| [TC-SUB-048](TC-SUB-048.md) | The renewal applies a pending decrease, never below the seats in use | AC-11 | Yes |
| [TC-SUB-049](TC-SUB-049.md) | Manage billing changes seats; Manage subscriptions only looks; an ended term refuses a change | AC-12 | Yes |
| [TC-SUB-050](TC-SUB-050.md) | A seat-increase invoice appears in Billing with its line and tax | AC-13 | No (Not Run) |

## Blocked or partial
- Seat billing is specified by C100 and tested (TC-SUB-045 to TC-SUB-048). Not covered: the audit entries of a scheduled and a withdrawn decrease, tax on the seat-increase invoice (TC-SUB-050, Not Run), and the quantity on the invoice line of a new purchase.
- Per-seat pricing is an engineering default the product owner has to confirm; if it changes, TC-SUB-045 to TC-SUB-048 change with it.
