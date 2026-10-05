package com.vyoog.eisplatform.modules.search.model;

/** Who may see an indexed record. PUBLIC: anyone (published catalog
 * products and published knowledge articles). OWNER: only the record's
 * owner (a support ticket's requester). Every query filters on this before
 * ranking (REQ-PRT-002 BR-SRCH-001). */
public enum SearchVisibility {
    PUBLIC,
    OWNER
}
