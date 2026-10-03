# UAT-TEN-005-02 — Grant product access

Requirement: [REQ-TEN-005.3, .5](../../../docs/02-requirements/FRD/access-management/requirement.md). Acceptance: AC-5, AC-9, AC-10.

| Step | Action | Expected result |
|---|---|---|
| 1 | Sign in as **Ravi**; Access management → **Dev** → **Product access**. | Valam.ai is listed with its plan and "Seats 12/15" (if seats apply). Thittam.ai is **not** listed (not subscribed). |
| 2 | Turn Valam.ai ON (if it is OFF) and **Save changes**. | Toast "Access updated for Dev"; badge "Custom" (or "From role" if the role default already gives it). |
| 3 | Sign in as **Dev**; open My products. | Valam.ai is available to open. |
| 4 | (Only if product access uses seats, Open question 3) With all seats used, try to turn the product ON for another member. | The switch is disabled with "No free seats on Valam.ai". |

Result: ☐ Pass ☐ Fail — Tester: ______ Date: ______
