# Screen: Organization structure (tab "Org structure" of `/organization/members`)

> C83: the separate sidebar entry is gone; the screen is the third tab of **People & structure**. `/organization/structure` redirects to `/organization/members?tab=structure`. Platform administrators see the same chart read-only in the Structure tab of an organization ([admin-organization-detail.md](admin-organization-detail.md)).

**Requirement:** [REQ-TEN-006](../../02-requirements/FRD/org-hierarchy/requirement.md) · **Access:** `MANAGE_ORGANIZATION` · Cloned from the Thittam org-hierarchy page ([C82](../../01-business/roadmap/open-decisions.md#c82)).

- **Toolbar:** actions *Configure levels*, *Import CSV*, *Add node* (always enabled: adds under the selected node, or under the root when none is selected).
- **Toolbar:** search (name, code, type; matches are highlighted, other cards dimmed, ancestors opened), type filter chips (one per level), *Active only* switch, *Expand all*, *Collapse all*.
- **Org chart canvas:** one card per node joined by connector lines. A card shows the type icon, name, a *Root* tag on the root, type and code, a type chip, "Total: N" nodes beneath, "Inactive" when inactive, a **⋯ menu** (Add child, Edit, Move, Activate/Deactivate, Delete; the root offers only Add child and Edit) and a **Details** button. A round badge under a card shows how many children it has (click to expand) or a minus (click to collapse). The first two levels are open by default.
- **Navigation:** zoom bar (zoom out, percentage, zoom in, fit to view), mouse wheel zoom, drag to pan, and a **minimap** (click to jump).
- **Details panel (right, opens from *Details*):** name, type, code, path, status, description, children, members (place and remove), tabs *Details* and *History*; close button.
- **Dialogs:** node form (types limited to levels below the parent), move (parent picker excluding the node and its descendants), confirm deactivate (names the children/members), confirm delete, **Configure levels** (ordered list, add, rename, remove, move up/down), **Import CSV** (columns explained, a sample file shown and downloadable as `org-structure-sample.csv`, result summary with failed rows).
- **States:** loading, error alert with retry, "no match" message; nothing is shown signed out.
- **Not built:** role assignments and working calendar per node (C82).
