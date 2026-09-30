# config/

| File | Committed | Purpose |
|---|---|---|
| `secrets.env.example` | Yes | Template listing every API key and password EIS uses, with empty values |
| `secrets.env` | **No** (git-ignored) | The real values. Create it by copying the template |

Rule and loading: [BR-SEC-001](../docs/03-business-rules/BR-SEC-001-central-secrets-file.md). Razorpay setup: [docs/09-integrations/razorpay.md](../docs/09-integrations/razorpay.md).
