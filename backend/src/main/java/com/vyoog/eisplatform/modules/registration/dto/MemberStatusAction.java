package com.vyoog.eisplatform.modules.registration.dto;

/** 05.03.01 User Lifecycle (sprint 2026.4.1) — the actions available on
 * {@code PATCH /organization/me/members/{id}/status}. INVITE and CREATE are
 * not in this list: identity creation (05.03.01.01/.02) still goes through
 * registration or first SSO sign-in (see FederatedAccountService); this
 * action set only re-states ACTIVATE/SUSPEND/REMOVE against an existing
 * member row. */
public enum MemberStatusAction {
    SUSPEND,
    REACTIVATE,
    REMOVE
}
