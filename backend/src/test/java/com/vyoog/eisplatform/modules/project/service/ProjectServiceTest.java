package com.vyoog.eisplatform.modules.project.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.project.dto.ProjectDtos.*;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.*;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-TEN-009 Projects (TC-TEN-060 to TC-TEN-066): a project is a group plus products (C88). */
@SpringBootTest
@ActiveProfiles("test")
class ProjectServiceTest {

    @Autowired private ProjectService service;
    @Autowired private OrganizationSelfService selfService;
    @Autowired private OrganizationMemberService memberService;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private OrganizationProductAccessRepository accessRepository;
    @Autowired private OrganizationGroupRepository groupRepository;
    @Autowired private NotificationService notificationService;

    private Organization org() {
        Organization o = new Organization();
        o.setName("Prj Org");
        o.setCode("PRJ-" + System.nanoTime());
        o.setBusinessEmail("biz@prj.example");
        o.setCountry("India");
        o.setLicensedSeats(20);
        return organizationRepository.save(o);
    }

    private Customer customer(String prefix) {
        Customer c = new Customer();
        c.setEmail(prefix + System.nanoTime() + "@prj.example");
        c.setFirstName("Pat");
        c.setLastName("Lee");
        return customerRepository.save(c);
    }

    private OrganizationMember member(Organization o, OrgRole role) {
        return memberService.addMember(o.getId(), customer("m").getId(), role);
    }

    private Product product(String name) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(BigDecimal.TEN);
        return productRepository.save(p);
    }

    private void subscribe(Organization o, Product p) {
        ProductSubscription s = new ProductSubscription();
        s.setProductId(p.getId());
        s.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        s.setOwnerOrganizationId(o.getId());
        s.setQuantity(20);
        s.setStatus(SubscriptionStatus.ACTIVE);
        s.setStartedAt(Instant.now());
        subscriptionRepository.save(s);
    }

    private OrganizationProductAccess access(OrganizationMember m, Product p) {
        return accessRepository.findByOrganizationMemberIdAndProductId(m.getId(), p.getId()).orElse(null);
    }

    private ProjectRequest req(String name) {
        return new ProjectRequest(name, null);
    }

    @Test
    void anAdministratorCreatesRenamesAndDeletesProjectsWithUniqueNames() {
        Organization o = org();
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);

        ProjectDto p = service.create(admin.getCustomerId(), new ProjectRequest("  Mumbai rollout ", "First wave"));
        assertThat(p.name()).isEqualTo("Mumbai rollout");
        assertThat(p.description()).isEqualTo("First wave");
        assertThat(p.members()).isEmpty();
        assertThat(p.products()).isEmpty();

        assertThatThrownBy(() -> service.create(admin.getCustomerId(), req("MUMBAI ROLLOUT"))).isInstanceOf(DuplicateResourceException.class);
        assertThatThrownBy(() -> service.create(admin.getCustomerId(), req("  "))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(admin.getCustomerId(), new ProjectRequest("x", "y".repeat(501)))).isInstanceOf(IllegalArgumentException.class);

        ProjectDto other = service.create(admin.getCustomerId(), req("Delhi"));
        assertThatThrownBy(() -> service.update(admin.getCustomerId(), other.id(), req("mumbai rollout"))).isInstanceOf(DuplicateResourceException.class);
        assertThat(service.update(admin.getCustomerId(), other.id(), new ProjectRequest("Delhi 2", "Second")).name()).isEqualTo("Delhi 2");
        assertThat(service.list(admin.getCustomerId())).extracting(ProjectDto::name).containsExactly("Delhi 2", "Mumbai rollout");

        service.delete(admin.getCustomerId(), other.id());
        assertThat(service.list(admin.getCustomerId())).extracting(ProjectDto::name).containsExactly("Mumbai rollout");
    }

    @Test
    void onlyActiveMembersOfTheSameOrganizationCanBeAdded() {
        Organization o = org();
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember plain = member(o, OrgRole.MEMBER);
        OrganizationMember gone = member(o, OrgRole.MEMBER);
        memberService.removeMember(gone.getId());
        OrganizationMember stranger = member(org(), OrgRole.MEMBER);
        ProjectDto p = service.create(admin.getCustomerId(), req("Team"));

        ProjectDto withOne = service.addMember(admin.getCustomerId(), p.id(), plain.getId());
        assertThat(withOne.members()).extracting(ProjectMemberDto::memberId).containsExactly(plain.getId());
        assertThat(service.addMember(admin.getCustomerId(), p.id(), plain.getId()).members()).hasSize(1);

        assertThatThrownBy(() -> service.addMember(admin.getCustomerId(), p.id(), stranger.getId())).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.addMember(admin.getCustomerId(), p.id(), gone.getId())).isInstanceOf(InvalidStateException.class);
        assertThat(service.removeMember(admin.getCustomerId(), p.id(), plain.getId()).members()).isEmpty();
        assertThat(service.removeMember(admin.getCustomerId(), p.id(), plain.getId()).members()).isEmpty();
    }

    @Test
    void aProductNeedsAnActiveSubscriptionAnActiveProductAndARole() {
        Organization o = org();
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        ProjectDto p = service.create(admin.getCustomerId(), req("Team"));
        Product unsubscribed = product("Unsubscribed");
        Product retired = product("Retired");
        subscribe(o, retired);
        retired.setStatus(ProductStatus.INACTIVE);
        productRepository.save(retired);
        Product ok = product("Valam");
        subscribe(o, ok);

        assertThatThrownBy(() -> service.setProduct(admin.getCustomerId(), p.id(), new ProductRequest(unsubscribed.getId(), "USER")))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("active subscription");
        assertThatThrownBy(() -> service.setProduct(admin.getCustomerId(), p.id(), new ProductRequest(retired.getId(), "USER")))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not currently available");
        assertThatThrownBy(() -> service.setProduct(admin.getCustomerId(), p.id(), new ProductRequest(ok.getId(), " ")))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.setProduct(admin.getCustomerId(), p.id(), new ProductRequest(null, "USER")))
            .isInstanceOf(IllegalArgumentException.class);

        ProjectDto added = service.setProduct(admin.getCustomerId(), p.id(), new ProductRequest(ok.getId(), "PMS_USER"));
        assertThat(added.products()).extracting(ProjectProductDto::productName, ProjectProductDto::productRole).containsExactly(org.assertj.core.groups.Tuple.tuple("Valam", "PMS_USER"));
        assertThat(service.setProduct(admin.getCustomerId(), p.id(), new ProductRequest(ok.getId(), "PMS_ADMIN")).products()).hasSize(1)
            .first().extracting(ProjectProductDto::productRole).isEqualTo("PMS_ADMIN");
    }

    @Test
    void projectMembersGetTheProjectsProductsAndLoseThemAgain() {
        Organization o = org();
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember a = member(o, OrgRole.MEMBER);
        OrganizationMember b = member(o, OrgRole.MEMBER);
        Product valam = product("Valam");
        Product thittam = product("Thittam");
        subscribe(o, valam);
        subscribe(o, thittam);
        ProjectDto p = service.create(admin.getCustomerId(), req("Rollout"));

        service.addMember(admin.getCustomerId(), p.id(), a.getId());
        service.setProduct(admin.getCustomerId(), p.id(), new ProductRequest(valam.getId(), "USER"));
        assertThat(access(a, valam)).satisfies(x -> {
            assertThat(x.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
            assertThat(x.getProductRole()).isEqualTo("USER");
            assertThat(x.getSourceGroupId()).isEqualTo(p.id());
        });
        assertThat(access(b, valam)).isNull();
        assertThat(notificationService.listNotifications(a.getCustomerId()).notifications())
            .extracting(n -> n.title()).containsExactly("Access granted: Valam");

        // joining later grants every product; a role change updates the access without a second notice
        service.addMember(admin.getCustomerId(), p.id(), b.getId());
        service.setProduct(admin.getCustomerId(), p.id(), new ProductRequest(thittam.getId(), "MANAGER"));
        service.setProduct(admin.getCustomerId(), p.id(), new ProductRequest(valam.getId(), "ADMIN"));
        assertThat(access(b, valam).getProductRole()).isEqualTo("ADMIN");
        assertThat(access(a, thittam).getProductRole()).isEqualTo("MANAGER");
        assertThat(notificationService.listNotifications(a.getCustomerId()).notifications()).hasSize(2);

        // removing the product, then the member, then the project takes the access back
        service.removeProduct(admin.getCustomerId(), p.id(), thittam.getId());
        assertThat(access(a, thittam).getStatus()).isEqualTo(MembershipStatus.INACTIVE);
        assertThat(access(a, thittam).getSourceGroupId()).isNull();
        assertThat(access(a, valam).getStatus()).isEqualTo(MembershipStatus.ACTIVE);

        service.removeMember(admin.getCustomerId(), p.id(), a.getId());
        assertThat(access(a, valam).getStatus()).isEqualTo(MembershipStatus.INACTIVE);
        assertThat(access(b, valam).getStatus()).isEqualTo(MembershipStatus.ACTIVE);

        service.delete(admin.getCustomerId(), p.id());
        assertThat(access(b, valam).getStatus()).isEqualTo(MembershipStatus.INACTIVE);
        assertThat(groupRepository.findById(p.id())).isEmpty();
    }

    @Test
    void aDirectAssignmentWinsOverAProjectAndAProjectGrantCannotBeRevokedDirectly() {
        Organization o = org();
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember a = member(o, OrgRole.MEMBER);
        OrganizationMember b = member(o, OrgRole.MEMBER);
        Product valam = product("Valam");
        subscribe(o, valam);
        ProjectDto p = service.create(admin.getCustomerId(), new ProjectRequest("Rollout", null));

        // a has direct access first: the project leaves it alone, even when the project is later deleted
        selfService.assignProductAccess(admin.getCustomerId(), a.getId(), valam.getId(), "OWNER");
        service.addMember(admin.getCustomerId(), p.id(), a.getId());
        service.addMember(admin.getCustomerId(), p.id(), b.getId());
        service.setProduct(admin.getCustomerId(), p.id(), new ProductRequest(valam.getId(), "USER"));
        assertThat(access(a, valam)).satisfies(x -> {
            assertThat(x.getProductRole()).isEqualTo("OWNER");
            assertThat(x.getSourceGroupId()).isNull();
        });

        // b has it from the project: a direct revoke is refused with the project name
        assertThatThrownBy(() -> selfService.revokeProductAccess(admin.getCustomerId(), b.getId(), valam.getId()))
            .isInstanceOf(InvalidStateException.class).hasMessageContaining("Rollout");
        assertThat(access(b, valam).getStatus()).isEqualTo(MembershipStatus.ACTIVE);

        // a direct assignment over project access turns it into direct access that survives the project
        selfService.assignProductAccess(admin.getCustomerId(), b.getId(), valam.getId(), "SUPPORT");
        assertThat(access(b, valam).getSourceGroupId()).isNull();
        service.delete(admin.getCustomerId(), p.id());
        assertThat(access(a, valam).getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(access(b, valam)).satisfies(x -> {
            assertThat(x.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
            assertThat(x.getProductRole()).isEqualTo("SUPPORT");
        });
        selfService.revokeProductAccess(admin.getCustomerId(), b.getId(), valam.getId());
        assertThat(access(b, valam).getStatus()).isEqualTo(MembershipStatus.INACTIVE);
    }

    @Test
    void aMemberInTwoProjectsKeepsTheProductWhileOneSourceRemains() {
        Organization o = org();
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember a = member(o, OrgRole.MEMBER);
        Product valam = product("Valam");
        subscribe(o, valam);
        ProjectDto first = service.create(admin.getCustomerId(), req("First"));
        ProjectDto second = service.create(admin.getCustomerId(), req("Second"));
        service.setProduct(admin.getCustomerId(), first.id(), new ProductRequest(valam.getId(), "USER"));
        service.setProduct(admin.getCustomerId(), second.id(), new ProductRequest(valam.getId(), "MANAGER"));
        service.addMember(admin.getCustomerId(), first.id(), a.getId());
        service.addMember(admin.getCustomerId(), second.id(), a.getId());
        assertThat(accessRepository.findByOrganizationMemberId(a.getId())).hasSize(1);
        assertThat(access(a, valam).getSourceGroupId()).isEqualTo(first.id());
        assertThat(notificationService.listNotifications(a.getCustomerId()).notifications()).hasSize(1);

        service.removeMember(admin.getCustomerId(), first.id(), a.getId());
        assertThat(access(a, valam)).satisfies(x -> {
            assertThat(x.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
            assertThat(x.getSourceGroupId()).isEqualTo(second.id());
            assertThat(x.getProductRole()).isEqualTo("MANAGER");
        });
        service.removeProduct(admin.getCustomerId(), second.id(), valam.getId());
        assertThat(access(a, valam).getStatus()).isEqualTo(MembershipStatus.INACTIVE);
    }

    @Test
    void permissionsAndOrganizationScopeAreEnforcedAndGroupsStayApart() {
        Organization o = org();
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember plain = member(o, OrgRole.MEMBER);
        Product valam = product("Valam");
        subscribe(o, valam);
        ProjectDto p = service.create(admin.getCustomerId(), req("Team"));

        assertThatThrownBy(() -> service.list(plain.getCustomerId())).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.create(plain.getCustomerId(), req("Nope"))).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.addMember(plain.getCustomerId(), p.id(), plain.getId())).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.setProduct(plain.getCustomerId(), p.id(), new ProductRequest(valam.getId(), "USER"))).isInstanceOf(ForbiddenException.class);

        OrganizationMember otherAdmin = member(org(), OrgRole.ORG_ADMIN);
        assertThatThrownBy(() -> service.update(otherAdmin.getCustomerId(), p.id(), req("Hijack"))).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.delete(otherAdmin.getCustomerId(), p.id())).isInstanceOf(ResourceNotFoundException.class);
        assertThat(service.list(otherAdmin.getCustomerId())).isEmpty();

        // plain groups are untouched: the Groups list shows no project, and the group endpoints do not find one
        selfService.createGroup(admin.getCustomerId(), "Team");
        assertThat(selfService.listMyOrgGroups(admin.getCustomerId())).extracting(g -> g.name()).containsExactly("Team");
        assertThat(selfService.listMyOrgGroups(admin.getCustomerId())).extracting(g -> g.id()).doesNotContain(p.id());
        assertThatThrownBy(() -> selfService.addGroupMember(admin.getCustomerId(), p.id(), plain.getId())).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> selfService.deleteGroup(admin.getCustomerId(), p.id())).isInstanceOf(ResourceNotFoundException.class);
        assertThat(service.list(admin.getCustomerId())).hasSize(1);
    }
}
