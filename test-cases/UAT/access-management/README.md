# UAT — Access management (REQ-TEN-005)

Step-by-step scenarios for a business user. Requirement: [REQ-TEN-005](../../../docs/02-requirements/FRD/access-management/requirement.md); acceptance criteria: [acceptance-criteria.md](../../../docs/02-requirements/FRD/access-management/acceptance-criteria.md). Run on a test organization once the feature is built (status: **not yet runnable** — the FRD is Draft).

| Script | Scenario | Acceptance criteria |
|---|---|---|
| [UAT-TEN-005-01](UAT-TEN-005-01-delegate-subscription-management.md) | Delegate subscription management to a member | AC-7, AC-8, AC-17 |
| [UAT-TEN-005-02](UAT-TEN-005-02-grant-product-access.md) | Grant a member access to a product | AC-5, AC-9, AC-10 |
| [UAT-TEN-005-03](UAT-TEN-005-03-reset-to-role-default.md) | Reset a member to their role defaults | AC-5, AC-6 |
| [UAT-TEN-005-04](UAT-TEN-005-04-forbidden-grant.md) | Try a forbidden grant and read the reason | AC-11, AC-12, AC-13 |

**Test data:** organization "UAT Org" with an active subscription to Valam.ai (15 seats) and none to Thittam.ai; users: Ravi (Organization admin), Meera (Organization admin), Asha (Member), Dev (Member).

Record for each run: tester, date, environment, result (Pass / Fail), defect link.
