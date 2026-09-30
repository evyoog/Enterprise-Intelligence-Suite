# Business rules — Billing & Payments (REQ-BIL-001)

Feature-specific rules. Cross-feature rules that also apply: [BR-BIL-001 No raw card data](../../../03-business-rules/BR-BIL-001-no-raw-card-data.md) and [BR-SEC-001 Central secrets file](../../../03-business-rules/BR-SEC-001-central-secrets-file.md).

| ID | Rule |
|---|---|
| BR-1 | A customer sees only their own billing data; an organization's billing data is visible only to the users allowed by Open question 5. Any other invoice, payment or payment method is refused with a generic 404. |
| BR-2 | An invoice is created in the currency of the plan it bills. Amounts are stored in the currency's smallest unit (for example paise). |
| BR-3 | An invoice number is unique and never changes after the invoice is finalized. A finalized invoice's lines and amounts never change. |
| BR-4 | Only an OPEN invoice can be paid. A PAID or VOID invoice cannot be paid again. |
| BR-5 | A payment is recorded as successful only after the Razorpay payment signature (or webhook signature) is verified with the secret from the common secrets file. An unverified result changes nothing. |
| BR-6 | Each Razorpay webhook event is processed at most once, identified by its event ID. Repeated deliveries are acknowledged and ignored. |
| BR-7 | A refund amount must be greater than zero and not more than the captured amount minus refunds already made. A reason is required. |
| BR-8 | Only one payment method per customer or organization is the default. Removing the default leaves no default until another is chosen. |
| BR-9 | Saving a card requires the customer's explicit consent, recorded with the time it was given. |
| BR-10 | When any Razorpay credential is missing, all Razorpay-dependent actions are refused with "Payment gateway not configured"; nothing is sent to Razorpay. |
| BR-11 | Payments, refunds, reconciles, payment-method changes and billing-detail changes are audited with the acting user. |
