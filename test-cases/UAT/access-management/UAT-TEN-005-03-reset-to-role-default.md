# UAT-TEN-005-03 — Reset to role default

Requirement: [REQ-TEN-005.3](../../../docs/02-requirements/FRD/access-management/requirement.md). Acceptance: AC-5, AC-6.

| Step | Action | Expected result |
|---|---|---|
| 1 | As **Ravi**, open **Asha**'s access (after UAT-TEN-005-01). | *Manage subscriptions* is ON with "Custom". |
| 2 | Click the **Reset to role default** icon on that row, then **Save changes**. | The item returns to OFF with "From role"; Asha's row no longer shows a custom count. |
| 3 | Give Asha two custom items, save, then click **Reset all to role default** and confirm. | All custom badges disappear; the Activity tab shows the reset entries. |

Result: ☐ Pass ☐ Fail — Tester: ______ Date: ______
