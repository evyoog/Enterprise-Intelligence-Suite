package com.vyoog.eisplatform.modules.servicestatus.model;

/** C101 / REQ-SUP-003 (IC-1 A): where an incident is in its handling. Resolved sets the end time and needs a resolution summary; Closed only follows Resolved. */
public enum IncidentPhase {
    LOGGED, INVESTIGATING, RESOLVED, CLOSED
}
