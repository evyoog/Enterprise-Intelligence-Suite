# Acceptance criteria — Product content

Each criterion maps to test cases in [test-cases/functional/product-content/](../../../../test-cases/functional/product-content/).

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** an administrator **When** they ask for an upload URL for a PDF datasheet within the limit **Then** they get a presigned PUT URL for a new key under the application's prefix | [TC-CAT-019](../../../../test-cases/functional/product-content/TC-CAT-019.md) |
| AC-2 | **Given** a wrong type (SVG, EXE) or a file over the limit **When** an upload URL is requested **Then** 400 with a friendly message | [TC-CAT-020](../../../../test-cases/functional/product-content/TC-CAT-020.md) |
| AC-3 | **Given** an upload whose size or type differs from what was declared **When** the item is saved **Then** it is refused and the object is deleted | [TC-CAT-021](../../../../test-cases/functional/product-content/TC-CAT-021.md) |
| AC-4 | **Given** a Draft item **When** a visitor opens the product **Then** it is not shown; after Publish it is; after Unpublish it is gone | [TC-CAT-022](../../../../test-cases/functional/product-content/TC-CAT-022.md) |
| AC-5 | **Given** a published datasheet **When** a visitor clicks Download **Then** they get a 5-minute link; no response contains a permanent URL or credential | [TC-CAT-023](../../../../test-cases/functional/product-content/TC-CAT-023.md) |
| AC-6 | **Given** a YouTube link **When** it is saved **Then** it is recognised with its thumbnail and plays embedded; a non-https link is refused; another site is a plain link | [TC-CAT-024](../../../../test-cases/functional/product-content/TC-CAT-024.md) |
| AC-7 | **Given** a case study **When** it is saved **Then** it needs a customer name and shows problem, result, logo and PDF when published | [TC-CAT-025](../../../../test-cases/functional/product-content/TC-CAT-025.md) |
| AC-8 | **Given** an item **When** its file is replaced or it is edited **Then** its version rises by one, "Updated on" changes and the old file is deleted | [TC-CAT-026](../../../../test-cases/functional/product-content/TC-CAT-026.md) |
| AC-9 | **Given** an article that is no longer live or public **When** a visitor opens the product **Then** its documentation link is not shown | [TC-CAT-027](../../../../test-cases/functional/product-content/TC-CAT-027.md) |
| AC-10 | **Given** a user without `MANAGE_CATALOG` **When** they call an admin endpoint **Then** 403 | [TC-CAT-028](../../../../test-cases/functional/product-content/TC-CAT-028.md) |
| AC-11 | **Given** storage is not configured **When** a file upload is requested **Then** 503 `STORAGE_NOT_CONFIGURED`; links still save | [TC-CAT-029](../../../../test-cases/functional/product-content/TC-CAT-029.md) |
| AC-12 | **Given** an application with content **When** the application is deleted **Then** its items and files are deleted | [TC-CAT-030](../../../../test-cases/functional/product-content/TC-CAT-030.md) |
| AC-13 | **Given** the Content tab **When** an administrator adds, reorders, publishes and deletes items **Then** the product page reflects it and the Content tab passes the accessibility checks | [TC-CAT-031](../../../../test-cases/functional/product-content/TC-CAT-031.md), [TC-CAT-032](../../../../test-cases/functional/product-content/TC-CAT-032.md) |
