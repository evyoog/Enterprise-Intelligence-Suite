# Acceptance criteria — Seats and quantity (REQ-SUB-003)

| ID | Requirement | Criterion | Test case |
|---|---|---|---|
| AC-1 | .1 | **Given** an organization subscription **then** its seat summary shows quantity ≥ 1, seats in use and the minimum; **given** an individual subscription **then** no seat control is offered and a seat change is refused. | [TC-SUB-013](../../../../test-cases/functional/subscription-seats/TC-SUB-013.md) |
| AC-2 | .2 | **Given** 12 seats in use of 15 **when** the admin sets 20 **then** the quantity is 20 immediately and a 21st member… can be added up to 20. | [TC-SUB-014](../../../../test-cases/functional/subscription-seats/TC-SUB-014.md) |
| AC-3 | .3 | **Given** 12 seats in use **when** the admin sets 10 **then** it is refused with "12 seats are in use…" and the quantity is unchanged; setting 12 succeeds. | [TC-SUB-015](../../../../test-cases/functional/subscription-seats/TC-SUB-015.md) |
| AC-4 | .4 | **Given** all seats of a subscription in use **when** a member is added **then** it is refused as over the seat limit. | [TC-SUB-016](../../../../test-cases/functional/subscription-seats/TC-SUB-016.md) |
| AC-5 | .5 | **Given** a seat change **then** an audit entry and a `SeatsChanged` event (from, to) exist. | [TC-SUB-014](../../../../test-cases/functional/subscription-seats/TC-SUB-014.md) |
| AC-6 | .2 | **Given** a member without `MANAGE_ORGANIZATION` or another organization's subscription **when** changing seats **then** 403 / 404. | [TC-SUB-017](../../../../test-cases/functional/subscription-seats/TC-SUB-017.md) |
| AC-7 | Accessibility | **Given** the seats section and its confirmation dialog **then** each passes the axe test. | [TC-SUB-017](../../../../test-cases/functional/subscription-seats/TC-SUB-017.md) |
