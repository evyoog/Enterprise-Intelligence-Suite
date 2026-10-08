package com.vyoog.eisplatform.modules.orgdirectory.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.orgdirectory.dto.OrgDirectoryDtos.*;
import com.vyoog.eisplatform.modules.orghierarchy.service.OrgHierarchyService;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-TEN-007 Organizations directory (TC-TEN-039 to TC-TEN-044). */
@SpringBootTest
@ActiveProfiles("test")
class OrgDirectoryServiceTest {

    @Autowired
    private OrgDirectoryService service;
    @Autowired
    private OrgHierarchyService hierarchyService;
    @Autowired
    private OrganizationMemberService memberService;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Organization org(String name, boolean full) {
        Organization o = new Organization();
        o.setName(name);
        o.setCode("DIR-" + System.nanoTime());
        o.setBusinessEmail("biz@dir.example");
        o.setCountry("India");
        o.setLicensedSeats(2);
        if (full) {
            o.setType("Company");
            o.setIndustry("Software");
            o.setWebsite("https://dir.example");
            o.setPhone("123");
            o.setState("KA");
            o.setCity("Bengaluru");
            o.setAddress("1 Main Road");
            o.setGstin("GST1");
            o.setBillingSameAsAddress(true);
            o.setRegionId(1L);
        }
        return organizationRepository.save(o);
    }

    private Customer customer(String email, boolean full) {
        Customer c = new Customer();
        c.setEmail(email);
        c.setFirstName("Dina");
        c.setLastName("Rao");
        if (full) {
            c.setMobile("999");
            c.setCountry("India");
            c.setCompanyName("Acme");
            c.setJobTitle("CTO");
            c.setIndustry("Software");
        }
        return customerRepository.save(c);
    }

    private DirectoryRow row(DirectoryDto d, String kind, Long id) {
        return d.rows().stream().filter(r -> r.kind().equals(kind) && r.id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void directoryListsOrganizationsAndIndividualsTogetherWithProfileCompletion() {
        Organization empty = org("Empty Co", false);
        Customer indFull = customer("full" + System.nanoTime() + "@dir.example", true);
        Customer indEmpty = customer("empty" + System.nanoTime() + "@dir.example", false);
        Customer adminC = customer("admin" + System.nanoTime() + "@dir.example", false);
        Organization full = org("Full Co", true);
        memberService.addMember(full.getId(), adminC.getId(), OrgRole.ORG_ADMIN);

        DirectoryDto d = service.directory();
        DirectoryRow fullRow = row(d, "ORGANIZATION", full.getId());
        assertThat(fullRow.profileCompletion()).isEqualTo(100);
        assertThat(fullRow.missingFields()).isEmpty();
        assertThat(fullRow.seatsUsed()).isEqualTo(1);
        assertThat(fullRow.contactEmail()).isEqualTo(adminC.getEmail());

        DirectoryRow emptyRow = row(d, "ORGANIZATION", empty.getId());
        assertThat(emptyRow.profileCompletion()).isEqualTo(8); // only the country (mandatory at registration) is filled
        assertThat(emptyRow.missingFields()).contains("INDUSTRY", "ADMIN_CONTACT", "TAX_REGISTRATION", "REGION", "BILLING_ADDRESS");

        assertThat(row(d, "INDIVIDUAL", indFull.getId()).profileCompletion()).isEqualTo(100);
        assertThat(row(d, "INDIVIDUAL", indEmpty.getId()).missingFields()).containsExactly("MOBILE", "COUNTRY", "COMPANY_NAME", "JOB_TITLE", "INDUSTRY");
        // a member is listed under their organization only, never as an individual
        assertThat(d.rows().stream().noneMatch(r -> r.kind().equals("INDIVIDUAL") && r.id().equals(adminC.getId()))).isTrue();

        Summary s = d.summary();
        assertThat(s.total()).isEqualTo(d.rows().size());
        assertThat(s.organizations() + s.individuals()).isEqualTo(s.total());
        assertThat(s.profileComplete() + s.profileInProgress() + s.profileNotStarted()).isEqualTo(s.total());
    }

    @Test
    void billingAddressNeedsItsOwnFieldsWhenNotTheSameAsTheAddress() {
        Organization o = org("Bill Co", true);
        o.setBillingSameAsAddress(false);
        organizationRepository.save(o);
        DirectoryRow r = row(service.directory(), "ORGANIZATION", o.getId());
        assertThat(r.missingFields()).containsExactly("BILLING_ADDRESS", "ADMIN_CONTACT");
    }

    @Test
    void overviewAndTabsAreReadable() {
        Organization o = org("Tabs Co", true);
        Customer admin = customer("tabs" + System.nanoTime() + "@dir.example", false);
        memberService.addMember(o.getId(), admin.getId(), OrgRole.ORG_ADMIN);

        assertThat(service.overview(o.getId()).completion().percent()).isEqualTo(100);
        assertThat(service.members(o.getId())).hasSize(1);
        assertThat(service.subscriptions(o.getId())).isEmpty();
        assertThat(service.invoices(o.getId())).isEmpty();
        assertThat(service.tickets(o.getId())).isEmpty();
        assertThatThrownBy(() -> service.members(-1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void theHierarchyIsReadOnlyForThePlatformAdministratorAndNeverCreatesTheRoot() {
        Organization o = org("Tree Co", true);
        assertThat(service.hierarchy(o.getId()).nodes()).isEmpty();
        assertThat(service.hierarchy(o.getId()).levels()).hasSize(7);
        assertThat(hierarchyService.nodeCount(o.getId())).isZero();

        Customer admin = customer("tree" + System.nanoTime() + "@dir.example", false);
        var m = memberService.addMember(o.getId(), admin.getId(), OrgRole.ORG_ADMIN);
        var tree = hierarchyService.tree(m.getCustomerId());
        assertThat(service.hierarchy(o.getId()).nodes()).hasSize(1);
        Long rootId = tree.nodes().get(0).id();
        assertThat(service.hierarchyNode(o.getId(), rootId).path()).containsExactly("Tree Co");
        assertThat(service.hierarchyHistory(o.getId(), rootId)).isEmpty();
        assertThat(row(service.directory(), "ORGANIZATION", o.getId()).hierarchyNodes()).isEqualTo(1);

        Organization other = org("Other Co", true);
        assertThatThrownBy(() -> service.hierarchyNode(other.getId(), rootId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void anIndividualHasAProfileButAMemberDoesNot() {
        Customer ind = customer("solo" + System.nanoTime() + "@dir.example", true);
        IndividualDetail detail = service.individual(ind.getId());
        assertThat(detail.completion().percent()).isEqualTo(100);
        assertThat(detail.subscriptions()).isEmpty();

        Organization o = org("Member Co", true);
        Customer member = customer("mem" + System.nanoTime() + "@dir.example", false);
        memberService.addMember(o.getId(), member.getId(), OrgRole.MEMBER);
        assertThatThrownBy(() -> service.individual(member.getId())).isInstanceOf(ResourceNotFoundException.class);
    }
}
