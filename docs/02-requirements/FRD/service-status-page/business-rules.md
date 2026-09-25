# Business rules — Interim Service Status Page

These rules come from the decisions recorded in [open-decisions.md](../../../01-business/roadmap/open-decisions.md) on 2026-09-25. There is no backend code for them yet. Rules marked **(to confirm)** mirror the existing SAML design and must be confirmed when the FRD is approved.

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-PRT-001.1 | Status and incidents are posted **manually** by platform administrators. | backend | [C20](../../../01-business/roadmap/open-decisions.md#c20) |
| BR-PRT-001.2 | Customers can view per-product status and incidents. Who sees what depends on the visibility field, which is Not specified. | backend + frontend | [C20](../../../01-business/roadmap/open-decisions.md#c20) |
| BR-PRT-001.3 | The whole interim page is switched on or off by **one configuration setting**. | backend + frontend | [C20](../../../01-business/roadmap/open-decisions.md#c20) |
| BR-PRT-001.4 | In sprint 2027.1.1 per-product status switches to Health Monitoring (10.04). In sprint 2027.1.3 incidents switch to Incident & Problem Management (12.04), and the interim page is removed after that sprint. | plan | [C20](../../../01-business/roadmap/open-decisions.md#c20) |
