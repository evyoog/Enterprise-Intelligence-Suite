# UI requirements — Service instances (REQ-SRM-001)

| Screen | Route | Roles | Screen requirement |
|---|---|---|---|
| Instances tab: summary, instance table, reporter keys (Platform → Service status) | `/status?tab=instances` (admin tabs `Status \| Manage \| Instances \| Incidents \| Problems`) | `MANAGE_SERVICE_STATUS` | [service-instances.md](../../../05-ui/screen-requirements/service-instances.md#1-instances-tab-platform-administrator) |
| Change an instance by hand (dialog on the Instances tab) | `/status?tab=instances` | `MANAGE_SERVICE_STATUS` | [service-instances.md](../../../05-ui/screen-requirements/service-instances.md#change-by-hand-dialog) |
| Create and revoke reporter keys (section and dialogs on the Instances tab) | `/status?tab=instances` | `MANAGE_SERVICE_STATUS` | [service-instances.md](../../../05-ui/screen-requirements/service-instances.md#reporter-keys) |
| Service instances (organization, read-only) | `/organization/service-instances` (Organization → Service instances) | Organization administrator (`MANAGE_ORGANIZATION`) | [service-instances.md](../../../05-ui/screen-requirements/service-instances.md#2-service-instances-organization-administrator) |
| Status page: "Based on N reporting instances (x down, y degraded)" | `/status` | Every signed-in user | [service-instances.md](../../../05-ui/screen-requirements/service-instances.md#3-status-page-caption) |

The status page tabs are shared with [incident-problem-management](../incident-problem-management/ui-requirements.md) (Incidents and Problems tabs).
