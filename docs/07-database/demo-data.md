# Demo data — `DemoDataSeeder`

Decision: [C54](../01-business/roadmap/open-decisions.md#c54).

**Never run this against a database holding real customer data.** It is a Spring `ApplicationRunner` gated `@Profile("!test")` (it never runs under this repository's own automated test suite, which always activates the `test` profile), meant for local/dev and UAT environments only, per [`database/seed/README.md`](../../database/seed/README.md)'s own rule. It runs on every startup where the `test` profile is not active, and every write it makes is idempotent — safe to restart the backend repeatedly without duplicating anything.

## What it does NOT do
It never creates an organization, a customer registration, or a subscription itself. Those represent a real signup/purchase decision and are never fabricated. It only **enriches** rows that already exist from real use of the platform (an organization someone actually registered, a subscription someone actually started) with the usage and billing history a platform running for months would naturally have accumulated — so the dashboards built in [C53](../01-business/roadmap/open-decisions.md#c53) have something to show instead of zeros.

On a brand-new, completely empty database (no organizations, no subscriptions), every step below is a safe no-op.

## What it seeds

| Table | Rule | Idempotency check |
|---|---|---|
| `product_usage` | One row per (customer, product) for every ACTIVE organization member's ACTIVE product access — a launch count (3–60, deterministic from the customer/product id pair so it never changes once written) and a `lastLaunchedAt` within the past two weeks. | Skipped if a `product_usage` row already exists for that (customer, product) pair — real usage, once recorded, is never touched. |
| `invoice` / `invoice_line` | Two PAID invoices (one month ago, two months ago) for every ACTIVE, organization-owned subscription that has **no invoice at all yet** — amount, currency and line description computed exactly like `InvoiceService#generateForSubscription` (the plan's price, or the product's flat price). Invoice numbers follow the real `INV-<year>-<id>` format. | Skipped if the subscription already has any invoice, of any status — a subscription that has gone through the real purchase flow (and so already has its own real first invoice) is never touched. |
| `support_ticket` | Up to 5 fixed demo tickets ("Demo: Cannot access the product catalog", etc.), varied statuses (OPEN, IN_PROGRESS, ESCALATED, RESOLVED), attached to one synthetic demo customer. | Only tops the platform up to 5 tickets **total**, platform-wide — if 5 or more tickets already exist (real or previously seeded), nothing more is added. |
| `product_review` | Up to 4 fixed demo reviews ("Demo: Exactly what our team needed...", etc.), mixed PENDING/APPROVED, one per distinct ACTIVE product, attached to the same demo customer. | Only tops the platform up to 4 reviews **total**, platform-wide, with the same floor behavior as tickets. |
| `customer` | One demo customer (`demo.user@eis-demo.vyoog.local`, "Demo User") that the demo tickets and reviews are attached to — created once, looked up by email afterward. | Looked up by email before creating. |

## Why a demo customer, not `null`

`SupportTicket.requestedByCustomerId` and `ProductReview.customerId` are both used elsewhere to resolve a display name (the admin Support and Reviews screens join back to `Customer`) — a dangling id would show as broken rather than clearly synthetic. One real, clearly-named `Customer` row keeps every admin screen that reads these tables working normally, and "Demo User" makes it obvious to a platform admin which rows are synthetic.

## Why invoices are built directly, not through `InvoiceService`

`InvoiceService#generateForSubscription` always issues at the real current instant (by design — a real invoice is never backdated). Demo invoices need to look like they were issued one and two months ago, for the business dashboard's and platform dashboard's "this period vs last period" trend to have two different numbers to compare. The seeder mirrors that method's own amount/currency/line logic field-for-field rather than reusing it, then backdates `issuedAt`/`dueAt` with a direct bulk update (`InvoiceRepository#backdateIssuedAtForDemoData`) that bypasses the `@CreatedDate` auditing listener that would otherwise overwrite any pre-set value with "now" on save. This keeps the real invoice-generation code path completely untouched (per `CLAUDE.md`'s "do not modify existing business logic" rule) while still producing historically-dated, real PAID invoice rows.

## Where it's registered

`backend/src/main/java/com/vyoog/eisplatform/modules/dashboard/service/DemoDataSeeder.java`. Backed by small additive repository methods only (`OrganizationRepository`/`ProductRepository`/`ProductSubscriptionRepository`/`ProductReviewRepository`/`SupportTicketRepository` count/exists helpers, `InvoiceRepository#findByStatusIn`/`backdateIssuedAtForDemoData`, `ProductUsageRepository#topProductsByTotalLaunches`) — no schema change, so no new migration file.
