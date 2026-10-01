# REQ-ORD-002 — Provisioning contract (EIS ↔ hosted products)

**Status:** Draft (stub — documents only; built in its own sprint)
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved. The contract shape must be confirmed with every hosted product team first.
**Decision:** [C56](../../../01-business/roadmap/open-decisions.md#c56) (answer to D6, option B)

| Field | Value |
|---|---|
| Sprint | [2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md) |
| Requirement ID | REQ-ORD-002 |
| Application | [09 Order & Provisioning Management](../../../01-business/roadmap/applications/09-order-provisioning-management.md) |
| Application code | `APP-ORD` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 09.02.01.01 | Provision service | Yes — "provision requested" message |
| 09.02.01.02 | Configure service | No — Not specified |
| 09.02.01.03 | Activate service | Yes — "provisioned" reply |
| 09.02.01.04 | Suspend service | Yes — "suspend" and "resume" messages |
| 09.02.01.05 | Deprovision service | Yes — "deprovision requested" message |

## Summary
When a subscription starts, is suspended, is resumed or is cancelled, EIS notifies the hosted product (Valam.ai, Varthan.ai, Thittam.ai, Thiran.ai, Yukth.ai, Tharav.ai). The product creates or changes the customer's tenant and reports the result back. Users keep reaching the product through the existing internal sign-in bridge. Each hosted product team implements the contract.

## Proposed contract — **to confirm with hosted product teams**
Nothing in this section is decided. It is a starting proposal for review.

### Messages from EIS to the product
| Message | When | Carries |
|---|---|---|
| Provision requested | Subscription starts (individual subscribe, approved organization order) | subscription ID, customer or organization, product, plan, region |
| Suspend | Subscription suspended | same |
| Resume | Suspended subscription resumed | same |
| Deprovision requested | Subscription cancelled or expired | same |

### Replies from the product to EIS
| Reply | Carries |
|---|---|
| Provisioned | subscription ID, product tenant reference, access URL |
| Failed | subscription ID, reason |
| Health status (periodic) | subscription ID or tenant reference, health value, reported time — consumed by [REQ-SRM-001](../service-instances/requirement.md) |

### Authentication
The existing internal shared secret (`INTERNAL_SSO_SHARED_SECRET`, held in `config/secrets.env` per [BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md)). Whether each product gets its own secret is Not specified.

### Delivery, retries and ordering
- **Delivery mechanism:** pending **D13** (events) and **D19** (webhooks). Not decided.
- **Retries:** Not specified.
- **Ordering** (for example a suspend arriving before the provisioned reply): Not specified.
- **Timeouts** (when a missing reply counts as failed): Not specified.

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | Delivery mechanism: events (D13) or webhooks (D19)? | Yes |
| 2 | Retries, ordering and timeouts. | Yes |
| 3 | Confirmation from **each** hosted product team that they can implement the contract, and by when. | Yes |
| 4 | Message fields: is "subscription, customer or organization, product, plan, region" complete? Does a product need users, seats (D14) or configuration? | Yes |
| 5 | What happens to a subscription whose provisioning failed: does it stay ACTIVE, or get a new state? Does billing change? | Yes |
| 6 | One shared secret for every product, or one per product? | No — confirm in review |
| 7 | Does "Configure service" (09.02.01.02) belong in this contract? | No — confirm in review |

## Not yet written
`business-rules.md`, `workflow.md`, `acceptance-criteria.md`, `ui-requirements.md` and `api-requirements.md` are written when this FRD is scoped for its sprint, after the open questions above are answered.
