# Screen: Organization structure (`/organization/structure`)

**Requirement:** [REQ-TEN-006](../../02-requirements/FRD/org-hierarchy/requirement.md) · **Access:** `MANAGE_ORGANIZATION`

- **Header:** title "Structure", actions *Add node*, *Import CSV*, *Configure levels*.
- **Left, tree:** search box (filters and highlights, expanding parents), expand/collapse all, one row per node (type chip, name, code, member count, "Inactive" chip). Keyboard: arrows move, Enter selects.
- **Right, details panel (selected node):** name, type, code, description, path, status, members (add/remove), tabs *Details* and *History*; actions *Add child*, *Edit*, *Move*, *Activate/Deactivate*, *Delete* (the root only offers *Edit* and *Add child*).
- **Dialogs:** node form (name, type limited to levels below the parent, code, description), move (parent picker excluding the node and its descendants), confirm deactivate (names the children/members), confirm delete, level configuration (ordered list, add, rename, remove, move up/down), CSV import (file, result summary with failed rows).
- **States:** loading, empty child list, error alert with retry; no data is shown signed out.
- **Not built:** org-chart canvas with zoom and mini-map ([C82](../../01-business/roadmap/open-decisions.md#c82)).
