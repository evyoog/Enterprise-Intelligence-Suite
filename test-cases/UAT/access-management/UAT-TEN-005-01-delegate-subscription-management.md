# UAT-TEN-005-01 — Delegate subscription management

Requirement: [REQ-TEN-005.4](../../../docs/02-requirements/FRD/access-management/requirement.md) (D16). Acceptance: AC-7, AC-8, AC-17.

| Step | Action | Expected result |
|---|---|---|
| 1 | Sign in as **Asha** (Member). Open My subscriptions. | The organization's subscriptions show no Suspend, Cancel, Change plan or Change seats actions. |
| 2 | Sign out. Sign in as **Ravi** (Organization admin). Open **Access management** in the sidebar. | The Members tab lists the organization's members. |
| 3 | Click **Asha** → **Feature permissions** → **Subscriptions**. | *Manage subscriptions* is OFF, badge "From role". |
| 4 | Turn *Manage subscriptions* ON, click **Save changes**. | Toast "Access updated for Asha"; the item shows the "Custom" badge; Asha's row shows "1 custom". |
| 5 | Open the **Activity** tab. | An entry: Ravi changed Asha's *Manage subscriptions* Off → On (override), with the time. |
| 6 | Sign in as **Asha**; open My subscriptions; suspend Valam.ai, then reactivate it. | Both actions succeed. |

Result: ☐ Pass ☐ Fail — Tester: ______ Date: ______
