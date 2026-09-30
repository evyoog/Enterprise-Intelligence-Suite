# BR-BIL-001 — No raw card or bank data in EIS

| Field | Value |
|---|---|
| Area | Billing & Payments (applies to every feature that takes payment) |
| Decision | [C46](../01-business/roadmap/open-decisions.md#c46) |
| Used by | [REQ-BIL-001](../02-requirements/FRD/billing-payments/requirement.md) |

## Rule
1. No EIS screen, API, log, database table, file or analytics event may collect, transmit or store a full card number, CVV/CVC, card PIN, or full bank-account credentials.
2. Card and UPI details are entered only in the payment provider's own secure window (Razorpay Checkout).
3. EIS stores only the provider's reference for a saved method (token or customer/method ID) and display details: method type, card network, last 4 digits, expiry month and year, card type, issuer name, and a masked UPI ID.
4. Saving a card needs the customer's explicit consent, recorded with the time.
5. Payment results are trusted only after the provider's signature is verified.

## Why
Card-industry security rules (PCI-DSS) and India's RBI card-on-file tokenization rules restrict who may store card data. Keeping card entry in the provider's window keeps EIS out of that scope. This is the standard practice for SaaS billing on Razorpay, Stripe and similar providers.
