# REQ-BIL-001 — Billing & Payments (Razorpay, screens first)

**Status:** Draft
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved. Cannot be approved until the open questions below marked **Blocks approval** are answered.
**Decision:** [C46](../../../01-business/roadmap/open-decisions.md#c46), [C50](../../../01-business/roadmap/open-decisions.md#c50) (billing scope), [C51](../../../01-business/roadmap/open-decisions.md#c51) (tax, see REQ-BIL-002), [C55](../../../01-business/roadmap/open-decisions.md#c55) (checkout, pay by invoice)

| Field | Value |
|---|---|
| Sprint | [2026.4.3](../../../01-business/roadmap/sprints/SPRINT-2026.4.3.md) |
| Requirement ID | REQ-BIL-001 |
| Application | [08 Billing & Payments](../../../01-business/roadmap/applications/08-billing-payments.md) |
| Application code | `APP-BIL` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |
| Payment provider | Razorpay ([C46](../../../01-business/roadmap/open-decisions.md#c46)). Credentials: provided later by the product owner |

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

Not covered: 08.01 Pricing (price books, promotions), 08.02.01 Usage billing — later, per [C50](../../../01-business/roadmap/open-decisions.md#c50) (not MVP); 08.05.01 Tax — see [REQ-BIL-002](../tax-rules/requirement.md); 08.05.02 Convert currency — Not specified.

## Summary
Customers (individuals, and organization admins for their organization) can keep billing details, see invoices, pay them through **Razorpay Checkout**, save and manage payment methods, see payment history, and download invoice and receipt documents. Platform administrators can see all invoices and payments, refund payments, and see whether the payment gateway is configured.

The screens and backend are built **now**, before Razorpay credentials exist. Until the credentials are placed in the common secrets file ([BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md)), the platform runs in **Not configured** mode: everything works except actions that need Razorpay, which are disabled with a clear message.

EIS never collects, transmits or stores full card numbers or CVV ([BR-BIL-001](../../../03-business-rules/BR-BIL-001-no-raw-card-data.md)). Card and UPI details are entered only in Razorpay's own secure window.

## Actors
- Individual customer — own billing details, invoices, payment methods and payments
- Organization admin — the organization's billing (permission: see Open question 5)
- Platform administrator (new permission `MANAGE_BILLING`) — all invoices and payments, refunds, gateway status
- Razorpay — checkout window, tokenized payment methods, webhooks

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-BIL-001.1 | A customer can view and edit billing details: billing name, billing email, address line 1, address line 2, city, state/region, postal code, country, and an optional tax ID. | Must |
| REQ-BIL-001.2 | An invoice is generated and finalized when a paid subscription starts or renews (individual subscription, approved organization order, or renewal job). It holds one line per plan with the billing period, quantity, unit price, amount, currency, subtotal, and total. Each line also shows the tax name, tax rate and tax amount calculated by [REQ-BIL-002](../tax-rules/requirement.md), and the invoice records which tax method was used (Admin rate or Tax service). | Must |
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
| REQ-BIL-001.18 | **Checkout payment screen** ([C55](../../../01-business/roadmap/open-decisions.md#c55)): a three-step flow (Billing details → Payment → Complete) used (a) when an individual subscribes to a paid plan, (b) when paying an OPEN invoice from Billing, and (c) after an organization order is approved and its invoice is issued. Payment options: card, UPI and other online methods (all through Razorpay), and Pay by invoice (offline, REQ-BIL-001.19). Card entry follows BR-BIL-001: the card panel shows read-only placeholder fields and Pay opens Razorpay Checkout with the card method preselected (see C55 for why). A Terms & Conditions / Privacy Policy consent checkbox must be ticked before Pay. Screen: [checkout-payment.md](../../../05-ui/screen-requirements/checkout-payment.md). | Must |
| REQ-BIL-001.19 | **Offline pay-by-invoice:** selecting Pay by invoice generates and finalizes the invoice (if not already issued), records the route OFFLINE on it, shows payment instructions and the offline bank details (REQ-BIL-001.21), and emails the invoice to the billing email. The invoice stays OPEN until a payment is recorded (REQ-BIL-001.20). Who may use it: Open question 9. Due date: Open question 4. | Must |
| REQ-BIL-001.20 | **Record offline payment** (platform admin, `MANAGE_BILLING`): on an OPEN invoice, record amount received, date received (not in the future), method (bank transfer, NEFT/RTGS, cheque), reference number (required) and an optional note. The amount must equal the open amount (partial offline payments: Not specified). Recording marks the invoice PAID, creates a payment with method type OFFLINE, makes the receipt available, and is audited. | Must |
| REQ-BIL-001.21 | **Offline bank details:** the bank details printed on offline invoices and on the checkout's offline result are maintained by a platform billing administrator (`MANAGE_BILLING`) in admin settings. Fields: account name, bank name, account number, IFSC, SWIFT/BIC — to confirm (Open question 11). They are not secrets and are not stored in the common secrets file. | Must |

## Out of scope
- Collecting card numbers, CVV or bank details in any EIS screen or API ([BR-BIL-001](../../../03-business-rules/BR-BIL-001-no-raw-card-data.md)).
- Automatic charging of saved methods for renewals (auto-debit / e-mandate) — pending decision D15 (auto-renew).
- Price books, promotions, coupons, usage billing — later, per [C50](../../../01-business/roadmap/open-decisions.md#c50).
- Tax calculation rules — see [REQ-BIL-002](../tax-rules/requirement.md).
- Adjusting or crediting invoices, credit notes — Not specified.
- A second payment provider (for example Stripe for international customers) — Not specified.
- Partner payouts and revenue sharing (14.03).
- Editing gateway credentials in the UI — they live only in the common secrets file ([BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md)).

## Dependencies
- Existing subscription flows (REQ-SUB-001), organization orders (REQ-ORD-001), platform currencies and regions (REQ-GOV-001), `AuditService`, `NotificationService`.
- New tables described in [07-database/data-model/billing-payments.md](../../../07-database/data-model/billing-payments.md).
- New permission `MANAGE_BILLING` (platform ADMIN).
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

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | ~~Tax (D4)~~ — **Answered — [C51](../../../01-business/roadmap/open-decisions.md#c51).** Tax rules and calculation are specified in [REQ-BIL-002](../tax-rules/requirement.md) (Admin rate or Tax service per region, admin rate as fallback). REQ-BIL-002 has its own open questions that block its approval. | No |
| 2 | ~~Billing scope (D3)~~ — **Answered — [C50](../../../01-business/roadmap/open-decisions.md#c50).** The MVP includes recurring subscription billing, invoices, card/UPI payments, tax and currency; price books, promotions and usage billing are later, not MVP. | No |
| 3 | **Activation vs payment:** should a subscription or approved order become active only after its first invoice is paid, or immediately (with the invoice due later)? Today activation happens on subscribe / approval. | Yes |
| 4 | **Invoice number format and due date:** format (for example a prefix plus sequence per financial year) and payment terms (days until due) are Not specified. | Yes |
| 5 | **Who handles organization billing (D16):** organization admins only (`MANAGE_ORGANIZATION`), or a separate assignable billing permission? | Yes |
| 6 | **Refund policy:** who may request refunds (admins only?), and are partial refunds allowed? This FRD assumes admins only, full or partial. | No — confirm in review |
| 7 | **Payment methods offered:** cards and UPI are assumed. Netbanking, wallets? | No — confirm in review |
| 8 | **Invoice document legal fields:** which company details, registration and tax numbers must appear on invoices and receipts. | Yes |
| 9 | **Who may use Pay by invoice ([C55](../../../01-business/roadmap/open-decisions.md#c55)):** all customers, organizations only, selected organizations, or above an amount? | Yes |
| 10 | **Payment terms for offline invoices:** days until due, and whether reminder emails are sent. (Extends Open question 4 for the offline route.) | Yes |
| 11 | **Offline bank details fields:** confirm account name, bank name, account number, IFSC, SWIFT/BIC, and that they are maintained in admin settings (not the secrets file). | Yes |
| 12 | **Terms & Conditions and Privacy Policy URLs** for the checkout consent checkbox — no legal routes exist in the app today. | Yes |
| 13 | **Purchase-order number:** is a PO number field needed at checkout for organizations? | No — confirm in review |
| 14 | **Other online methods:** which methods (netbanking, wallets, others) the "Other online methods" option offers. (Refines Open question 7.) | No — confirm in review |
| 15 | **Card-panel approach:** re-verify against Razorpay's current documentation that no provider-hosted embeddable card fields exist; if they do, the card panel uses them instead of placeholder fields (C55). | No — confirm before Phase 2 build |
| 16 | **Partial offline payments:** may an admin record less than the open amount? Today the amount must equal it. | No — confirm in review |
