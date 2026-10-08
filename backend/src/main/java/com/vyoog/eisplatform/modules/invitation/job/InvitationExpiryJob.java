package com.vyoog.eisplatform.modules.invitation.job;

import com.vyoog.eisplatform.modules.invitation.service.InvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** REQ-TEN-008.5: hourly, marks overdue PENDING invitations EXPIRED. */
@Component
@RequiredArgsConstructor
@Slf4j
public class InvitationExpiryJob {

    private final InvitationService invitationService;

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void run() {
        int expired = invitationService.expireOverdue();
        if (expired > 0) {
            log.info("Expired {} overdue invitation(s)", expired);
        }
    }
}
