# Screens: Incidents and problems

| Field | Value |
|---|---|
| Requirement | [REQ-SUP-003](../../02-requirements/FRD/incident-problem-management/requirement.md); business rules [BR-INC-001 to BR-INC-019](../../02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../01-business/roadmap/open-decisions.md#c101) (IC-1 A, IC-2 A) |
| Route | **Platform → Service status** (`/status`), admin tabs `Status \| Manage \| Instances \| Incidents \| Problems` (query parameter `?tab=`): `/status?tab=incidents`, `/status?tab=problems` |
| Permission | Platform permission `MANAGE_SERVICE_STATUS`. Without it the tabs are not shown; the API returns 403 |
| Built | Sprint 2027.1.2: `frontend/src/pages/admin/AdminIncidentsPage.tsx`, `frontend/src/pages/admin/AdminProblemsPage.tsx`, `frontend/src/pages/ServiceStatusRoutePage.tsx` (tabs), `frontend/src/pages/admin/ServiceStatusAdminPage.tsx` (Manage tab), `frontend/src/api/serviceStatusApi.ts`, `frontend/src/api/problemsApi.ts` |

Shared presentation rules: unified MUI theme, light and dark mode; phases, severities and statuses are text chips, never colour alone. Strings are under `incidents.*`, `problems.*` and `serviceTabs.*` in `frontend/src/i18n/locales/{en,es}.json`.

## 1. Incidents tab
Header **"Incidents"** — "One record for the status page and for support. Customers see only the public fields." and a **New incident** button.

- **Phase filter** (label "Phase"): **Not closed** (default), **All phases**, and each phase (Logged, Investigating, Resolved, Closed).
- **Table:** Title, Product, Severity (Minor, Major, Critical), Phase, Started, Ended ("Ongoing" when open), Assignee ("Unassigned"), Tickets (count), Problem (title, or "None"), Actions (**Edit**, or **View** for a closed incident; accessible names "Edit incident {title}" / "View incident {title}").

### New and edit dialog
Two groups, so an administrator can see what customers see:

| Group | Fields |
|---|---|
| **"Shown on the status page"** — "Customers see these fields." | Product (required, active products), Title (required, up to 200), Message (required, up to 4000), Started (required), Ended (empty = open; hint "Leave empty to use the current time." when resolving) |
| **"Internal only — never shown to customers"** — "Only platform administrators see these fields." | Severity, Phase, Resolution summary (up to 2000; "Required to mark the incident resolved."), Internal notes (up to 4000), Assignee (customer ID, "The customer ID of the person handling it."; "Currently assigned to {name}."), "Part of problem:" (read-only) |

| Rule on screen | Message |
|---|---|
| Product, title, message and start are required | "Choose a product." / "Enter a title." / "Enter the message customers will see." / "Set the start time." |
| End not before start | "The end time must not be before the start time." |
| Resolved needs a summary | "Write a resolution summary before marking the incident resolved." |
| Logged or investigating has no end time | "An incident that is logged or investigating has no end time. Mark it resolved instead." |
| Assignee is a whole customer id | "Enter a whole customer ID." |

**Phase select** offers only the phases allowed from the current one ("Only the phases allowed from here are offered."): from Logged: Logged, Investigating, Resolved; from Investigating: Investigating, Resolved; from Resolved: Resolved, Closed, and reopen (Investigating, "(reopen)"). A **Closed** incident opens read-only ("This incident is closed (since {date}) and cannot be changed."). Buttons **Create incident** / **Save** / **Cancel** / **Close**; toasts `Incident "{title}" created` / `saved`; the server's refusal is shown in the dialog.

### Linked support tickets
A section in the edit dialog: **"Linked support tickets"** — "Tickets from customers affected by this incident." Each linked ticket shows its id, subject, status and priority with **Unlink** ("Unlink ticket {id}"); **"No tickets linked yet."** To link: **"Add a support ticket"** (a picker of tickets, or a **Support ticket ID** field when the ticket list is not available: "Enter a whole ticket ID.") and **Link ticket**. Not available on a closed incident.

### States
Empty "No incidents" — "Log an incident to post it on the status page and track it with support." (filtered: "No incidents in this phase. Choose another phase to see more."); load error "Incidents unavailable" / "Could not load the incidents." with a retry; save error "Could not save the incident."

## 2. Problems tab
Header **"Problems"** — "A problem ties several incidents to one root cause and tracks the corrective actions." and a **New problem** button.

- **Table:** Title, Status (Open, Root cause identified, Resolved, Closed), Incidents (count), Actions ("{open} open / {total}"), Created, **Manage** (**View** for a closed problem; accessible name "Open problem {title}").
- **New problem** dialog: **Title** (required, "Enter a title."), **Description**, **Incidents to link (optional)** — only incidents not yet in a problem are listed ("An incident can belong to only one problem, so only incidents not yet in a problem are listed."; "Every incident already belongs to a problem."). **Create problem**; toast `Problem "{title}" created`.

### Problem detail
Title "Problem: {title}", with sections:

| Section | Content |
|---|---|
| **Details** | Title, Description, **Root cause** ("Needed before the problem can be marked root cause identified or resolved."); **Save details** |
| **Status** | "Open, root cause identified, resolved, then closed." **Move to** (select) and **Change status**. A blocked move is explained under "Why some moves are not available": "Cannot move to "{status}": write the root cause first." / "…only a resolved problem can be closed." / "…{n} corrective actions are still open." Toast "Status changed to {status}" |
| **Linked incidents** | The incidents (title, product, phase, severity, start) with **Remove** ("Remove incident {title} from this problem"); **Add an incident** (only free incidents) and **Link incident**; "No incidents linked yet." |
| **Corrective actions** | "{open} open of {total}. Every action must be done before the problem can be closed." Each action: description, owner ("No owner"), due date ("No due date"), a **Done** check ("Done: {description}", "Done {date}"), **Edit**, **Delete** (confirmation "Delete this action?"). **New action** (required, "Describe the action."), **Owner (optional)**, **Due date (optional)**, **Add action** |

A **Closed** problem opens read-only ("This problem is closed (since {date}) and cannot be changed."). States: empty "No problems" — "Create a problem when several incidents share one root cause."; load error "Problems unavailable" / "Could not load the problems." with a retry.

## 3. Manage tab: no longer posts incidents
The **Manage** tab (what was the admin status screen at `/admin/service-status`) keeps **only the product status**: a row per product with the current status, **New status**, **Note (optional)** and **Save**. Its subtitle reads "Post each product's status. Customers see it on the service status page. Incidents are managed on the Incidents tab." **The incident form of REQ-PRT-001 is gone** from this tab; incidents are posted on the Incidents tab (one record). The customer view of the status page is unchanged: incident public fields for purchased products only.

## Accessibility and i18n
Tables have accessible names; every row action names its incident or problem; dialogs are labelled by their title; each field has a label with its error as helper text; chips are text; axe tests on both tabs. All strings are in `en.json` and `es.json`.

## API used
[incident-problem-management.md](../../06-api/api-requirements/incident-problem-management.md): the incident and problem endpoints under `/admin/service-status/**`; `PUT /admin/service-status/products/{productId}` (Manage tab).
