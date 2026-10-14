# UI requirements — SLA management (REQ-SUP-002)

| Screen | Route | Roles | Screen requirement |
|---|---|---|---|
| SLA settings tab (targets by priority; business hours, warning point, alert list) | `/admin/support/tickets?tab=sla` (Support tabs `Tickets \| SLA settings`) | `MANAGE_SUPPORT_TICKETS` | [sla-management.md](../../../05-ui/screen-requirements/sla-management.md#1-sla-settings-tab) |
| Support tickets: first-response and resolution SLA chips, breach flag, SLA filter | `/admin/support/tickets` (Tickets tab) | `MANAGE_SUPPORT_TICKETS` | [sla-management.md](../../../05-ui/screen-requirements/sla-management.md#2-support-tickets-sla-chips-and-filter) |
| My tickets: "we aim to respond by / resolve by" | My tickets | Any authenticated customer | [sla-management.md](../../../05-ui/screen-requirements/sla-management.md#3-my-tickets-deadlines) |

The ticket screens themselves are described in [ticket-management](../ticket-management/ui-requirements.md).
