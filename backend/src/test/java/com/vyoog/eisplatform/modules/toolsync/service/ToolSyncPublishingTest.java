package com.vyoog.eisplatform.modules.toolsync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.integration.model.OutboxEvent;
import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNode;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * REQ-INT-003 phase 7, step 2: every change of the six synchronized aggregates raises its version (once per change, never for an
 * unchanged save) and becomes a platform event in the same transaction, wherever it is made; and while no tool is connected nothing is
 * published at all. (TC-INT-051, TC-INT-052.)
 */
class ToolSyncPublishingTest extends ToolSyncTestBase {

    @Autowired private TransactionTemplate tx;
    @Autowired private ObjectMapper json;

    @SuppressWarnings("unchecked")
    private Map<String, Object> payload(OutboxEvent e) throws Exception {
        return json.readValue(e.getPayload(), Map.class);
    }

    // ── versions ─────────────────────────────────────────────────────────────

    @Test
    void aNewAggregateStartsAtVersionOneAndEachChangeAddsOne() {
        Organization org = organization();
        assertThat(org.getSyncVersion()).isEqualTo(1);

        org.setName("Renamed");
        org = organizations.save(org);
        assertThat(organizations.findById(org.getId()).orElseThrow().getSyncVersion()).isEqualTo(2);

        org.setCity("Chennai");
        organizations.save(org);
        assertThat(organizations.findById(org.getId()).orElseThrow().getSyncVersion()).isEqualTo(3);
    }

    @Test
    void aStaleCopySavedAgainNeverMakesTheVersionGoBack() {
        Organization stale = organization();                         // a copy that still says version 1
        Organization current = organizations.findById(stale.getId()).orElseThrow();
        current.setCity("Chennai");
        organizations.save(current);
        current.setCity("Madurai");
        organizations.save(current);
        assertThat(organizations.findById(stale.getId()).orElseThrow().getSyncVersion()).isEqualTo(3);

        stale.setName("Renamed from a stale copy");
        organizations.save(stale);

        assertThat(organizations.findById(stale.getId()).orElseThrow().getSyncVersion()).as("3 + 1, not 1 + 1").isEqualTo(4);
    }

    @Test
    void savingWithoutChangingAnythingDoesNotAddAVersion() {
        Organization org = organization();
        Organization loaded = organizations.findById(org.getId()).orElseThrow();

        organizations.save(loaded);
        organizations.save(loaded);

        assertThat(organizations.findById(org.getId()).orElseThrow().getSyncVersion()).isEqualTo(1);
    }

    @Test
    void everyOneOfTheSixAggregatesCarriesAVersion() {
        Organization org = organization();
        Customer person = customer(newSub());
        OrganizationMember m = member(org, person, OrgRole.MEMBER);
        OrgNode node = node(org, null, "Root", "ORGANIZATION");
        ProductSubscription sub = subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2099, 12, 31));
        OrganizationProductAccess access = access(m, "PMS_USER", MembershipStatus.ACTIVE);

        List<Long> versions = List.of(org.getSyncVersion(), person.getSyncVersion(), m.getSyncVersion(), node.getSyncVersion(),
            sub.getSyncVersion(), access.getSyncVersion());
        assertThat(versions).containsOnly(1L);

        person.setMobile("98400");
        customers.save(person);
        access.setStatus(MembershipStatus.INACTIVE);
        accesses.save(access);
        assertThat(customers.findById(person.getId()).orElseThrow().getSyncVersion()).isEqualTo(2);
        assertThat(accesses.findById(access.getId()).orElseThrow().getSyncVersion()).isEqualTo(2);
    }

    // ── nothing without a tool ───────────────────────────────────────────────

    @Test
    void whileNoToolIsConnectedNothingIsPublished() {
        Organization org = organization();
        Customer person = customer(newSub());
        member(org, person, OrgRole.ORG_ADMIN);
        node(org, null, "Root", "ORGANIZATION");
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2099, 12, 31));

        assertThat(events("Organization", org.getId())).isEmpty();
        assertThat(events("User", person.getKeycloakSub())).isEmpty();
        assertThat(outbox.findByEventTypeOrderByIdAsc("MembershipChanged").stream().filter(e -> e.getAggregateId().equals(person.getKeycloakSub()))).isEmpty();
    }

    @Test
    void aPausedToolStillGetsEventsSoNothingIsLostWhileItIsPaused() {
        var c = connector();
        c.setStatus(com.vyoog.eisplatform.modules.toolsync.model.ConnectorStatus.PAUSED);
        connectors.save(c);

        Organization org = organization();

        assertThat(events("Organization", org.getId())).hasSize(1);
    }

    // ── events ───────────────────────────────────────────────────────────────

    @Test
    void anOrganizationChangeIsAnEventWithItsVersion() throws Exception {
        connector();
        Organization org = organization();
        org.setName("Acme Manufacturing");
        organizations.save(org);

        List<OutboxEvent> events = events("Organization", org.getId());

        assertThat(events).extracting(OutboxEvent::getEventType).containsExactly("OrganizationUpserted", "OrganizationUpserted");
        assertThat(payload(events.get(0))).containsEntry("version", 1).containsEntry("organizationId", org.getId().intValue());
        assertThat(payload(events.get(1))).containsEntry("version", 2);
    }

    @Test
    void aPersonWithAKeycloakIdBecomesAnEventForEachOrganizationTheyBelongTo() throws Exception {
        connector();
        Organization a = organization();
        Organization b = organization();
        Customer person = customer(newSub());
        member(a, person, OrgRole.MEMBER);
        member(b, person, OrgRole.MEMBER);
        person.setFirstName("Changed");
        customers.save(person);

        List<OutboxEvent> events = events("User", person.getKeycloakSub());

        assertThat(events).isNotEmpty().allSatisfy(e -> assertThat(e.getEventType()).isEqualTo("UserUpserted"));
        OutboxEvent last = events.get(events.size() - 1);
        assertThat(payload(last)).containsEntry("version", 2);
        assertThat(ToolSyncPublisherAccess.organizations(payload(last))).containsExactlyInAnyOrder(a.getId(), b.getId());
    }

    @Test
    void aPersonWithoutAKeycloakIdOrWithoutAMembershipIsNotPublished() {
        connector();
        Organization org = organization();
        Customer unlinked = customer(null);
        member(org, unlinked, OrgRole.MEMBER);
        Customer loner = customer(newSub());
        loner.setLastName("Alone");
        customers.save(loner);

        assertThat(outbox.findByEventTypeOrderByIdAsc("UserUpserted").stream().filter(e -> e.getAggregateId().equals(loner.getKeycloakSub()))).isEmpty();
        assertThat(outbox.findByEventTypeOrderByIdAsc("MembershipChanged").stream().filter(e -> e.getPayload().contains("\"organizationId\":" + org.getId()))).isEmpty();
    }

    @Test
    void aMembershipAnnouncesThePersonFirstAndThenTheMembership() throws Exception {
        connector();
        Organization org = organization();
        Customer person = customer(newSub());

        member(org, person, OrgRole.ORG_ADMIN);

        List<OutboxEvent> user = events("User", person.getKeycloakSub());
        List<OutboxEvent> membership = events("Membership", person.getKeycloakSub());
        assertThat(user).hasSize(1);
        assertThat(membership).singleElement().satisfies(e -> {
            assertThat(e.getEventType()).isEqualTo("MembershipChanged");
            assertThat(payload(e)).containsEntry("organizationId", org.getId().intValue()).containsEntry("version", 1);
        });
        assertThat(user.get(0).getId()).as("the person comes before the membership").isLessThan(membership.get(0).getId());
    }

    @Test
    void hierarchyNodesAreUpsertedAndAnEmptyNodeIsDeletedWithATombstoneVersion() throws Exception {
        connector();
        Organization org = organization();
        OrgNode root = node(org, null, "Root", "ORGANIZATION");
        OrgNode team = node(org, root, "Platform", "TEAM");
        team.setName("Platform Engineering");
        nodes.save(team);
        long versionBefore = nodes.findById(team.getId()).orElseThrow().getSyncVersion();

        nodes.deleteById(team.getId());

        List<OutboxEvent> events = events("OrgNode", team.getId());
        assertThat(events).extracting(OutboxEvent::getEventType).containsExactly("OrgNodeUpserted", "OrgNodeUpserted", "OrgNodeDeleted");
        assertThat(payload(events.get(2))).containsEntry("organizationId", org.getId().intValue()).containsEntry("version", (int) versionBefore + 1);
    }

    @Test
    void anOrganizationSubscriptionIsPublishedAndAnIndividualsIsNot() throws Exception {
        connector();
        Organization org = organization();
        ProductSubscription orgSub = subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2099, 12, 31));
        Customer person = customer(newSub());
        ProductSubscription individual = new ProductSubscription();
        individual.setProductId(PRODUCT_ID);
        individual.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
        individual.setOwnerCustomerId(person.getId());
        individual.setStatus(SubscriptionStatus.ACTIVE);
        individual = subscriptions.save(individual);

        assertThat(events("Subscription", orgSub.getId())).singleElement().satisfies(e -> {
            assertThat(e.getEventType()).isEqualTo("SubscriptionSynced");
            assertThat(e.getPayload()).contains("\"organizationId\":" + org.getId()).contains("\"productId\":" + PRODUCT_ID);
        });
        assertThat(events("Subscription", individual.getId()).stream().filter(e -> e.getEventType().equals("SubscriptionSynced"))).isEmpty();
    }

    @Test
    void theExpiryJobsStatusChangeIsAnEventToo() {
        connector();
        Organization org = organization();
        ProductSubscription sub = subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2020, 1, 31));
        sub.setAutoRenew(false);
        subscriptions.save(sub);

        // what SubscriptionService.expireOverdueSubscriptions does: it changes the status and publishes no event of its own
        ProductSubscription loaded = subscriptions.findById(sub.getId()).orElseThrow();
        loaded.setStatus(SubscriptionStatus.EXPIRED);
        subscriptions.save(loaded);

        List<OutboxEvent> synced = events("Subscription", sub.getId()).stream().filter(e -> e.getEventType().equals("SubscriptionSynced")).toList();
        assertThat(synced).hasSize(3);       // created, auto-renew off, expired
    }

    @Test
    void productAccessIsGrantedOrRevokedForThePersonAndTheToolsProductCode() throws Exception {
        connector();
        Organization org = organization();
        Customer person = customer(newSub());
        OrganizationMember m = member(org, person, OrgRole.MEMBER);
        OrganizationProductAccess a = access(m, "PMS_MANAGER", MembershipStatus.ACTIVE);
        a.setStatus(MembershipStatus.INACTIVE);
        accesses.save(a);

        List<OutboxEvent> events = events("UserAccess", person.getKeycloakSub() + ":" + PRODUCT_CODE);

        assertThat(events).extracting(OutboxEvent::getEventType).containsExactly("UserProductAccessGranted", "UserProductAccessRevoked");
        assertThat(payload(events.get(1))).containsEntry("organizationId", org.getId().intValue()).containsEntry("version", 2);
    }

    @Test
    void accessToAProductThatHasNoToolIsNotPublished() {
        connector();
        Organization org = organization();
        Customer person = customer(newSub());
        OrganizationMember m = member(org, person, OrgRole.MEMBER);
        OrganizationProductAccess other = new OrganizationProductAccess();
        other.setOrganizationMemberId(m.getId());
        other.setProductId(999_999L);
        other.setProductRole("X_ADMIN");
        accesses.save(other);

        assertThat(outbox.findByEventTypeOrderByIdAsc("UserProductAccessGranted").stream().filter(e -> e.getAggregateId().startsWith(person.getKeycloakSub()))).isEmpty();
    }

    // ── transactions ─────────────────────────────────────────────────────────

    @Test
    void manyChangesToOneAggregateInOneTransactionAreOneEventWithTheLastVersion() throws Exception {
        connector();
        Organization org = organization();

        tx.executeWithoutResult(s -> {
            Organization o = organizations.findById(org.getId()).orElseThrow();
            o.setName("One");
            organizations.saveAndFlush(o);
            o.setName("Two");
            organizations.saveAndFlush(o);
            o.setName("Three");
            organizations.save(o);
        });

        List<OutboxEvent> events = events("Organization", org.getId());
        assertThat(events).hasSize(2);                           // creation, then the whole transaction
        assertThat(payload(events.get(1))).containsEntry("version", 4);
    }

    @Test
    void aRolledBackTransactionPublishesNothingAndKeepsTheOldVersion() {
        connector();
        Organization org = organization();

        try {
            tx.executeWithoutResult(s -> {
                Organization o = organizations.findById(org.getId()).orElseThrow();
                o.setName("Never saved");
                organizations.saveAndFlush(o);
                throw new IllegalStateException("boom");
            });
        } catch (IllegalStateException expected) {
            // the transaction is rolled back
        }

        assertThat(events("Organization", org.getId())).hasSize(1);
        assertThat(organizations.findById(org.getId()).orElseThrow().getSyncVersion()).isEqualTo(1);
    }

    @Test
    void theEventAndTheChangeCommitTogether() {
        connector();
        long[] orgId = new long[1];

        tx.executeWithoutResult(s -> orgId[0] = organization().getId());

        assertThat(events("Organization", orgId[0])).hasSize(1);
    }

    /** The helper the publisher offers for reading an event's organizations. */
    private static final class ToolSyncPublisherAccess {
        static List<Long> organizations(Map<String, Object> p) {
            return com.vyoog.eisplatform.modules.toolsync.service.ToolSyncPublisher.organizationsOf(p);
        }
    }
}
