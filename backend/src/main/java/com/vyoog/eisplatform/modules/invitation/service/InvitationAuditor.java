package com.vyoog.eisplatform.modules.invitation.service;

import com.vyoog.eisplatform.modules.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Audit entries for invitation events. A refused acceptance rolls its own transaction back, so its
 * FAILURE entry is written in a separate transaction and survives (REQ-TEN-008.17).
 */
@Component
@RequiredArgsConstructor
public class InvitationAuditor {

    private final AuditService auditService;

    public void success(String action, Long actorCustomerId, String actorEmail, Long invitationId, Long organizationId, String detail) {
        auditService.recordSuccess(action, null, actorCustomerId, actorEmail, "OrganizationInvitation",
            invitationId == null ? null : invitationId.toString(), organizationId, detail);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failure(String action, Long actorCustomerId, String actorEmail, Long invitationId, Long organizationId, String detail) {
        auditService.record(action, null, actorCustomerId, actorEmail, "OrganizationInvitation",
            invitationId == null ? null : invitationId.toString(), organizationId, "FAILURE", detail);
    }
}
