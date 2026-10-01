# Business rules — Tax Rules and Calculation (REQ-BIL-002)

Cross-feature rule that also applies: [BR-SEC-001 Central secrets file](../../../03-business-rules/BR-SEC-001-central-secrets-file.md).

| ID | Rule |
|---|---|
| BR-1 | A tax rule's rate is a percentage from 0 to 100, with up to 2 decimal places. |
| BR-2 | At most one **enabled** tax rule exists per region per effective-from date ([REQ-BIL-002.2](requirement.md)). Creating a second is refused as a conflict. |
| BR-3 | A region with no enabled tax rule produces a tax amount of 0; the invoice shows "No tax rule for this region" rather than failing. |
| BR-4 | The Tax service method always falls back to that region's Admin rate when the tax service is not configured, is unreachable, or returns an error. The invoice records which method actually produced the amount. |
| BR-5 | The tax name, rate and amount used are copied onto the invoice line at issue time. Changing or disabling a tax rule afterward never changes an already-finalized invoice ([REQ-BIL-002.5](requirement.md)). |
| BR-6 | Only a platform administrator (`MANAGE_BILLING`) may create, edit, enable or disable a tax rule. |
| BR-7 | Every tax-rule create, edit, enable and disable action is audited with the acting admin. |
