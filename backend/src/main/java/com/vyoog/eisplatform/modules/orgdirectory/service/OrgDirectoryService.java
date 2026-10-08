package com.vyoog.eisplatform.modules.orgdirectory.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.orgdirectory.dto.OrgDirectoryDtos.*;
import com.vyoog.eisplatform.modules.orghierarchy.dto.OrgHierarchyDtos;
import com.vyoog.eisplatform.modules.orghierarchy.service.OrgHierarchyService;
import com.vyoog.eisplatform.modules.administration.model.PlatformRegion;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.administration.repository.PlatformRegionRepository;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationAdminDto;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.*;
import com.vyoog.eisplatform.modules.registration.service.AdminRegistrationService;
import com.vyoog.eisplatform.modules.support.model.SupportTicket;
import com.vyoog.eisplatform.modules.support.model.TicketStatus;
import com.vyoog.eisplatform.modules.support.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REQ-TEN-007 Organizations directory: one read-only view over organizations and individuals.
 * Access (MANAGE_REGISTRATIONS) is enforced by the security configuration on /admin/organizations/**.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrgDirectoryService {

    private static final Set<TicketStatus> OPEN_TICKETS = EnumSet.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.ESCALATED);

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final CustomerRepository customerRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final SupportTicketRepository ticketRepository;
    private final ProductRepository productRepository;
    private final PlatformRegionRepository regionRepository;
    private final AdminRegistrationService adminRegistrationService;
    private final OrgHierarchyService hierarchyService;

    public DirectoryDto directory() {
        List<Organization> orgs = organizationRepository.findAll();
        List<OrganizationMember> members = memberRepository.findAll();
        Map<Long, Customer> customers = customerRepository.findAll().stream().collect(Collectors.toMap(Customer::getId, c -> c));
        Map<Long, List<OrganizationMember>> membersByOrg = members.stream().collect(Collectors.groupingBy(OrganizationMember::getOrganizationId));
        Map<Long, Long> orgOfCustomer = new HashMap<>();
        members.forEach(m -> orgOfCustomer.putIfAbsent(m.getCustomerId(), m.getOrganizationId()));
        Map<Long, String> orgNames = orgs.stream().collect(Collectors.toMap(Organization::getId, Organization::getName));
        Map<Long, String> regionNames = regionRepository.findAll().stream().collect(Collectors.toMap(PlatformRegion::getId, PlatformRegion::getName));

        Map<Long, Integer> productsByOrg = new HashMap<>();
        Map<Long, Integer> productsByCustomer = new HashMap<>();
        for (ProductSubscription s : subscriptionRepository.findAll()) {
            if (s.getStatus() == SubscriptionStatus.CANCELLED || s.getStatus() == SubscriptionStatus.EXPIRED) {
                continue;
            }
            if (s.getOwnerOrganizationId() != null) {
                productsByOrg.merge(s.getOwnerOrganizationId(), 1, Integer::sum);
            } else if (s.getOwnerCustomerId() != null) {
                productsByCustomer.merge(s.getOwnerCustomerId(), 1, Integer::sum);
            }
        }
        Map<Long, Integer> invoicesByOrg = new HashMap<>();
        Map<Long, Integer> invoicesByCustomer = new HashMap<>();
        for (Invoice i : invoiceRepository.findByStatusIn(List.of(InvoiceStatus.OPEN))) {
            if (i.getOwnerOrganizationId() != null) {
                invoicesByOrg.merge(i.getOwnerOrganizationId(), 1, Integer::sum);
            } else if (i.getOwnerCustomerId() != null) {
                invoicesByCustomer.merge(i.getOwnerCustomerId(), 1, Integer::sum);
            }
        }
        Map<Long, Integer> ticketsByOrg = new HashMap<>();
        Map<Long, Integer> ticketsByCustomer = new HashMap<>();
        for (SupportTicket t : ticketRepository.findAllByOrderByCreatedAtDesc()) {
            if (!OPEN_TICKETS.contains(t.getStatus())) {
                continue;
            }
            Long org = orgOfCustomer.get(t.getRequestedByCustomerId());
            if (org != null) {
                ticketsByOrg.merge(org, 1, Integer::sum);
            } else {
                ticketsByCustomer.merge(t.getRequestedByCustomerId(), 1, Integer::sum);
            }
        }

        List<DirectoryRow> rows = new ArrayList<>();
        for (Organization o : orgs) {
            List<OrganizationMember> ms = membersByOrg.getOrDefault(o.getId(), List.of());
            long active = ms.stream().filter(m -> m.getStatus() == MembershipStatus.ACTIVE).count();
            Customer admin = ms.stream().filter(m -> m.getOrgRole() == OrgRole.ORG_ADMIN).findFirst()
                .map(m -> customers.get(m.getCustomerId())).orElse(null);
            Completion completion = ProfileCompleteness.organization(o, admin != null);
            rows.add(new DirectoryRow("ORGANIZATION", o.getId(), o.getName(), o.getCode(), o.getType(), o.getIndustry(),
                o.getCountry(), o.getState(), o.getCity(), o.getBusinessEmail(), o.getPhone(),
                admin == null ? null : fullName(admin), admin == null ? null : admin.getEmail(),
                o.getLicensedSeats(), active, o.getStatus().name(), o.getLifecycleStatus().name(), o.getRegionId(),
                o.getRegionId() == null ? null : regionNames.get(o.getRegionId()), o.getParentOrganizationId(),
                o.getParentOrganizationId() == null ? null : orgNames.get(o.getParentOrganizationId()), o.getCreatedAt(),
                admin != null && admin.getKeycloakSub() != null, o.isMfaRequired(), productsByOrg.getOrDefault(o.getId(), 0),
                hierarchyService.nodeCount(o.getId()), ticketsByOrg.getOrDefault(o.getId(), 0),
                invoicesByOrg.getOrDefault(o.getId(), 0), completion.percent(), completion.missing()));
        }
        for (Customer c : customers.values()) {
            if (orgOfCustomer.containsKey(c.getId())) {
                continue;
            }
            Completion completion = ProfileCompleteness.individual(c);
            rows.add(new DirectoryRow("INDIVIDUAL", c.getId(), fullName(c), null, null, c.getIndustry(), c.getCountry(),
                null, null, c.getEmail(), c.getMobile(), fullName(c), c.getEmail(), 0, 0, c.getStatus().name(), null,
                null, null, null, null, c.getCreatedAt(), c.getKeycloakSub() != null, false,
                productsByCustomer.getOrDefault(c.getId(), 0), 0, ticketsByCustomer.getOrDefault(c.getId(), 0),
                invoicesByCustomer.getOrDefault(c.getId(), 0), completion.percent(), completion.missing()));
        }
        rows.sort(Comparator.comparing(DirectoryRow::createdAt, Comparator.nullsLast(Comparator.reverseOrder())));
        int complete = (int) rows.stream().filter(r -> r.profileCompletion() == 100).count();
        int none = (int) rows.stream().filter(r -> r.profileCompletion() == 0).count();
        int orgsCount = (int) rows.stream().filter(r -> r.kind().equals("ORGANIZATION")).count();
        int avg = rows.isEmpty() ? 0 : (int) Math.round(rows.stream().mapToInt(DirectoryRow::profileCompletion).average().orElse(0));
        return new DirectoryDto(rows, new Summary(rows.size(), orgsCount, rows.size() - orgsCount, complete,
            rows.size() - complete - none, none, avg));
    }

    public OrganizationOverview overview(Long organizationId) {
        OrganizationAdminDto dto = adminRegistrationService.getOrganization(organizationId);
        Organization o = organizationRepository.findById(organizationId).orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        return new OrganizationOverview(dto, ProfileCompleteness.organization(o, dto.adminEmail() != null), o.isMfaRequired());
    }

    public List<MemberRow> members(Long organizationId) {
        requireOrganization(organizationId);
        Map<Long, String> nodeNames = new HashMap<>();
        hierarchyService.adminTree(organizationId).nodes().forEach(n -> nodeNames.put(n.id(), n.name()));
        return memberRepository.findByOrganizationId(organizationId).stream().map(m -> {
            Customer c = customerRepository.findById(m.getCustomerId()).orElse(null);
            return new MemberRow(m.getId(), m.getCustomerId(), c == null ? null : fullName(c), c == null ? null : c.getEmail(),
                m.getOrgRole().name(), m.getStatus().name(), m.getJoinedAt(), m.getOrgNodeId(),
                m.getOrgNodeId() == null ? null : nodeNames.get(m.getOrgNodeId()));
        }).toList();
    }

    public List<SubscriptionRow> subscriptions(Long organizationId) {
        requireOrganization(organizationId);
        return subscriptionRows(subscriptionRepository.findByOwnerOrganizationId(organizationId));
    }

    public List<InvoiceRow> invoices(Long organizationId) {
        requireOrganization(organizationId);
        return invoiceRepository.findByOwnerOrganizationIdOrderByIssuedAtDesc(organizationId, PageRequest.of(0, 200))
            .map(this::invoiceRow).getContent();
    }

    public List<TicketRow> tickets(Long organizationId) {
        requireOrganization(organizationId);
        Set<Long> customerIds = memberRepository.findByOrganizationId(organizationId).stream()
            .map(OrganizationMember::getCustomerId).collect(Collectors.toSet());
        return ticketRows(ticketRepository.findAllByOrderByCreatedAtDesc().stream()
            .filter(t -> customerIds.contains(t.getRequestedByCustomerId())).toList());
    }

    public IndividualDetail individual(Long customerId) {
        Customer c = customerRepository.findById(customerId).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (!memberRepository.findAll().stream().noneMatch(m -> m.getCustomerId().equals(customerId))) {
            throw new ResourceNotFoundException("Customer not found");
        }
        List<InvoiceRow> invoices = invoiceRepository.findByOwnerCustomerIdOrderByIssuedAtDesc(customerId, PageRequest.of(0, 200))
            .map(this::invoiceRow).getContent();
        return new IndividualDetail(c.getId(), c.getFirstName(), c.getLastName(), c.getEmail(), c.getMobile(), c.getCountry(),
            c.getCompanyName(), c.getJobTitle(), c.getIndustry(), c.getStatus().name(), c.getKeycloakSub() != null,
            c.getCreatedAt(), ProfileCompleteness.individual(c),
            subscriptionRows(subscriptionRepository.findByOwnerCustomerId(customerId)), invoices,
            ticketRows(ticketRepository.findByRequestedByCustomerIdOrderByCreatedAtDesc(customerId)));
    }

    public OrgHierarchyDtos.TreeDto hierarchy(Long organizationId) {
        requireOrganization(organizationId);
        return hierarchyService.adminTree(organizationId);
    }

    public OrgHierarchyDtos.NodeDetailDto hierarchyNode(Long organizationId, Long nodeId) {
        requireOrganization(organizationId);
        return hierarchyService.adminDetail(organizationId, nodeId);
    }

    public List<OrgHierarchyDtos.HistoryDto> hierarchyHistory(Long organizationId, Long nodeId) {
        requireOrganization(organizationId);
        return hierarchyService.adminHistory(organizationId, nodeId);
    }

    private void requireOrganization(Long organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
    }

    private List<SubscriptionRow> subscriptionRows(List<ProductSubscription> subs) {
        Map<Long, String> names = productRepository.findAllById(subs.stream().map(ProductSubscription::getProductId).toList())
            .stream().collect(Collectors.toMap(Product::getId, Product::getName));
        return subs.stream().map(s -> new SubscriptionRow(s.getId(), s.getProductId(), names.get(s.getProductId()),
            s.getStatus().name(), s.getQuantity(), s.getStartedAt(), s.getExpiresAt(), s.isAutoRenew())).toList();
    }

    private InvoiceRow invoiceRow(Invoice i) {
        return new InvoiceRow(i.getId(), i.getInvoiceNumber(), i.getStatus().name(),
            i.getCurrency() == null ? null : i.getCurrency().name(), i.getTotal(), i.getIssuedAt(), i.getDueAt());
    }

    private List<TicketRow> ticketRows(List<SupportTicket> tickets) {
        Map<Long, String> names = new HashMap<>();
        return tickets.stream().map(t -> new TicketRow(t.getId(), t.getSubject(), t.getStatus().name(), t.getPriority().name(),
            names.computeIfAbsent(t.getRequestedByCustomerId(),
                id -> customerRepository.findById(id).map(OrgDirectoryService::fullName).orElse(null)), t.getCreatedAt())).toList();
    }

    private static String fullName(Customer c) {
        return ((c.getFirstName() == null ? "" : c.getFirstName()) + " " + (c.getLastName() == null ? "" : c.getLastName())).trim();
    }
}
