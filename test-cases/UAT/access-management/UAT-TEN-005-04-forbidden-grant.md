# UAT-TEN-005-04 — Try a forbidden grant and see the reason

Requirement: [REQ-TEN-005.6](../../../docs/02-requirements/FRD/access-management/requirement.md). Acceptance: AC-11, AC-12, AC-13.

| Step | Action | Expected result |
|---|---|---|
| 1 | As **Ravi**, give **Asha** *Manage access* (but not *Approve orders*) and save. | Asha becomes a delegated administrator and sees Access management in the sidebar. |
| 2 | Sign in as **Asha**; open **Dev**'s access; hover or tab to *Approve orders*. | The switch is disabled with a lock and the reason "You can only grant permissions you hold". |
| 3 | Open **Meera**'s access (an Organization admin). | Every control is disabled; banner "Only organization admins can change an administrator's access". |
| 4 | Open **your own** access (Asha). | Every control is disabled; banner "You cannot change your own access". |
| 5 | Sign in as **Ravi**; open **your own** access and try the role selector. Then change **Meera** to Member. | Your own role and access are read-only ("You cannot change your own access"). Changing Meera works because Ravi remains an Organization admin. |

Result: ☐ Pass ☐ Fail — Tester: ______ Date: ______
