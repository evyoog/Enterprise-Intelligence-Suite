# Business rules — Organizations directory

| ID | Rule |
|---|---|
| BR-DIR-001 | Only holders of `MANAGE_REGISTRATIONS` read the directory and the detail tabs. |
| BR-DIR-002 | An individual is a customer who belongs to no organization (as in the former Individuals tab). |
| BR-DIR-003 | Profile completion = filled items / total items, rounded to a whole percent. A blank or whitespace-only value is not filled. 100 = Complete, 1–99 = In progress, 0 = Not started. |
| BR-DIR-004 | Organization items (12): type, industry, website, phone, country, state, city, address, tax registration (GSTIN, PAN, company registration number or VAT number: any one), billing address (filled when "same as address" is on and the address is filled, otherwise billing address and billing country), region, administrator contact. Name, code and business email are mandatory at registration and are not counted. |
| BR-DIR-005 | Individual items (5): mobile, country, company name, job title, industry. |
| BR-DIR-006 | Seats: "Full" = used equals licensed, "Over limit" = used above licensed, "Available" = used below licensed. Only organizations have seats. |
| BR-DIR-007 | Open tickets = tickets in OPEN, IN_PROGRESS or ESCALATED raised by the organization's members (or by the individual). Outstanding invoices = invoices in status OPEN owned by the organization (or the individual). |
| BR-DIR-008 | The hierarchy shown to the platform administrator is read-only and never creates the root; an organization that has not opened its structure yet shows an empty chart. |
