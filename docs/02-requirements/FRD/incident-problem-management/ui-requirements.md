# UI requirements — Incident and problem management (REQ-SUP-003)

| Screen | Route | Roles | Screen requirement |
|---|---|---|---|
| Incidents tab: list, new and edit dialog, linked tickets | `/status?tab=incidents` (admin tabs `Status \| Manage \| Instances \| Incidents \| Problems`) | `MANAGE_SERVICE_STATUS` | [incident-problem-management.md](../../../05-ui/screen-requirements/incident-problem-management.md#1-incidents-tab) |
| Problems tab: list, new problem, problem detail with linked incidents and corrective actions | `/status?tab=problems` | `MANAGE_SERVICE_STATUS` | [incident-problem-management.md](../../../05-ui/screen-requirements/incident-problem-management.md#2-problems-tab) |
| Manage tab: product status only; **no incident form** | `/status?tab=manage` | `MANAGE_SERVICE_STATUS` | [incident-problem-management.md](../../../05-ui/screen-requirements/incident-problem-management.md#3-manage-tab-no-longer-posts-incidents) |
| Status page, customer view: public incident fields only | `/status` | Every signed-in user | [service-status-page ui-requirements](../service-status-page/ui-requirements.md) |

The status page tabs are shared with [service-instances](../service-instances/ui-requirements.md) (Instances tab).
