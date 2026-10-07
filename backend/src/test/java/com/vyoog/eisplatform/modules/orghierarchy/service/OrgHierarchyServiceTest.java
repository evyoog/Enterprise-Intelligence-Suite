package com.vyoog.eisplatform.modules.orghierarchy.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.orghierarchy.dto.OrgHierarchyDtos.*;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-TEN-006 Organization hierarchy (TC-TEN-027 to TC-TEN-038). */
@SpringBootTest
@ActiveProfiles("test")
class OrgHierarchyServiceTest {

    @Autowired
    private OrgHierarchyService service;
    @Autowired
    private OrganizationMemberService memberService;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Organization newOrganization() {
        Organization organization = new Organization();
        organization.setName("Hier Org");
        organization.setCode("HIER-" + System.nanoTime());
        organization.setBusinessEmail("biz@hier.example");
        organization.setCountry("India");
        organization.setLicensedSeats(10);
        return organizationRepository.save(organization);
    }

    private Customer newCustomer(String email) {
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setFirstName("Test");
        customer.setLastName("User");
        return customerRepository.save(customer);
    }

    private record Setup(Organization org, Long adminCustomerId, OrganizationMember member) {
    }

    private Setup setup() {
        Organization org = newOrganization();
        long n = System.nanoTime();
        var admin = memberService.addMember(org.getId(), newCustomer("a" + n + "@hier.example").getId(), OrgRole.ORG_ADMIN);
        var member = memberService.addMember(org.getId(), newCustomer("m" + n + "@hier.example").getId(), OrgRole.MEMBER);
        return new Setup(org, admin.getCustomerId(), member);
    }

    private Long rootId(Long adminId) {
        return service.tree(adminId).nodes().stream().filter(n -> n.parentId() == null).findFirst().orElseThrow().id();
    }

    private NodeDto add(Long adminId, Long parent, String name, String type) {
        return service.create(adminId, new CreateNodeRequest(parent, name, type, null, null, null));
    }

    @Test
    void firstOpenCreatesTheRootAndTheSevenDefaultLevels() {
        Setup s = setup();
        TreeDto tree = service.tree(s.adminCustomerId());
        assertThat(tree.nodes()).hasSize(1);
        assertThat(tree.nodes().get(0).name()).isEqualTo("Hier Org");
        assertThat(tree.nodes().get(0).type()).isEqualTo("ORGANIZATION");
        assertThat(tree.levels()).extracting(LevelDto::type).containsExactly(
            "ORGANIZATION", "DIVISION", "BUSINESS_UNIT", "DEPARTMENT", "LOCATION", "COST_CENTER", "TEAM");
        assertThat(service.tree(s.adminCustomerId()).nodes()).hasSize(1);
    }

    @Test
    void aChildMustBeOnALowerLevelThanItsParent() {
        Setup s = setup();
        Long root = rootId(s.adminCustomerId());
        NodeDto dept = add(s.adminCustomerId(), root, "Engineering", "department");
        assertThat(dept.type()).isEqualTo("DEPARTMENT");
        assertThatThrownBy(() -> add(s.adminCustomerId(), dept.id(), "Sub", "DIVISION"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> add(s.adminCustomerId(), dept.id(), "Sub", "DEPARTMENT"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> add(s.adminCustomerId(), dept.id(), "Sub", "NOPE"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(add(s.adminCustomerId(), dept.id(), "Platform", "TEAM").parentId()).isEqualTo(dept.id());
    }

    @Test
    void siblingNamesAreUniqueIgnoringCaseAndANodeCannotBeCreatedWithoutAParent() {
        Setup s = setup();
        Long root = rootId(s.adminCustomerId());
        add(s.adminCustomerId(), root, "Sales", "DIVISION");
        assertThatThrownBy(() -> add(s.adminCustomerId(), root, " sales ", "DIVISION"))
            .isInstanceOf(DuplicateResourceException.class);
        assertThatThrownBy(() -> add(s.adminCustomerId(), null, "Second root", "DIVISION"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void retypingMustKeepTheNodeAboveItsChildren() {
        Setup s = setup();
        Long root = rootId(s.adminCustomerId());
        NodeDto div = add(s.adminCustomerId(), root, "Ops", "DIVISION");
        add(s.adminCustomerId(), div.id(), "Support", "DEPARTMENT");
        assertThatThrownBy(() -> service.update(s.adminCustomerId(), div.id(),
            new UpdateNodeRequest("Ops", "TEAM", null, null, null, null, null)))
            .isInstanceOf(IllegalArgumentException.class);
        NodeDto bu = service.update(s.adminCustomerId(), div.id(),
            new UpdateNodeRequest("Operations", "BUSINESS_UNIT", "OP", "d", 3, null, null));
        assertThat(bu.type()).isEqualTo("BUSINESS_UNIT");
        assertThat(bu.name()).isEqualTo("Operations");
        assertThat(bu.code()).isEqualTo("OP");
    }

    @Test
    void movingWritesHistoryAndRefusesCyclesRootAndLevelViolations() {
        Setup s = setup();
        Long a = s.adminCustomerId();
        Long root = rootId(a);
        NodeDto d1 = add(a, root, "D1", "DIVISION");
        NodeDto d2 = add(a, root, "D2", "DIVISION");
        NodeDto dept = add(a, d1.id(), "Dept", "DEPARTMENT");
        NodeDto team = add(a, dept.id(), "Team", "TEAM");

        NodeDto moved = service.move(a, dept.id(), d2.id());
        assertThat(moved.parentId()).isEqualTo(d2.id());
        List<HistoryDto> history = service.history(a, dept.id());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).previousParentName()).isEqualTo("D1");
        assertThat(history.get(0).newParentName()).isEqualTo("D2");

        assertThatThrownBy(() -> service.move(a, d2.id(), team.id())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.move(a, d2.id(), d2.id())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.move(a, root, d1.id())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.move(a, d1.id(), team.id())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.move(a, dept.id(), null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(service.history(a, dept.id())).hasSize(1);
    }

    @Test
    void deactivatingAndDeletingAreGuardedByChildrenAndMembersAndTheRootIsProtected() {
        Setup s = setup();
        Long a = s.adminCustomerId();
        Long root = rootId(a);
        NodeDto div = add(a, root, "Div", "DIVISION");
        NodeDto dept = add(a, div.id(), "Dept", "DEPARTMENT");

        UpdateNodeRequest off = new UpdateNodeRequest("Div", "DIVISION", null, null, null, false, null);
        assertThatThrownBy(() -> service.update(a, div.id(), off)).isInstanceOf(InvalidStateException.class);
        NodeDto forced = service.update(a, div.id(), new UpdateNodeRequest("Div", "DIVISION", null, null, null, false, true));
        assertThat(forced.active()).isFalse();
        assertThat(service.update(a, div.id(), new UpdateNodeRequest("Div", "DIVISION", null, null, null, true, null)).active()).isTrue();

        assertThatThrownBy(() -> service.delete(a, div.id())).isInstanceOf(InvalidStateException.class);
        service.placeMember(a, dept.id(), s.member().getId());
        assertThatThrownBy(() -> service.delete(a, dept.id())).isInstanceOf(InvalidStateException.class);
        service.removeMember(a, dept.id(), s.member().getId());
        service.delete(a, dept.id());
        service.delete(a, div.id());
        assertThat(service.tree(a).nodes()).hasSize(1);

        assertThatThrownBy(() -> service.delete(a, root)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.update(a, root, new UpdateNodeRequest("Hier Org", "ORGANIZATION", null, null, null, false, true)))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.update(a, root, new UpdateNodeRequest("Hier Org", "DIVISION", null, null, null, null, null)))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(service.update(a, root, new UpdateNodeRequest("Renamed", "ORGANIZATION", null, "d", null, null, null)).name())
            .isEqualTo("Renamed");
    }

    @Test
    void membersCanBePlacedOnlyWithinTheirOwnOrganization() {
        Setup s = setup();
        Setup other = setup();
        Long a = s.adminCustomerId();
        Long root = rootId(a);
        NodeDetailDto placed = service.placeMember(a, root, s.member().getId());
        assertThat(placed.members()).extracting(NodeMemberDto::memberId).containsExactly(s.member().getId());
        assertThat(placed.node().memberCount()).isEqualTo(1);
        assertThatThrownBy(() -> service.placeMember(a, root, other.member().getId()))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThat(service.removeMember(a, root, s.member().getId()).members()).isEmpty();
    }

    @Test
    void anotherOrganizationsNodesAreNotFoundAndNonAdminsAreForbidden() {
        Setup s = setup();
        Setup other = setup();
        Long foreign = rootId(other.adminCustomerId());
        rootId(s.adminCustomerId());
        assertThatThrownBy(() -> service.detail(s.adminCustomerId(), foreign)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.delete(s.adminCustomerId(), foreign)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.move(s.adminCustomerId(), foreign, foreign)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.tree(s.member().getCustomerId())).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void csvImportCreatesGoodRowsAndReportsBadOnes() {
        Setup s = setup();
        Long a = s.adminCustomerId();
        String csv = "﻿Name,Type,Code,Description,Parent Name\r\n"
            + "Engineering,division,ENG,\"Builds, ships\",\r\n"
            + "Platform,Department,,,Engineering\r\n"
            + "Platform,Department,,,Engineering\r\n"
            + "Ghost,Team,,,Nowhere\r\n"
            + "Bad,Galaxy,,,\r\n"
            + "Top,Division,,,Platform\r\n"
            + "Core,Team,,,Platform\r\n";
        ImportResultDto result = service.importCsv(a, csv.getBytes(StandardCharsets.UTF_8));
        assertThat(result.created()).isEqualTo(3);
        assertThat(result.failed()).isEqualTo(4);
        assertThat(result.errors()).extracting(ImportError::row).containsExactly(4, 5, 6, 7);
        TreeDto tree = service.tree(a);
        assertThat(tree.nodes()).extracting(NodeDto::name).contains("Engineering", "Platform", "Core");
        assertThat(tree.nodes().stream().filter(n -> n.name().equals("Engineering")).findFirst().orElseThrow().description())
            .isEqualTo("Builds, ships");
        assertThatThrownBy(() -> service.importCsv(a, "foo,bar\r\n1,2".getBytes(StandardCharsets.UTF_8)))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.importCsv(a, new byte[0])).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void levelsCanBeReorderedExtendedAndRenamedButNotRemovedWhileInUse() {
        Setup s = setup();
        Long a = s.adminCustomerId();
        Long root = rootId(a);
        add(a, root, "Plant", "LOCATION");

        List<LevelInput> withoutLocation = List.of(new LevelInput("ORGANIZATION", null), new LevelInput("DIVISION", null),
            new LevelInput("TEAM", null));
        assertThatThrownBy(() -> service.updateLevels(a, new UpdateLevelsRequest(withoutLocation)))
            .isInstanceOf(InvalidStateException.class);
        assertThatThrownBy(() -> service.updateLevels(a, new UpdateLevelsRequest(List.of(new LevelInput("DIVISION", null)))))
            .isInstanceOf(IllegalArgumentException.class);

        List<LevelInput> ok = List.of(new LevelInput("ORGANIZATION", "Company"), new LevelInput("REGION", "Region"),
            new LevelInput("LOCATION", null), new LevelInput("TEAM", null));
        List<LevelDto> saved = service.updateLevels(a, new UpdateLevelsRequest(ok));
        assertThat(saved).extracting(LevelDto::type).containsExactly("ORGANIZATION", "REGION", "LOCATION", "TEAM");
        assertThat(saved.get(0).label()).isEqualTo("Company");
        assertThat(add(a, root, "EMEA", "region").type()).isEqualTo("REGION");
    }

    @Test
    void csvParserHandlesQuotesAndBlankLines() {
        var rows = OrgHierarchyService.parseCsv("a,\"b \"\"q\"\"\",c\n\n1,2,3");
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).containsExactly("a", "b \"q\"", "c");
    }
}
