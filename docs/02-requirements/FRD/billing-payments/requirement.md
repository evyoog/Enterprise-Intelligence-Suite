# REQ-BIL-001 — Billing & Payments (Razorpay, screens first)

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner, 2026-10-13 ([C97](../../../01-business/roadmap/open-decisions.md#c97), answers BI-1 to BI-9). Every open question that blocked approval (3, 4, 5, 8, 9, 10, 11, 12) is answered below.
**Decision:** [C97](../../../01-business/roadmap/open-decisions.md#c97) (sprint 2026.4.3 answers, 2026-10-13), [C100](../../../01-business/roadmap/open-decisions.md#c100) (PB-1 note: the coupon exception, per-seat invoice lines, 2026-10-14), [C46](../../../01-business/roadmap/open-decisions.md#c46), [C50](../../../01-business/roadmap/open-decisions.md#c50) (billing scope), [C51](../../../01-business/roadmap/open-decisions.md#c51) (tax, see REQ-BIL-002), [C55](../../../01-business/roadmap/open-decisions.md#c55) (checkout, pay by invoice)

| Field | Value |
|---|---|
| Sprint | [2026.4.3](../../../01-business/roadmap/sprints/SPRINT-2026.4.3.md) |
| Requirement ID | REQ-BIL-001 |
| Application | [08 Billing & Payments](../../../01-business/roadmap/applications/08-billing-payments.md) |
| Application code | `APP-BIL` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |
| Payment provider | Razorpay is the first gateway ([C46](../../../01-business/roadmap/open-decisions.md#c46)); the payment screens are gateway-neutral and the product owner may add others ([C97](../../../01-business/roadmap/open-decisions.md#c97) BI-8, REQ-BIL-001.31). Credentials: provided later by the product owner |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 08.02.02 | Generate invoice; Finalize invoice | Yes |
| 08.02.02 | Adjust invoice; Credit invoice | No — Not specified (see Out of scope) |
| 08.03.01 | Add payment method; Remove payment method; Set default payment method | Yes |
| 08.03.02 | Authorize payment; Capture payment; Refund payment; Retry payment; Reconcile payment | Yes |
| 08.04.01 | Generate invoice; Generate receipt; Download document | Yes |
| 08.04.01 | Generate credit note | No — Not specified |
| 08.05.02 | Format currency | Yes (uses existing platform currencies) |
| 01.02.01 | View spending (moved to Billing by [C19](../../../01-business/roadmap/open-decisions.md#c19)) | Yes |

**08.01.02 Promotions — coupon codes only** ([C100](../../../01-business/roadmap/open-decisions.md#c100) PB-1 note): create coupon, apply promotion (REQ-BIL-001.34 to .36). Not covered: 08.01.01 Price books, plan-level discounts and any other promotion, 08.02.01 Usage billing — later, per [C50](../../../01-business/roadmap/open-decisions.md#c50) (not MVP), which [C100](../../../01-business/roadmap/open-decisions.md#c100) PB-1 A confirms; 08.05.01 Tax — see [REQ-BIL-002](../tax-rules/requirement.md); 08.05.02 Convert currency — Not specified.

## Summary
Customers (individuals, and organization members with the **Manage billing** permission for their organization) can keep billing details, see invoices, pay them through **Razorpay Checkout**, save and manage payment methods, see payment history, and download invoice and receipt documents. Platform administrators can see all invoices and payments, refund payments, and see whether the payment gateway is configured.

The screens and backend are built **now**, before Razorpay credentials exist. Until the credentials are placed in the common secrets file ([BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md)), the platform runs in **Not configured** mode: everything works except actions that need Razorpay, which are disabled with a clear message.

EIS never collects, transmits or stores full card numbers or CVV ([BR-BIL-001](../../../03-business-rules/BR-BIL-001-no-raw-card-data.md)). Card and UPI details are entered only in Razorpay's own secure window.

## Actors
- Individual customer — own billing details, invoices, payment methods and payments
- Organization member with **Manage billing** (`MANAGE_ORGANIZATION_BILLING`) — the organization's billing; organization admins have it by default and can give it to others ([C97](../../../01-business/roadmap/open-decisions.md#c97) BI-5, [C65](../../../01-business/roadmap/open-decisions.md#c65))
- Platform administrator (new permission `MANAGE_BILLING`) — all invoices and payments, refunds, gateway status and choice, which organizations may pay by invoice, **coupons**
- Razorpay — checkout window, tokenized payment methods, webhooks

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-BIL-001.1 | A customer can view and edit billing details: billing name, billing email, address line 1, address line 2, city, state/region, postal code, country, and an optional tax ID. | Must |
| REQ-BIL-001.2 | An invoice is generated and finalized when a paid subscription starts or renews (individual subscription, approved organization order, or renewal job). It holds one line per plan with the billing period, quantity (for an organization the seats, REQ-BIL-001.36), unit price, amount, currency, subtotal, and total. Each line also shows the tax name, tax rate and tax amount calculated by [REQ-BIL-002](../tax-rules/requirement.md), and the invoice records which tax method was used (Admin rate or Tax service). | Must |
| REQ-BIL-001.3 | Every invoice has a unique invoice number that never changes once finalized. | Must |
| REQ-BIL-001.4 | A customer can list their invoices (filter by status and date) and open an invoice's detail. | Must |
| REQ-BIL-001.5 | A customer can pay an OPEN invoice. The backend creates a Razorpay order for the invoice total; the UI opens Razorpay Checkout with that order; after completion the backend verifies Razorpay's signature before recording the payment. | Must |
| REQ-BIL-001.6 | A verified, captured payment marks the invoice PAID and records the payment (method type, network or UPI app, last 4 digits where applicable, Razorpay payment ID, amount, currency, time). | Must |
| REQ-BIL-001.7 | A failed or cancelled payment leaves the invoice OPEN and records the failure reason Razorpay returns; the customer can retry payment. | Must |
| REQ-BIL-001.8 | Razorpay webhooks (payment captured, payment failed, refund processed) are accepted only with a valid webhook signature, are processed once per event ID, and update payment and invoice status (reconciliation). | Must |
| REQ-BIL-001.9 | A platform administrator can trigger a reconcile for one payment, which fetches its current status from Razorpay and updates EIS. | Should |
| REQ-BIL-001.10 | A customer can add a payment method through Razorpay (card with saving consent, or UPI), list saved methods (type, network, last 4, expiry month/year, default badge), set one as default, and remove one. Removing also deletes the saved token at Razorpay. | Must |
| REQ-BIL-001.11 | A customer can download an invoice document for any finalized invoice and a receipt for any paid invoice. | Must |
| REQ-BIL-001.12 | A platform administrator (`MANAGE_BILLING`) can list and filter all invoices and payments, open a payment, and refund it fully or partly (amount up to the captured amount not yet refunded, reason required). | Must |
| REQ-BIL-001.13 | A platform administrator can view the payment gateway status: provider, mode (test/live), whether key ID, key secret and webhook secret are set (never their values, except the key ID masked), the webhook URL to register in Razorpay, the last webhook received, and a Test connection action. Values are read from the common secrets file only; the screen does not edit them. | Must |
| REQ-BIL-001.14 | When Razorpay credentials are missing, every Razorpay-dependent action returns a "Payment gateway not configured" error, and the screens show a banner and disable Pay, Add payment method, Refund, Reconcile and Test connection. Billing details and invoices still work. | Must |
| REQ-BIL-001.15 | Amounts are shown using the platform's configured currencies and formatting. | Must |
| REQ-BIL-001.16 | The business dashboard's spending card (01.02.01) shows the total paid in the current and previous period from paid invoices, replacing the current "not available" note. | Must |
| REQ-BIL-001.17 | Every payment, refund, reconcile, payment-method change and billing-details change is written to the audit log. | Must |
| REQ-BIL-001.18 | **Checkout payment screen** ([C55](../../../01-business/roadmap/open-decisions.md#c55), layout redesigned by [C59](../../../01-business/roadmap/open-decisions.md#c59)): one screen with the breadcrumb steps **Cart › Billing details › Payment › Complete**, used (a) when an individual proceeds from the cart ([REQ-MKT-003.8](../cart-checkout/requirement.md)), (b) when paying an OPEN invoice from Billing, and (c) after an organization order is approved and its invoice is issued. The Payment step shows a billing-details summary with **Change** links, an **Amount due** badge (display only — no partial payments), and a row of payment-method tiles: **Card**, **UPI**, **Netbanking**, **Wallets** (all through Razorpay) and **Pay by invoice** (offline, REQ-BIL-001.19). Card entry follows BR-BIL-001: Pay opens Razorpay Checkout with the selected method preselected (see C55 for why; the card fields on the EIS page are read-only placeholders). A Terms & Conditions, Refund policy and Privacy policy consent checkbox must be ticked before Pay. Screen: [checkout-payment.md](../../../05-ui/screen-requirements/checkout-payment.md). | Must |
| REQ-BIL-001.19 | **Offline pay-by-invoice:** selecting Pay by invoice generates and finalizes the invoice (if not already issued), records the route OFFLINE on it, shows payment instructions and the offline bank details (REQ-BIL-001.21), and emails the invoice to the billing email. The invoice stays OPEN until a payment is recorded (REQ-BIL-001.20). Who may use it: REQ-BIL-001.28. Due date: REQ-BIL-001.26 (15 days). | Must |
| REQ-BIL-001.20 | **Record offline payment** (platform admin, `MANAGE_BILLING`): on an OPEN invoice, record amount received, date received (not in the future), method (bank transfer, NEFT/RTGS, cheque), reference number (required) and an optional note. The amount must equal the open amount (partial offline payments: Not specified). Recording marks the invoice PAID, creates a payment with method type OFFLINE, makes the receipt available, and is audited. | Must |
| REQ-BIL-001.21 | **Offline bank details:** the bank details printed on offline invoices and on the checkout's offline result are maintained by a platform billing administrator (`MANAGE_BILLING`) in admin settings. Fields: account name, bank name, account number, IFSC, SWIFT/BIC — to confirm (Open question 11). They are not secrets and are not stored in the common secrets file. | Must |
| REQ-BIL-001.22 | **Saved methods at checkout** ([C59](../../../01-business/roadmap/open-decisions.md#c59)): under the Card tile the customer's saved cards are listed (network logo, `•••• •••• •••• {last4}`, name on card if Razorpay returns it, expiry, an **Expired** chip when past expiry) and can be chosen for the payment; **Remove** is in each card's overflow menu (the same delete as REQ-BIL-001.10). **+ Use another card** pays with a new card in Razorpay's window, with the "Save this card securely with Razorpay" consent. Saved UPI IDs are listed under the UPI tile only if Razorpay returns them (Open question 18). Only display fields from Razorpay are shown (BR-BIL-001). | Must |
| REQ-BIL-001.23 | **Payment brand logos** ([C59](../../../01-business/roadmap/open-decisions.md#c59)): card network, UPI and UPI app logos and the "Powered by Razorpay" badge are official assets stored in the app, shown only for methods EIS offers, used under each brand's guidelines and licence ([payment-brand-assets.md](../../../05-ui/screen-requirements/payment-brand-assets.md)). Wallets show a generic icon until the wallet list is decided (Open question 19). | Should |
| REQ-BIL-001.24 | **Billing settings** ([C60](../../../01-business/roadmap/open-decisions.md#c60)): a billing administrator maintains the business profile printed on invoices (legal name, registrations, address, contact), the invoice number prefix, payment terms and footer note, the full offline payment details and accepted offline methods, which checkout methods are offered, and the Razorpay Checkout name, description and colour. Razorpay keys are shown only as set / not set (BR-SEC-001). Screen: [admin-billing-settings.md](../../../05-ui/screen-requirements/admin-billing-settings.md). | Must |
| REQ-BIL-001.25 | **A purchase starts after payment** ([C97](../../../01-business/roadmap/open-decisions.md#c97) BI-1 C). A card, UPI, netbanking or wallet purchase from the cart stays **PENDING_SUBSCRIPTION** until its invoice is PAID, then becomes ACTIVE (the term starts then). A purchase paid by invoice (Pay by invoice) and an approved organization order become ACTIVE **immediately**, with the invoice's due date. A free plan, and a plan the customer already has active, start at once. Activation happens once however many times the payment event arrives. A purchase whose invoice is a tax-blocked Draft stays pending. | Must |
| REQ-BIL-001.26 | **Invoice number and terms** ([C97](../../../01-business/roadmap/open-decisions.md#c97) BI-2 A; built with the tax work, [C96](../../../01-business/roadmap/open-decisions.md#c96)). The number is `PREFIX/YY-YY/NNNNNN` — for example `EIS/26-27/000123`: the prefix, the financial year April to March, and a six-digit sequence that restarts each financial year. The default payment terms are **15 days** (due date = issue date + 15). Both come from Billing settings (prefix, payment terms). The invoice also prints the legal fields of [BI-3](../../../01-business/roadmap/open-decisions.md#c97) (see [tax-rules](../tax-rules/requirement.md)). | Must |
| REQ-BIL-001.27 | **Invoice payment reminders** ([C97](../../../01-business/roadmap/open-decisions.md#c97) BI-2 A): an OPEN invoice with a due date gets a reminder **3 days before** the due date, **on** the due date and **7 days after** it, each kind **once** per invoice. Reminders stop when the invoice is paid or refunded, and never go out for a Draft. They are an in-app notification and an e-mail to the billing e-mail (individual: the customer; organization: its administrators). | Must |
| REQ-BIL-001.28 | **Pay by invoice only for enabled organizations** ([C97](../../../01-business/roadmap/open-decisions.md#c97) BI-4 C). The platform administrator (`MANAGE_BILLING`) enables Pay by invoice organization by organization. An individual customer can **never** pay by invoice. The global Pay by invoice switch in Billing settings (REQ-BIL-001.24) still applies: with it off, no one can. The checkout shows the tile, and the API accepts the offline route, only when both the switch and the organization's setting are on. Enabling and disabling are audited. | Must |
| REQ-BIL-001.29 | **Organization billing needs Manage billing** ([C97](../../../01-business/roadmap/open-decisions.md#c97) BI-5 A). Every `/organization/me/billing/**` call, **reads included**, requires the separate **Manage billing** permission (`MANAGE_ORGANIZATION_BILLING`); a member without it gets 403 and the Billing screen explains why. | Must |
| REQ-BIL-001.30 | **Terms, Privacy and Refund pages** ([C97](../../../01-business/roadmap/open-decisions.md#c97) BI-7 A). They are pages on the evyoog.com website. Their three URLs are set by the billing administrator in Billing settings → Business & invoicing; the checkout consent sentence links to them; the invoice document prints them. A URL not set is shown as plain text and not printed. | Must |
| REQ-BIL-001.31 | **Gateway-neutral payment screens** ([C97](../../../01-business/roadmap/open-decisions.md#c97) BI-8). The backend has a `PaymentGateway` interface; Razorpay is its first implementation. The administrator sees a list of gateways, which one is active, and can test each and make a configured one active. The checkout summary carries `gatewayCode` and `gatewayName`; the screens say nothing gateway-specific. Creating and confirming a payment is still Razorpay-specific in `PaymentService`; a second gateway extends that when it is chosen (how to add one: [billing-payments API](../../../06-api/api-requirements/billing-payments.md#how-to-add-another-payment-gateway)). | Must |
| REQ-BIL-001.32 | **Optional purchase-order number** ([C97](../../../01-business/roadmap/open-decisions.md#c97) BI-9 A). A customer may give a purchase-order number (up to 50 characters) for an invoice until it is paid; it is printed on the invoice and never required. | Should |
| REQ-BIL-001.33 | **Refunds, methods, bank payments and fees** ([C97](../../../01-business/roadmap/open-decisions.md#c97) BI-9 A, BI-6 A). Refunds are made by platform administrators only, full or partial (REQ-BIL-001.12). Online methods are cards, UPI, netbanking and wallets. A bank payment recorded by an administrator must equal the open amount: **no partial bank payments** (REQ-BIL-001.20). The bank details shown are account name, bank name, account number, IFSC and SWIFT/BIC (REQ-BIL-001.21). **No fees line** is added to any invoice or checkout summary. | Must |

### Coupons and seat billing ([C100](../../../01-business/roadmap/open-decisions.md#c100))
| ID | Requirement | Priority |
|---|---|---|
| REQ-BIL-001.34 | **Coupons, set up by the platform administrator** (PB-1 note; the exception to REQ-BIL-001's "promotions are later"). A platform administrator with `MANAGE_BILLING` creates a coupon on **Billing → Coupons** (`/admin/billing/coupons`) with a **code** (3 to 40 letters, digits, dashes or underscores, stored in capitals, unique), an optional **description** (up to 200 characters), a **type** — **percent off** from 1 to 99, or a **fixed amount** above zero in **one currency** — and an **expiry date and time** that must be in the future when the coupon is created. The administrator can **edit** everything but the code, **disable** a coupon before its expiry and **enable** it again, and **delete** a coupon that was **never redeemed**. The list shows each coupon's status (Active, Disabled, Expired) and how many times it was redeemed. Every create, change, enable, disable and delete is audited. A coupon is usable only while it is enabled and before its expiry. | Must |
| REQ-BIL-001.35 | **A coupon on an invoice.** A customer who pays (an individual, an organization administrator or a Manage billing holder) enters **one code per cart** ([REQ-MKT-003.18](../cart-checkout/requirement.md)). When the invoice is created the code is **checked again** (it exists, is enabled, has not expired, and a fixed coupon is in the invoice's currency) and a coupon worth the whole invoice or more is **refused**; if it fails, **nothing is created** and the customer is told why. A percent coupon takes its percentage of the invoice subtotal (rounded to the currency's smallest unit); a fixed coupon takes its amount. The discount is **shared across the invoice lines in proportion to their amounts** (the last line takes the rounding remainder) and **tax is calculated on the discounted amounts**. The invoice records the **code** and the **discount** and each line its share. **`subtotal` on the invoice is net of the discount** (so subtotal + tax = total); the checkout summary shows the **gross** subtotal, a **Discount ({code})** line, the tax and the total. The coupon's **redemption count goes up when the invoice is created**, once per invoice. The invoice list, the checkout and the invoice document show the discount and the code. A **purchase request** from a member carries no coupon. A renewal invoice and a seat-increase invoice take no coupon. | Must |
| REQ-BIL-001.36 | **Per-seat invoice lines** ([REQ-SUB-003.7, .8](../subscription-seats/requirement.md)). For an organization subscription a new or renewal invoice has a line of the plan price × the seats, with the seat count as the line quantity. A **seat increase** during a term issues its own invoice with one line "Added seats: {product} — {plan}": quantity = the added seats, amount = added seats × seat price × remaining days ÷ days in the term (30 or 365), taxed like any invoice. Nothing is invoiced for a free plan or when no days remain. Per-seat pricing is an engineering default to confirm (see [Open points](#open-points)). | Must |

## Out of scope
- Collecting card numbers, CVV or bank details in any EIS screen or API ([BR-BIL-001](../../../03-business-rules/BR-BIL-001-no-raw-card-data.md)).
- Automatic charging of saved methods for renewals (auto-debit / e-mandate) — pending decision D15 (auto-renew).
- Price books, plan-level discounts, promotions other than the coupon code, usage billing — later, per [C50](../../../01-business/roadmap/open-decisions.md#c50) (confirmed by [C100](../../../01-business/roadmap/open-decisions.md#c100) PB-1 A). Coupon **limits** (maximum redemptions, per customer, minimum amount, first purchase only), coupons scoped to products or plans, and stacking are **Not specified**.
- Tax calculation rules — see [REQ-BIL-002](../tax-rules/requirement.md).
- Adjusting or crediting invoices, credit notes — Not specified.
- A second payment provider's payment creation, confirmation and webhooks: the gateway list and interface exist ([REQ-BIL-001.31](#functional-requirements)), but only Razorpay can take a payment today. Which gateways follow Razorpay is Not specified ([C97](../../../01-business/roadmap/open-decisions.md#c97)).
- Partial offline (bank) payments and a fees line — decided out ([C97](../../../01-business/roadmap/open-decisions.md#c97) BI-9).
- Partner payouts and revenue sharing (14.03).
- Editing gateway credentials in the UI — they live only in the common secrets file ([BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md)).

## Dependencies
- Existing subscription flows (REQ-SUB-001), seats (REQ-SUB-003), the cart (REQ-MKT-003), organization orders (REQ-ORD-001), platform currencies and regions (REQ-GOV-001), `AuditService`, `NotificationService`.
- New tables described in [07-database/data-model/billing-payments.md](../../../07-database/data-model/billing-payments.md).
- New permission `MANAGE_BILLING` (platform ADMIN) and the organization permission `MANAGE_ORGANIZATION_BILLING` ("Manage billing", REQ-TEN-005).
- Migration [V037__billing_rules_sprint_2026_4_3.sql](../../../../database/migrations/V037__billing_rules_sprint_2026_4_3.sql) and, for coupons and invoice discounts, [V040__seats_grace_purchase_requests_coupons.sql](../../../../database/migrations/V040__seats_grace_purchase_requests_coupons.sql) (apply by hand: Flyway is disabled).
- Razorpay account and credentials (provided later by the product owner): [09-integrations/razorpay.md](../../../09-integrations/razorpay.md).
- Common secrets file: [BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md).

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement (this file), feature business rules, feature workflow, acceptance criteria | this folder |
| Screens (UI) | [docs/05-ui/screen-requirements/](../../../05-ui/screen-requirements/) — see [ui-requirements.md](ui-requirements.md) |
| API | [docs/06-api/api-requirements/billing-payments.md](../../../06-api/api-requirements/billing-payments.md) — see [api-requirements.md](api-requirements.md) |
| Cross-feature rules | [BR-BIL-001](../../../03-business-rules/BR-BIL-001-no-raw-card-data.md), [BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md) |
| Cross-feature flow | [docs/04-workflows/purchase-to-payment.md](../../../04-workflows/purchase-to-payment.md) |
| Data model | [docs/07-database/data-model/billing-payments.md](../../../07-database/data-model/billing-payments.md) |
| Razorpay integration | [docs/09-integrations/razorpay.md](../../../09-integrations/razorpay.md) |

## Open points
Raised for the product owner ([C100](../../../01-business/roadmap/open-decisions.md#c100)); none blocks use of the feature.
1. **Per-seat pricing is an engineering default to confirm** (REQ-BIL-001.36, [REQ-SUB-003.7](../subscription-seats/requirement.md#open-points)): it changes what organizations are invoiced.
2. **Coupons have no limits yet.** There is no maximum number of redemptions, no per-customer limit, no minimum amount, no first-purchase-only rule and no scoping to products or plans; any enabled, unexpired coupon works for any cart in its currency, any number of times. Specify these before a campaign relies on them.
3. **A coupon is counted when the invoice is created, even if that invoice is a tax-blocked Draft** ([REQ-BIL-002](../tax-rules/requirement.md)) or is never paid; there is no un-counting.
4. **The coupon edit screen refuses a past expiry, the API accepts it** on an existing coupon (that is how a coupon is expired at once through the API). The create rule (expiry in the future) is the same on both.

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | ~~Tax (D4)~~ — **Answered — [C51](../../../01-business/roadmap/open-decisions.md#c51).** Tax rules and calculation are specified in [REQ-BIL-002](../tax-rules/requirement.md) (Admin rate or Tax service per region, admin rate as fallback). REQ-BIL-002 has its own open questions that block its approval. | No |
| 2 | ~~Billing scope (D3)~~ — **Answered — [C50](../../../01-business/roadmap/open-decisions.md#c50).** The MVP includes recurring subscription billing, invoices, card/UPI payments, tax and currency; price books, promotions and usage billing are later, not MVP. | No |
| 3 | ~~**Activation vs payment**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-1 C.** A card or UPI purchase becomes active after payment; an approved organization order and a pay-by-invoice purchase become active immediately, with a due date ([REQ-BIL-001.25](#functional-requirements)). | No |
| 4 | ~~**Invoice number format and due date**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-2 A.** Number `EIS/26-27/000123`; due in 15 days; reminders 3 days before, on the due date and 7 days after ([REQ-BIL-001.26, .27](#functional-requirements)). | No |
| 5 | ~~**Who handles organization billing (D16)**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-5 A.** A separate **Manage billing** permission (`MANAGE_ORGANIZATION_BILLING`), needed for every organization billing call including reads ([REQ-BIL-001.29](#functional-requirements)). Organization admins hold it by default. | No |
| 6 | ~~**Refund policy**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-9 A.** Refunds by administrators only, full or partial. | No |
| 7 | ~~**Payment methods offered**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-9 A.** Cards, UPI, netbanking and wallets. | No |
| 8 | ~~**Invoice document legal fields**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-3 A.** Legal name, address, GSTIN and PAN; invoice number and date; place of supply; SAC code; the customer's name, address and GSTIN (built with the tax work, [tax-rules](../tax-rules/requirement.md)). | No |
| 9 | ~~**Who may use Pay by invoice**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-4 C.** Only organizations the platform administrator enables; never an individual ([REQ-BIL-001.28](#functional-requirements)). | No |
| 10 | ~~**Payment terms for offline invoices**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-2 A.** 15 days; reminders are sent ([REQ-BIL-001.26, .27](#functional-requirements)). | No |
| 11 | ~~**Offline bank details fields**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-6 A.** Account name, bank name, account number, IFSC and SWIFT/BIC, kept in the admin Billing settings (as built). | No |
| 12 | ~~**Terms & Conditions and Privacy Policy URLs**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-7 A.** The pages are on the evyoog.com website; EIS links to them; the URLs are set in Billing settings ([REQ-BIL-001.30](#functional-requirements)). The URLs themselves are **Not specified** until the product owner supplies them. | No |
| 13 | ~~**Purchase-order number**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-9 A.** Optional ([REQ-BIL-001.32](#functional-requirements)). | No |
| 14 | **Other online methods:** which methods (netbanking, wallets, others) the "Other online methods" option offers. (Refines Open question 7.) | No — confirm in review |
| 15 | **Card-panel approach:** re-verify against Razorpay's current documentation that no provider-hosted embeddable card fields exist; if they do, the card panel uses them instead of placeholder fields (C55). | No — confirm before Phase 2 build |
| 16 | ~~**Partial offline payments**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-9 A.** No partial bank payments; the amount must equal the open amount. | No |
| 17 | **Netbanking and wallets as separate tiles** ([C59](../../../01-business/roadmap/open-decisions.md#c59)) replace the single "Other online methods" option (Open question 14). Which banks and which wallets Razorpay offers on the EIS account: provider-defined; confirm before showing any wallet brand. | No — confirm in review |
| 18 | **Saved UPI IDs and UPI ID entry:** whether Razorpay returns saved UPI IDs for a customer, and whether a UPI ID typed on the EIS page can be passed to Razorpay Standard Checkout (otherwise it is typed in Razorpay's window). Not verified (razorpay.com not reachable from the build environment). | No — confirm in review |
| 19 | **Wallet brands** to show as logos. Until decided, a generic wallet icon. | No — confirm in review |
| 20 | ~~**Refund policy URL**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-7 A.** One of the three links in Billing settings ([REQ-BIL-001.30](#functional-requirements)). | No |
| 21 | ~~**Fees line**~~ — **Answered — [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-9 A.** No fees line. | No |
| 22 | **Breadcrumb "Cart" step for an invoice paid from Billing** (entry b, c): shown as a link to the cart, or hidden? Proposed: hidden when the checkout did not start from the cart. | No — confirm in review |

Questions 14, 15, 17, 18, 19 and 22 were not part of the C97 answers and stay as written above; none blocks approval. Question 14 is superseded by the Netbanking and Wallets tiles (question 17).

## Defaults applied (not asked)
Recorded because C97 did not specify them:
- **Pay by invoice backfill:** organizations that already had an OFFLINE-route invoice when migration V037 ran keep Pay by invoice (their open invoices stay payable); every other organization starts disabled.
- **Reminder timing:** the day is the UTC calendar day; if the job was down, only the most advanced reminder that applies is sent, never a burst; the job runs every 30 minutes (first run 2 minutes after start) and is off when `app.renewals.job-enabled=false`.
- **Purchase order number format:** letters, digits, spaces and `. / _ -`, 1 to 50 characters, starting with a letter or digit; blank clears it; changeable while the invoice is OPEN or a Draft.
- **Policy URLs:** `http://` or `https://`, up to 480 characters after the scheme check, stored up to 500.
- **Active gateway:** only a gateway whose credentials are set can be made active; changing it affects new payments only; an unknown saved code falls back to the first gateway in the list.
- **Already active subscription:** a repeat purchase of a product the customer already has active keeps running while the new invoice is paid.
