package com.vyoog.eisplatform.modules.orgdirectory.controller;

import com.vyoog.eisplatform.modules.orgdirectory.dto.OrgDirectoryDtos.*;
import com.vyoog.eisplatform.modules.orgdirectory.service.OrgDirectoryService;
import com.vyoog.eisplatform.modules.orghierarchy.dto.OrgHierarchyDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REQ-TEN-007 Organizations directory. Read-only. SecurityConfig requires
 * MANAGE_REGISTRATIONS for {@code /admin/organizations/**} (BR-DIR-001).
 */
@RestController
@RequestMapping("/admin/organizations")
@RequiredArgsConstructor
public class AdminOrganizationDirectoryController {

    private final OrgDirectoryService service;
    private final com.vyoog.eisplatform.modules.invitation.service.InvitationService invitationService;

    @GetMapping("/directory")
    public DirectoryDto directory() {
        return service.directory();
    }

    @GetMapping("/{id}")
    public OrganizationOverview overview(@PathVariable Long id) {
        return service.overview(id);
    }

    @GetMapping("/{id}/members")
    public List<MemberRow> members(@PathVariable Long id) {
        return service.members(id);
    }

    @GetMapping("/{id}/subscriptions")
    public List<SubscriptionRow> subscriptions(@PathVariable Long id) {
        return service.subscriptions(id);
    }

    @GetMapping("/{id}/invoices")
    public List<InvoiceRow> invoices(@PathVariable Long id) {
        return service.invoices(id);
    }

    @GetMapping("/{id}/tickets")
    public List<TicketRow> tickets(@PathVariable Long id) {
        return service.tickets(id);
    }

    @GetMapping("/{id}/invitations")
    public List<com.vyoog.eisplatform.modules.invitation.dto.InvitationDtos.InvitationDto> invitations(@PathVariable Long id) {
        return invitationService.adminList(id);
    }

    @GetMapping("/{id}/org-hierarchy")
    public OrgHierarchyDtos.TreeDto hierarchy(@PathVariable Long id) {
        return service.hierarchy(id);
    }

    @GetMapping("/{id}/org-hierarchy/nodes/{nodeId}")
    public OrgHierarchyDtos.NodeDetailDto hierarchyNode(@PathVariable Long id, @PathVariable Long nodeId) {
        return service.hierarchyNode(id, nodeId);
    }

    @GetMapping("/{id}/org-hierarchy/nodes/{nodeId}/history")
    public List<OrgHierarchyDtos.HistoryDto> hierarchyHistory(@PathVariable Long id, @PathVariable Long nodeId) {
        return service.hierarchyHistory(id, nodeId);
    }

    @GetMapping("/individuals/{customerId}")
    public IndividualDetail individual(@PathVariable Long customerId) {
        return service.individual(customerId);
    }
}
