# Acceptance criteria — Billing & Payments (REQ-BIL-001)

Source for test cases in `test-cases/functional/billing-payments/` (created when the feature is built).

| ID | Requirement | Given / When / Then |
|---|---|---|
| AC-1 | REQ-BIL-001.1 | **Given** a signed-in customer **when** they save billing details with a blank billing name or an invalid billing email **then** the save is refused with a field error; **when** all required fields are valid **then** the details are saved and shown on reload. |
| AC-2 | REQ-BIL-001.2, .3 | **Given** a paid plan **when** a subscription starts, renews, or an organization order is approved **then** an OPEN invoice is created with a unique invoice number, one line per plan, the billing period and the plan's currency. |
| AC-3 | REQ-BIL-001.4, BR-1 | **Given** two customers **when** customer A requests customer B's invoice **then** the response is 404. |
| AC-4 | REQ-BIL-001.5, .6 | **Given** an OPEN invoice and a configured gateway **when** the customer completes Razorpay Checkout and the signature verifies **then** the payment is CAPTURED and the invoice is PAID. |
| AC-5 | REQ-BIL-001.5, BR-5 | **Given** a checkout result with an invalid signature **when** the backend verifies it **then** nothing changes and an error is returned. |
| AC-6 | REQ-BIL-001.7 | **Given** a failed payment **when** the customer returns to the invoice **then** it is still OPEN, the failure reason is shown, and **Retry payment** is available. |
| AC-7 | REQ-BIL-001.8, BR-6 | **Given** a `payment.captured` webhook delivered twice with the same event ID **when** both are received **then** the payment is updated once and both deliveries get a success response. |
| AC-8 | REQ-BIL-001.8 | **Given** a webhook with an invalid signature **when** it is received **then** it is rejected and nothing changes. |
| AC-9 | REQ-BIL-001.10, BR-9 | **Given** a customer adding a card **when** they have not ticked the saving consent **then** Save card is disabled; **when** the card is saved **then** the list shows network, last 4 digits and expiry, and no full card number exists anywhere in EIS. |
| AC-10 | REQ-BIL-001.10, BR-8 | **Given** two saved methods **when** the customer sets one as default **then** it is the only default; **when** they remove a method **then** it disappears and its token is deleted at Razorpay. |
| AC-11 | REQ-BIL-001.11 | **Given** a PAID invoice **when** the customer downloads it **then** both the invoice and the receipt documents are available; an OPEN invoice offers the invoice document only. |
| AC-12 | REQ-BIL-001.12, BR-7 | **Given** a captured payment of 1,000 **when** an admin refunds 1,200 **then** it is refused; **when** they refund 400 with a reason **then** the payment becomes PARTIALLY_REFUNDED; a refund without a reason is refused. |
| AC-13 | REQ-BIL-001.13 | **Given** the admin opens Payment gateway **then** it shows whether each credential is set, the key ID masked, the webhook URL, and never shows the key secret or webhook secret. |
| AC-14 | REQ-BIL-001.14, BR-10 | **Given** no Razorpay credentials in the common secrets file **when** a customer opens Billing **then** a "Payment gateway not configured" banner shows, Pay and Add payment method are disabled, and invoices and billing details still load. |
| AC-15 | REQ-BIL-001.15 | **Given** an INR invoice **then** amounts are shown with the INR symbol and formatting from the platform currency settings. |
| AC-16 | REQ-BIL-001.16 | **Given** paid invoices this period **when** the organization admin opens the business dashboard **then** the spending card shows the total paid instead of "not available". |
| AC-17 | REQ-BIL-001.17 | **Given** any payment, refund, reconcile, payment-method or billing-details change **then** an audit entry records the action and the acting user. |
| AC-18 | Accessibility | **Given** every Billing screen and dialog **then** it passes the automated accessibility (axe) test. |
