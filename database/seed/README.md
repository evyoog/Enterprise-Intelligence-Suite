# Seed data

Reference and demo data (products, platforms, default roles/permissions) for dev and uat. Never include real customer data or credentials.

Dashboard demo data (usage, backfilled invoices, support tickets, reviews — [C54](../../docs/01-business/roadmap/open-decisions.md#c54)) is seeded in code, not SQL: `backend/src/main/java/com/vyoog/eisplatform/modules/dashboard/service/DemoDataSeeder.java`, documented in [docs/07-database/demo-data.md](../../docs/07-database/demo-data.md). It never runs under the automated test suite (`@Profile("!test")`).
