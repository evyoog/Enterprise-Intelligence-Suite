package com.vyoog.eisplatform.modules.servicestatus.model;

/** C101 / REQ-SUP-003 (IC-2 A). Resolved needs a root cause; Closed only follows Resolved and needs every corrective action done. */
public enum ProblemStatus {
    OPEN, ROOT_CAUSE_IDENTIFIED, RESOLVED, CLOSED
}
