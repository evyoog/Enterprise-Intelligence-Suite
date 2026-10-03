# REQ-INT-002 — Platform events

**Status:** Draft
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved
**Decision:** [C62](../../../01-business/roadmap/open-decisions.md#c62) (answer to D13, option A)
**Built:** 2026-10-03 at the product owner's request ("build the code"), with the engineering defaults below. Test cases: [TESTPLAN-INT-002](../../../../test-cases/functional/event-platform/TESTPLAN-INT-002.md).

| Field | Value |
|---|---|
| Sprint | [2026.4.2](../../../01-business/roadmap/sprints/SPRINT-2026.4.2.md) (13a moved here by [C31](../../../01-business/roadmap/open-decisions.md#c31)) |
| Requirement ID | REQ-INT-002 |
| Application | [13 Integration & API Platform](../../../01-business/roadmap/applications/13-integration-api-platform.md) |
| Application code | `APP-INT` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 13.03.01.01 | Publish event | Yes (.1, .8) |
| 13.03.01.02 | Subscribe to event | Yes, as in-application handlers (.3, .4). External subscribers (webhooks, 13.04) are D19 — not covered |
| 13.03.01.03 | Route event | Yes: by event type to the handlers that declare it (.3) |
| 13.03.01.04 | Retry event | Yes: automatic retries and admin retry (.3, .6) |
| 13.03.01.05 | Replay event | No. Replaying DELIVERED events is Not specified |

## Summary
Every business change that other features react to writes an event to an **outbox table in the same database transaction** as the change. A background dispatcher delivers pending events, in order per aggregate, to the in-application handlers registered for their type, with retries. Platform administrators can see events and retry failed ones. No external message broker is used ([C62](../../../01-business/roadmap/open-decisions.md#c62)).

## Actors
- **Platform features** (orders, subscriptions, billing, seats, reminders): publish events.
- **Handlers** (in-application): receive events. None exist yet beyond tests; provisioning ([REQ-ORD-002](../provisioning-contract/requirement.md)) and webhooks (D19) are the first planned handlers.
- **Platform administrator** (`MANAGE_INTEGRATIONS`): lists events, opens one, retries a FAILED event.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-INT-002.1 | **Outbox:** when a business change happens, its event is written to `outbox_event` **in the same database transaction** as the change. If the change is rolled back, no event exists. | Must |
| REQ-INT-002.2 | **Event record:** event ID (unique UUID), event type, aggregate type and ID (for example `Order` 42), occurred-at time, payload (JSON — **no secrets and no card data**, [BR-BIL-001](../../../03-business-rules/BR-BIL-001-no-raw-card-data.md)), status (PENDING, DELIVERED, FAILED), attempt count, next-attempt time, last error. | Must |
| REQ-INT-002.3 | **Dispatcher:** a background job picks up PENDING events whose next-attempt time has come, in order of occurrence, and delivers each to the handlers registered for its type. Delivery is **at least once**. A failure is retried with an increasing delay; after the maximum attempts the event is FAILED. An event with no handler is DELIVERED. | Must |
| REQ-INT-002.4 | **Idempotent handlers:** each handler's processed event IDs are recorded (`event_handler_receipt`); a repeat delivery is ignored by that handler. | Must |
| REQ-INT-002.5 | **Ordering:** events for the same aggregate are delivered in the order they occurred. A later event waits while an earlier event of the same aggregate is not yet DELIVERED. | Must |
| REQ-INT-002.6 | **Admin view:** a platform administrator can list events (filter by type, status, date), open one, and **retry** a FAILED event (back to PENDING, attempts reset). | Should |
| REQ-INT-002.7 | **Retention:** DELIVERED events older than the retention period are removed (period: Open question 2). | Should |
| REQ-INT-002.8 | **First events published:** see the [event catalogue](#event-catalogue). | Must |

## Event catalogue
Names reuse the workbook's Events column where one matches (`CheckoutCompleted`, `PaymentAuthorized`); the others are **new names** introduced here.

| Event type | Aggregate | Published when | Source |
|---|---|---|---|
| `CheckoutCompleted` | Cart | A cart checkout creates its invoice or orders ([REQ-MKT-003](../cart-checkout/requirement.md)) | Workbook |
| `OrderApproved` | Order | An organization admin approves an order ([REQ-ORD-001](../order-lifecycle/requirement.md)) | New |
| `SubscriptionCreated` | Subscription | A subscription starts (individual, organization, cart) | New |
| `SubscriptionChanged` | Subscription | The plan changes | New |
| `SubscriptionSuspended` | Subscription | Suspended | New |
| `SubscriptionResumed` | Subscription | Reactivated | New |
| `SubscriptionCancelled` | Subscription | Cancelled | New |
| `SubscriptionRenewed` | Subscription | Renewed (manually or by auto-renew, [REQ-SUB-004](../renewal-reminders/requirement.md)) | New |
| `InvoiceGenerated` | Invoice | An invoice is issued | New |
| `PaymentAuthorized` | Payment | A payment is captured (online or recorded offline) | Workbook (used for "payment captured") |
| `PaymentFailed` | Payment | An online payment fails | New |
| `SeatsChanged` | Subscription | Seat quantity changes ([REQ-SUB-003](../subscription-seats/requirement.md)) | New |
| `RenewalReminderSent` | Subscription | A renewal reminder email is sent ([REQ-SUB-004](../renewal-reminders/requirement.md)) | New |

The workbook's `ProvisioningStarted` belongs to [REQ-ORD-002](../provisioning-contract/requirement.md) and is not published until that FRD is built.

## Engineering defaults (built 2026-10-03, until the open questions are answered)
| Topic | Default | Where |
|---|---|---|
| Retries (OQ 1) | 10 attempts; delay doubles from 30 seconds (30 s, 1 min, 2 min, …), capped at 6 hours | `OutboxDispatcher` |
| Retention (OQ 2) | DELIVERED events older than 30 days are removed by a daily job | `OutboxRetentionJob` |
| Failure notification (OQ 3) | None; FAILED events are visible in the admin view | — |
| Dispatcher interval | Every 5 seconds, 100 events per run (`app.events.dispatcher.*`) | `application.yml` |
| Permission | New platform permission `MANAGE_INTEGRATIONS` (also used by REQ-INT-001's admin usage view) | `RbacSeeder` |

## Out of scope
- External delivery (webhooks, D19) and any message broker ([C62](../../../01-business/roadmap/open-decisions.md#c62)).
- Replaying delivered events (13.03.01.05).
- Moving existing synchronous work (invoice generation, notifications) onto handlers: those keep running inside their own transactions; the events describe what happened.

## Dependencies
- PostgreSQL (the same database), Spring scheduling.
- Publishers: order lifecycle, subscription lifecycle, billing, cart, seats, renewal reminders.

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement, business rules, workflow, acceptance criteria | this folder |
| Screens | [ui-requirements.md](ui-requirements.md) → [platform-events.md](../../../05-ui/screen-requirements/platform-events.md) |
| API | [api-requirements.md](api-requirements.md) → [event-platform.md](../../../06-api/api-requirements/event-platform.md) |
| Data model | [event-platform.md](../../../07-database/data-model/event-platform.md) |
| Cross-feature flow | [platform-events.md](../../../04-workflows/platform-events.md) |

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | Maximum retry attempts and delay schedule (proposed default — confirm: 10 attempts, doubling from 30 seconds). | No — confirm in review |
| 2 | Retention period for delivered events (built default: 30 days — confirm). | No — confirm in review |
| 3 | Should failed events notify an administrator (email or in-app)? | No — confirm in review |
