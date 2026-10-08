package com.vyoog.eisplatform.modules.orgdirectory.dto;

import com.vyoog.eisplatform.modules.registration.dto.OrganizationAdminDto;

import java.time.Instant;
import java.util.List;

/** REQ-TEN-007 Organizations directory payloads (read-only). */
public final class OrgDirectoryDtos {

    private OrgDirectoryDtos() {
    }

    public record DirectoryRow(String kind, Long id, String name, String code, String type, String industry,
                               String country, String state, String city, String email, String phone,
                               String contactName, String contactEmail, int seatsLicensed, long seatsUsed,
                               String status, String lifecycleStatus, Long regionId, String regionName,
                               Long parentOrganizationId, String parentOrganizationName, Instant createdAt,
                               boolean signInLinked, boolean mfaRequired, int productCount, int hierarchyNodes,
                               int openTickets, int outstandingInvoices, int profileCompletion,
                               List<String> missingFields) {
    }

    public record Summary(int total, int organizations, int individuals, int profileComplete,
                          int profileInProgress, int profileNotStarted, int averageCompletion) {
    }

    public record DirectoryDto(List<DirectoryRow> rows, Summary summary) {
    }

    public record Completion(int percent, List<String> missing) {
    }

    public record MemberRow(Long memberId, Long customerId, String name, String email, String orgRole, String status,
                            Instant joinedAt, Long orgNodeId, String orgNodeName) {
    }

    public record SubscriptionRow(Long id, Long productId, String productName, String status, int quantity,
                                  Instant startedAt, Instant expiresAt, boolean autoRenew) {
    }

    public record InvoiceRow(Long id, String number, String status, String currency, long total, Instant issuedAt,
                             Instant dueAt) {
    }

    public record TicketRow(Long id, String subject, String status, String priority, String requestedBy,
                            Instant createdAt) {
    }

    public record IndividualDetail(Long id, String firstName, String lastName, String email, String mobile,
                                   String country, String companyName, String jobTitle, String industry, String status,
                                   boolean signInLinked, Instant createdAt, Completion completion,
                                   List<SubscriptionRow> subscriptions, List<InvoiceRow> invoices,
                                   List<TicketRow> tickets) {
    }

    public record OrganizationOverview(OrganizationAdminDto organization, Completion completion, boolean mfaRequired) {
    }
}
