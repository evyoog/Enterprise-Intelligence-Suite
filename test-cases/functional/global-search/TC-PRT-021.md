# TC-PRT-021: Word forms, accents and exact phrases (English and Spanish)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-021 |
| Requirement ID (required) | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Published articles in English and Spanish.

## Steps
1. Search "invoice pays" for an article titled "Paying … invoices".
2. Search "factura pagos" and "facturacion" for "Cómo pagar las facturas" / "facturación".
3. Search "single sign on" where one article has the phrase and one has the words apart.
4. Search the quoted phrase "cycle billing".

## Expected Result
Every article is found; the phrase match ranks first with the label Exact phrase; the quoted search returns only the article containing the phrase.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/PostgresSearchIntegrationTest.java` — `matchesEnglishWordForms`, `matchesSpanishWordFormsAndIgnoresAccents`, `exactPhraseRanksAboveScatteredWords`, `quotedPhraseFindsOnlyThePhrase`
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/SearchTextUtilitiesTest.java` — `foldsCaseAndAccents`, `parsesPhrasesIdsAndLimits`, `buildsTsQueriesFromWordsOnly`

## Actual Result
The automated tests above passed on 2026-10-05 (PostgreSQL 16 with pg_trgm and pgvector 0.6.0; embeddings from the stub test model).

## Status
Passed (automated run 2026-10-05)

## Linked Defect (if failed)
None.
