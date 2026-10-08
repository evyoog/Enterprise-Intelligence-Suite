package com.vyoog.eisplatform.modules.invitation.repository;

import com.vyoog.eisplatform.modules.invitation.model.InvitationStatus;
import com.vyoog.eisplatform.modules.invitation.model.OrganizationInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<OrganizationInvitation, Long> {

    Optional<OrganizationInvitation> findByTokenHash(String tokenHash);

    List<OrganizationInvitation> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    List<OrganizationInvitation> findByOrganizationIdAndInvitedByCustomerIdOrderByCreatedAtDesc(Long organizationId, Long invitedByCustomerId);

    Optional<OrganizationInvitation> findByOrganizationIdAndNormalizedEmailAndStatus(Long organizationId, String normalizedEmail, InvitationStatus status);

    List<OrganizationInvitation> findByStatusAndExpiresAtBefore(InvitationStatus status, Instant instant);
}
