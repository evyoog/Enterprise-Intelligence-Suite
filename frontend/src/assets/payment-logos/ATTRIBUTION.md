# Payment logos — sources and licences

Rules: [docs/05-ui/screen-requirements/payment-brand-assets.md](../../../../docs/05-ui/screen-requirements/payment-brand-assets.md) (C59, REQ-BIL-001.23).

Put each official logo here as `<brand>.svg` (and `<brand>-dark.svg` for a dark-mode variant), using the brand keys in `src/components/payments/PaymentLogos.tsx` (`visa`, `mastercard`, `rupay`, `amex`, `upi`, `gpay`, `phonepe`, `paytm`, `bhim`, `razorpay`). The component picks a file up automatically; until a file is here, the brand's name is shown as a text label.

**Add a row below before adding a file.** A file without a row must not be committed.

| File | Brand | Source (official brand kit or package, URL) | Licence / usage terms | Downloaded on | Checked by |
|---|---|---|---|---|---|

No logo files have been added yet (2026-10-01). The official brand kits could not be reached from the build environment, so every brand currently shows as a text label.
