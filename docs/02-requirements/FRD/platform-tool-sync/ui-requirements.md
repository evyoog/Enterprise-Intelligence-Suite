# UI requirements — Platform ↔ tool synchronization

Screen specs: [tool-sync-monitor.md](../../../05-ui/screen-requirements/tool-sync-monitor.md) (written with the platform-side phase). Summary:

- **Where:** Platform area → Integrations → *Platform events*, new tab **Tool sync** (permission `MANAGE_INTEGRATIONS`).
- **Table:** organization, product, tenant status, schema version, last successful sync, last error, attempts, version lag; filters for product, status, organization; search by organization.
- **Row actions:** *Retry* (failed deliveries), *Replay current state* (one aggregate type or all), both with a confirmation and an audit entry.
- **Organization detail (platform admin):** a read-only "Products and sync" panel showing the same status for that organization.
- **Tool screens (every tool):** shared fields are shown as managed by the platform; a failed write-through shows "Could not reach the platform, nothing was saved. Try again." A denied entitlement shows the reason (subscription ended, no product access, not a member, organization inactive, not provisioned).
- English and Spanish; accessible (labels, focus, contrast), as all EIS screens.
