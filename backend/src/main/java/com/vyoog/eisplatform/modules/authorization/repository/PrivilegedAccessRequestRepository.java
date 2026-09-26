package com.vyoog.eisplatform.modules.authorization.repository;

import com.vyoog.eisplatform.modules.authorization.model.PrivilegedAccessRequest;
import com.vyoog.eisplatform.modules.authorization.model.PrivilegedAccessStatus;
import com.vyoog.eisplatform.modules.authorization.model.RoleScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PrivilegedAccessRequestRepository extends JpaRepository<PrivilegedAccessRequest, Long> {

    List<PrivilegedAccessRequest> findByRequesterKeycloakSubOrderByRequestedAtDesc(String requesterKeycloakSub);

    List<PrivilegedAccessRequest> findByScopeAndOrganizationIdAndStatusOrderByRequestedAtDesc(
        RoleScope scope, Long organizationId, PrivilegedAccessStatus status);

    List<PrivilegedAccessRequest> findByScopeAndStatusOrderByRequestedAtDesc(RoleScope scope, PrivilegedAccessStatus status);

    /** The check every grant-aware permission lookup runs: is there a
     * currently-APPROVED, unexpired request for exactly this requester +
     * scope + org + permission. At most one should ever be active at a time
     * in practice, but this returns every match rather than assuming that. */
    List<PrivilegedAccessRequest> findByRequesterCustomerIdAndScopeAndOrganizationIdAndPermissionNameAndStatusAndExpiresAtAfter(
        Long requesterCustomerId, RoleScope scope, Long organizationId, String permissionName,
        PrivilegedAccessStatus status, Instant now);

    List<PrivilegedAccessRequest> findByRequesterKeycloakSubAndScopeAndPermissionNameAndStatusAndExpiresAtAfter(
        String requesterKeycloakSub, RoleScope scope, String permissionName, PrivilegedAccessStatus status, Instant now);

    Optional<PrivilegedAccessRequest> findByIdAndScope(Long id, RoleScope scope);

    /** REQ-IAM-004.7: approved grants that have not expired yet, soonest expiry first. */
    List<PrivilegedAccessRequest> findByScopeAndOrganizationIdAndStatusAndExpiresAtAfterOrderByExpiresAtAsc(
        RoleScope scope, Long organizationId, PrivilegedAccessStatus status, Instant now);

    List<PrivilegedAccessRequest> findByScopeAndStatusAndExpiresAtAfterOrderByExpiresAtAsc(
        RoleScope scope, PrivilegedAccessStatus status, Instant now);
}
