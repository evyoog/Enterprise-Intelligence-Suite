# REQ-SRM-001 — Service instances

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-14 ([C101](../../../01-business/roadmap/open-decisions.md#c101), SR-1 to SR-5). The engineering defaults listed under [Engineering defaults](#engineering-defaults) are applied "to confirm"; none blocks use of the feature.
**Decision:** [C57](../../../01-business/roadmap/open-decisions.md#c57) (answer to D7, option A), [C101](../../../01-business/roadmap/open-decisions.md#c101) (SR-1 to SR-5). Related: [C56](../../../01-business/roadmap/open-decisions.md#c56) (provisioning contract), [C20](../../../01-business/roadmap/open-decisions.md#c20) (interim status page).
**Built:** 2026-10-14 (migration [V041](../../../../database/migrations/V041__service_instances_sla_incidents_problems.sql), apply by hand: Flyway is disabled). Test cases: [TESTPLAN-SRM-001](../../../../test-cases/functional/service-instances/TESTPLAN-SRM-001.md).

| Field | Value |
|---|---|
| Sprint | [2027.1.2](../../../01-business/roadmap/sprints/SPRINT-2027.1.2.md) |
| Requirement ID | REQ-SRM-001 |
| Application | [10 Service & Resource Management](../../../01-business/roadmap/applications/10-service-resource-management.md) |
| Application code | `APP-SRM` |
| Priority | P1 / stretch for the application under [C4](../../../01-business/roadmap/open-decisions.md#c4)/[C5](../../../01-business/roadmap/open-decisions.md#c5); built in this sprint on the product owner's answers ([C101](../../../01-business/roadmap/open-decisions.md#c101)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 10.01.01.01 | Create service instance | Yes: one instance per subscription, created automatically when the subscription becomes ACTIVE (.1, .2). Provisioning itself ([REQ-ORD-002](../provisioning-contract/requirement.md)) is not built |
| 10.01.01.02–.07 | Configure, start, stop, restart, scale, delete service | **No. Not specified.** EIS does not run the products' infrastructure ([C101](../../../01-business/roadmap/open-decisions.md#c101) SR-1 A): starting, stopping and scaling servers is not built |
| 10.04.01.01 | Collect health status | Yes: the product reports it (.3 to .5) |
| 10.04.01.04 | View health | Yes: platform administrators see every instance, organization administrators their own (.7, .8); it feeds the status page (.9) |
| 10.04.01.02 / .03 | Detect anomaly; Create alert | **No. Not specified.** Automatic anomaly detection and alerts are not built |
| 10.04.02 | Usage monitoring (collect metrics, view usage, set threshold) | **No. Not specified** |
| 10.02, 10.03 | Resource management; configuration management | **No. Not specified** |

## Summary
A **service instance** is one subscription's tenant in a hosted product. EIS keeps **one instance per subscription**, created when the subscription becomes ACTIVE and kept in step with it afterwards. EIS does not start, stop or scale anything; it only **records the health the products report** ([C101](../../../01-business/roadmap/open-decisions.md#c101) SR-1 A). Each product reports **Healthy, Degraded or Down, about every minute**, through a **reporter key** a platform administrator creates for it. Organization administrators see their own organization's instances; platform administrators see all of them and can set an instance's status and health **by hand** when a product cannot report (SR-5 B). The status page's **per-product status is derived from the instances** (SR-3 B): a product is **Down** (Major outage) only if more than half of its reporting instances are Down.

## Actors
- **Product** (a hosted product acting as a system): reports instance health with its reporter key.
- **Platform administrator** (`MANAGE_SERVICE_STATUS`): sees all instances, sets one by hand, creates and revokes reporter keys.
- **Organization administrator** (organization permission `MANAGE_ORGANIZATION`): sees the organization's own instances, read-only.
- **Customer** (individual or member): does not see instances ([SR-4 A](../../../01-business/roadmap/open-decisions.md#c101) names only organization administrators and platform administrators); sees the derived product status on the status page.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-SRM-001.1 | Each subscription has **at most one service instance**, created when the subscription becomes **ACTIVE** (a subscription still waiting for payment has none). The instance records the product and the owner (organization, or the individual customer). | Must |
| REQ-SRM-001.2 | **Instance status** is `PROVISIONING` until the product's first report, then `ACTIVE`; `SUSPENDED` and `DEPROVISIONED` follow the subscription (suspended; cancelled or expired); `FAILED` is set by a platform administrator or reported by the product. The instance follows every subscription change: from the subscription flow at once and from a reconcile job every 15 minutes as a safety net. | Must |
| REQ-SRM-001.3 | **Health** is `HEALTHY`, `DEGRADED` or `DOWN`, **reported by the product about every minute** ([C101](../../../01-business/roadmap/open-decisions.md#c101) SR-2 A). `UNKNOWN` is the value before any report and the value an instance counts as when its report is stale. A report can also carry a short message, the tenant reference and the access URL. | Must |
| REQ-SRM-001.4 | A product reports with a **reporter key** (`X-Reporter-Key`): a platform administrator creates one **per product**; the key is **shown once**, only a **hash** is stored, and it can be **revoked**. A product can report **only its own instances**. The call carries no user token, so the key is the credential. Up to 200 reports per call; each report is accepted or rejected on its own. | Must |
| REQ-SRM-001.5 | A report older than **5 minutes** is "no recent report": it is shown as such and **left out** of the status-page combination. | Must |
| REQ-SRM-001.6 | A platform administrator can **set an instance's status and/or health by hand**, with a **required note** ([C101](../../../01-business/roadmap/open-decisions.md#c101) SR-5 B). A hand-set health counts as a current report. The change is audited. | Must |
| REQ-SRM-001.7 | **Platform administrators see every instance** (SR-4 A), with a summary (total, healthy, degraded, down, unknown, no recent report) and filters by product, status and health, and manage reporter keys (list, create, revoke). | Must |
| REQ-SRM-001.8 | An **organization administrator sees the organization's own instances**, read-only, without deprovisioned ones (SR-4 A). Any other organization member is refused (403). Individual customers have no instance screen. | Must |
| REQ-SRM-001.9 | **Per-product status on the status page is derived from the instances** that reported in the last 5 minutes (SR-3 B): more than half Down = Major outage; at least one Down = Partial outage; at least one Degraded = Degraded; otherwise Operational. A platform administrator's manual **Maintenance wins**. A product with **no recently reporting instance keeps its manually set status**. The status page says how many instances the status is based on (REQ-PRT-001, see [service-status-page](../service-status-page/requirement.md)). | Must |
| REQ-SRM-001.10 | Hand changes and reporter-key creation and revocation are **audited** (`SERVICE_INSTANCE_CHANGED`, `SERVICE_REPORTER_KEY_CREATED`, `SERVICE_REPORTER_KEY_REVOKED`). Reports from a product (about every minute) and automatic status follow-ups are not audited; the instance shows who changed it last (`SYSTEM`, `PRODUCT` or `ADMIN`). | Must |

## Engineering defaults
Applied to make C101 work; **to confirm** with the product owner. None changes an answer of C101.

| Topic | Default | Where |
|---|---|---|
| One instance per subscription | Created when the subscription is ACTIVE; none for a pending subscription | `ServiceInstanceService#syncFromSubscription`, hook in `SubscriptionService#publish` |
| Status transitions | PROVISIONING until the first report; a re-activated (previously suspended or deprovisioned) subscription goes back to PROVISIONING, or ACTIVE if the instance has reported before | `syncFromSubscription` |
| Provisioning not built | Because provisioning ([REQ-ORD-002](../provisioning-contract/requirement.md)) is not built, products report through a reporter key and **do not need a "provisioned" reply** to create an instance | — |
| Reports for a suspended or deprovisioned instance | Rejected ("the instance is suspended") | `ServiceInstanceService#apply` |
| A product may set status | In a report, only `PROVISIONING`, `ACTIVE` or `FAILED` | `ServiceInstanceService#apply` |
| FAILED counts as Down | For the status-page combination a `FAILED` instance counts as Down | `ServiceInstanceService#derivedStatus` |
| Which instances count | `ACTIVE` or `FAILED` instances with a recent report and a health other than `UNKNOWN`; `PROVISIONING`, `SUSPENDED` and `DEPROVISIONED` are left out | `derivedStatus` |
| Individual customers | Do not see instances; their instances are visible to platform administrators | SR-4 A |
| Key format | `eisrk_` + 43 URL-safe characters; the first 12 characters are kept in the clear as a prefix to recognise the key; SHA-256 hash stored | `ServiceInstanceService#createKey` |

## Out of scope
Starting, stopping, restarting, scaling, configuring and deleting a service; resource and configuration management (10.02, 10.03); automatic anomaly detection and alerts (10.04.01.02, .03); usage monitoring (10.04.02); the provisioning contract itself ([REQ-ORD-002](../provisioning-contract/requirement.md)); customer-facing instance screens for individual customers.

## Dependencies
Subscriptions ([REQ-SUB-001](../subscription-lifecycle/requirement.md)), the product catalog, organizations and the organization permission `MANAGE_ORGANIZATION`, the status page ([REQ-PRT-001](../service-status-page/requirement.md)), the platform permission `MANAGE_SERVICE_STATUS`, audit. Provisioning ([REQ-ORD-002](../provisioning-contract/requirement.md)) will later also supply the tenant reference and access URL at creation; today a product sends them in its reports.

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement, business rules, workflow, acceptance criteria | this folder |
| Screens | [ui-requirements.md](ui-requirements.md) → [service-instances.md](../../../05-ui/screen-requirements/service-instances.md) |
| API | [api-requirements.md](api-requirements.md) → [service-instances.md](../../../06-api/api-requirements/service-instances.md) |
| Data model | [service-instances-sla-incidents.md](../../../07-database/data-model/service-instances-sla-incidents.md) (V041) |
| Test cases | [TESTPLAN-SRM-001](../../../../test-cases/functional/service-instances/TESTPLAN-SRM-001.md) |

## Decisions ([C101](../../../01-business/roadmap/open-decisions.md#c101), product owner, 2026-10-14)
The open questions of the draft are answered. None remains open.

| # | Question | Answer |
|---|---|---|
| 1 | ~~Health value set and reporting interval~~ | **SR-2 A:** Healthy, Degraded or Down, reported every minute ([REQ-SRM-001.3](#functional-requirements)) |
| 2 | ~~How instance health combines into one per-product status~~ | **SR-3 B:** a product is Down only if more than half of its instances are Down; the engineering default for the in-between values is in [REQ-SRM-001.9](#functional-requirements) |
| 3 | ~~Who sees service instances~~ | **SR-4 A:** organization administrators see their own organization's instances; platform administrators see all ([REQ-SRM-001.7, .8](#functional-requirements)) |
| 4 | ~~Status transitions and who causes each~~ | **SR-5 B:** the status is changed by the products' reports and also by platform administrators, by hand ([REQ-SRM-001.2, .6](#functional-requirements)). Because provisioning is not built, transitions do not depend on REQ-ORD-002 replies |
| 5 | ~~How much of the 10.01 life cycle is built~~ | **SR-1 A:** only service instances and their health, as reported by the products; starting, stopping and scaling is not built |

## Open points
1. **Provisioning will change who creates the instance's tenant reference.** Until [REQ-ORD-002](../provisioning-contract/requirement.md) is built the product sends the tenant reference and access URL in its health reports. Confirm that this stays acceptable.
2. **Individual customers do not see their instance** (SR-4 A names only organization administrators and platform administrators). Confirm.
3. **The 5-minute "no recent report" window and FAILED counting as Down** are engineering defaults.
4. **Not specified:** anomaly detection and alerts (10.04.01.02, .03), usage monitoring (10.04.02), resource and configuration management (10.02, 10.03), instance actions such as start, stop and scale.
