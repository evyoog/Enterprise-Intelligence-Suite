# Payment brand assets (logos and icons)

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.23](../../02-requirements/FRD/billing-payments/requirement.md); used by [REQ-MKT-003](../../02-requirements/FRD/cart-checkout/requirement.md) |
| Decision | [C59](../../01-business/roadmap/open-decisions.md#c59) |
| Used on | [checkout-payment.md](checkout-payment.md) (method tiles, saved cards, "Powered by Razorpay"), [cart.md](cart.md) (trust line), Billing → Payment methods ([billing.md](billing.md)) |
| Asset folder (Phase 2) | `frontend/src/assets/payment-logos/` with `ATTRIBUTION.md` |

## Rules
1. **Official files only.** Each brand logo comes from that brand's official brand or media kit, or from a maintained open-source payment-icon package whose licence permits commercial use. Never copied or traced from the reference screenshots.
2. **Licence recorded.** The source URL, licence and download date of every file are recorded in `frontend/src/assets/payment-logos/ATTRIBUTION.md` before the file is used.
3. **Usage guidelines followed.** Logos are not recoloured, stretched, cropped, rotated or combined. Each keeps the clear space its guideline asks for. Where a brand provides light and dark variants, the dark-mode variant is used in dark mode; otherwise the logo sits on a neutral light chip in dark mode.
4. **Bundled, never hot-linked.** Logos are SVG files inside the app and imported by the components. Nothing is loaded from an external website at runtime.
5. **Offered methods only.** A brand logo appears only for a method EIS actually offers on its Razorpay account. **Wallet brands stay a generic wallet icon** until the wallet list is confirmed (REQ-BIL-001 Open question 19). Bank logos are not shown for Netbanking.
6. **Alt text.** Every logo has alt text with the brand name (for example `alt="Visa"`). When the brand name is already visible as text next to it, the logo is decorative (`alt=""`).
7. **Non-brand icons** (bank, wallet, invoice, cart, lock, trash, product placeholder) come from the icon set already used by the frontend. Today that is `lucide-react` (with MUI icons available through `@mui/icons-material` if added); the request named MUI icons — which set to standardise on is not decided, so existing components keep their set.
8. **Product images** come from the catalog's product media; where none exists, a consistent generic product icon is used. Product media storage (D23) is not decided.

## Asset list
Source and licence are filled in when each file is obtained in Phase 2. Until a file with a recorded licence exists, the UI shows the brand **name as a text label** in the same place (as built under C55).

| Brand | Used for | Source (to obtain) | Licence | Variants |
|---|---|---|---|---|
| Visa | Card tile, saved cards | Visa brand / merchant acceptance-mark resources | To record | Light, dark |
| Mastercard | Card tile, saved cards | Mastercard brand center (acceptance mark) | To record | Light, dark |
| RuPay | Card tile, saved cards | NPCI / RuPay brand resources | To record | To check |
| American Express | Card tile, saved cards | American Express brand resources | To record | To check |
| UPI | UPI tile | NPCI UPI brand resources | To record | To check |
| Google Pay | UPI tile | Google Pay brand guidelines | To record | Light, dark |
| PhonePe | UPI tile | PhonePe brand resources | To record | To check |
| Paytm | UPI tile | Paytm brand resources | To record | To check |
| BHIM | UPI tile | NPCI BHIM brand resources | To record | To check |
| Razorpay "Powered by" badge | Checkout footer, cart trust line | Razorpay brand / badge resources | To record | Light, dark |

Alternative to individual kits: a maintained open-source payment-icon package, if its licence permits commercial use and it states that its marks follow the brands' guidelines. Which package: Not specified — record the choice and licence in `ATTRIBUTION.md`.

## Sizes
- Method tile: logos up to 24 px high; up to four card-network logos in a row, wrapping on small tiles.
- Saved-card row: one network logo, 20 px high.
- Cart trust line and checkout footer: 16–20 px high.
- Exact sizes follow each brand's minimum-size rule where it is larger.

## Open points
- Brand guideline links, and whether any brand requires written permission or a merchant agreement before showing its mark: to check per brand when the files are obtained.
- Wallet list (REQ-BIL-001 Open question 19).
