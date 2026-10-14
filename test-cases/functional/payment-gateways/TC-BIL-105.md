# TC-BIL-105: Migration V038 upgrades an existing installation without interrupting Razorpay

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-105 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.14) |
| Acceptance Criterion | [AC-35](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-13, BR-17](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 13 (The existing Razorpay flow still works) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Regression |
| Automated | No |

## Preconditions
A PostgreSQL 16 copy of the schema at V037 with data.

## Steps
1. Apply V038.
2. Select from `payment_gateway_config` and check the Razorpay row.
3. Check `payment_webhook_event` columns and constraints.
4. Apply V038 again.
5. Start the backend and open the Payment Gateways page; pay an invoice.

## Expected Result
One Razorpay row (activated and default true); old webhook rows have provider RAZORPAY; the constraint `uq_payment_webhook_event_provider` exists and the old single-column unique is gone; the second run has no effect; Razorpay still takes payments.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theRegistryListsOnlyRealAndAdministratorAddedGatewaysNothingIsListedAsPlanned`

Manual only: the migration SQL itself is not executed by the automated tests (they run on in-memory H2, not PostgreSQL); `theRegistryListsOnlyRealAndAdministratorAddedGatewaysNothingIsListedAsPlanned` only shows that a missing Razorpay row is recreated active and default. **Not Run.**

## Actual Result
Not run. Needs the environment described under Preconditions.

## Status
Not Run

## Linked Defect (if failed)
None.
