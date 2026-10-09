# Screen: Tool sync monitor

**Requirement:** [REQ-INT-003](../../02-requirements/FRD/platform-tool-sync/requirement.md) (In Review). Built with the platform-side phase of the [phase plan](../../09-integrations/platform-tool-sync-plan.md).

1. **Location.** Platform area → Integrations → *Platform events* → tab **Tool sync**; needs `MANAGE_INTEGRATIONS`.
2. **Table.** One row per organization and product: organization, product, tenant status (Pending, Ready, Failed, Suspended), schema version, last successful sync, last error (shortened, full text in the details), attempts, version lag (platform version minus tool version, per aggregate type in the details).
3. **Filters.** Product, status, organization search, "only with errors".
4. **Details panel.** Recent deliveries (event type, aggregate, version, result, time), per aggregate type counts and versions, last error text.
5. **Actions.** *Retry failed* and *Replay current state* (all or chosen aggregate types), each with a confirmation naming the organization and product; both are audited.
6. **Organization detail.** A read-only *Products and sync* panel with the same status for that organization.
7. English and Spanish; keyboard and screen-reader accessible; the empty state says that no organization has a tool subscription yet.
