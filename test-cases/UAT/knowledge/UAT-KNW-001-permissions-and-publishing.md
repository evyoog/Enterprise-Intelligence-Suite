# UAT-KNW-001 — Knowledge roles, drafting, review, publishing and versions

| Step | Who | Action | Expected |
|---|---|---|---|
| 1 | Priya | Roles & permissions: create platform role `KNOWLEDGE_WRITER` with *Knowledge contributor* (`KNOWLEDGE_CONTRIBUTE`); in Keycloak give Karthik the client role `KNOWLEDGE_WRITER`. Remove and re-add the permission once. | Audit log shows the role changes. |
| 2 | Asha | Open the app; try `/knowledge-management`. | No Knowledge Management menu item; the page says she has no permission. |
| 3 | Karthik | Knowledge Management → Content → New content: type Product guide, title "Create a purchase order", product Varthan.ai, module Purchase; add a heading, steps and a code block; Save draft. | Saved as Draft; no Publish or Delete buttons. |
| 4 | Karthik | Preview. | The article opens as readers will see it, with a Preview banner. |
| 5 | Karthik | Submit for review; try to edit. | In review; editing is read-only with a message. |
| 6 | Priya | Return to draft with a comment; Karthik sees the comment, fixes, resubmits; Priya approves and publishes (minor). | Live version 1.0; readers see it. |
| 7 | Karthik | Edit the published guide and submit; Priya approves and publishes as major. | Readers saw 1.0 until publish, then 2.0. |
| 8 | Priya | Versions → Compare 1.0 with draft; Restore 1.0. | Changed blocks highlighted; a new draft holds 1.0 content; history unchanged. |
| 9 | Priya | Schedule a publish 5 minutes ahead. | Scheduled; published by itself at the time. |
| 10 | Priya | Deprecate, then Archive. | Deprecated shows a banner to readers; archived disappears from readers and search. |
