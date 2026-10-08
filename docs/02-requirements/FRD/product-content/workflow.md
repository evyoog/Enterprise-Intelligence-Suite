# Workflow — Product content

## States
```mermaid
stateDiagram-v2
    [*] --> Draft : add item
    Draft --> Published : publish
    Published --> Draft : unpublish
    Published --> Published : edit or replace file (version + 1)
    Draft --> [*] : delete
    Published --> [*] : delete
```

## Uploading a file
```mermaid
sequenceDiagram
    participant A as Administrator (browser)
    participant API as EIS backend
    participant S3 as Private S3 bucket
    A->>API: POST upload-url (kind, file name, type, size)
    API-->>A: presigned PUT URL + upload key
    A->>S3: PUT file (progress, cancel)
    A->>API: POST / PUT item with the upload key
    API->>S3: HEAD (exists? size and type match?)
    API-->>A: item (Draft) or error
```

## Transitions
| From | To | Actor | Condition / rule | Side effects |
|---|---|---|---|---|
| (new) | Draft | Administrator | BR-PCON-001, -009 | Audit `PRODUCT_CONTENT_ADDED` |
| Draft | Published | Administrator | The item is complete (file or link present) | Audit `PRODUCT_CONTENT_PUBLISHED`; item appears on the product page |
| Published | Draft | Administrator | | Audit `PRODUCT_CONTENT_UNPUBLISHED`; item disappears |
| Draft / Published | same | Administrator | BR-PCON-008 | Version + 1, "Updated on", old file deleted; audit `PRODUCT_CONTENT_UPDATED` |
| any | deleted | Administrator | confirmation in the UI | Files deleted; audit `PRODUCT_CONTENT_DELETED` |
