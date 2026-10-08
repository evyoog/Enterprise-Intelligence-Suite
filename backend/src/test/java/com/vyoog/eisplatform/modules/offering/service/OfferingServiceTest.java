package com.vyoog.eisplatform.modules.offering.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.offering.dto.OfferingDtos.*;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-CAT-005 offering management (02.02): offerings, audience rule, "works with" list. */
@SpringBootTest
@ActiveProfiles("test")
class OfferingServiceTest {

    @Autowired
    private OfferingService offerings;
    @Autowired
    private EligibilityService eligibility;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private com.vyoog.eisplatform.modules.registration.service.SubscriptionService subscriptions;

    private Long active;
    private Long other;
    private Long draft;

    @BeforeEach
    void products() {
        active = newProduct(ProductStatus.ACTIVE);
        other = newProduct(ProductStatus.ACTIVE);
        draft = newProduct(ProductStatus.INACTIVE);
    }

    private Long newProduct(ProductStatus status) {
        Product product = new Product();
        product.setName("Offering test " + System.nanoTime());
        product.setPrice(BigDecimal.TEN);
        product.setStatus(status);
        return productRepository.save(product).getId();
    }

    private String name() {
        return "Bundle " + System.nanoTime();
    }

    @Test
    void createsAnOfferingInDraftAndKeepsProductOrder() {
        OfferingDto created = offerings.create("sub", "a@test", new OfferingRequest(name(), "desc", null, List.of(other, active)));
        assertThat(created.status()).isEqualTo("DRAFT");
        assertThat(created.products()).extracting(ProductRef::id).containsExactly(other, active);
    }

    @Test
    void anOfferingNeedsAtLeastOneExistingProduct() {
        assertThatThrownBy(() -> offerings.create("sub", "a@test", new OfferingRequest(name(), null, null, List.of())))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> offerings.create("sub", "a@test", new OfferingRequest(name(), null, null, List.of(-5L))))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void namesAreUniqueIgnoringCase() {
        String n = name();
        offerings.create("sub", "a@test", new OfferingRequest(n, null, null, List.of(active)));
        assertThatThrownBy(() -> offerings.create("sub", "a@test", new OfferingRequest(n.toUpperCase(), null, null, List.of(active))))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void canBePublishedOnlyWithAnActiveProduct() {
        OfferingDto o = offerings.create("sub", "a@test", new OfferingRequest(name(), null, null, List.of(draft)));
        assertThatThrownBy(() -> offerings.update("sub", "a@test", o.id(), new OfferingRequest(o.name(), null, "ACTIVE", List.of(draft))))
            .isInstanceOf(InvalidStateException.class);
        OfferingDto published = offerings.update("sub", "a@test", o.id(), new OfferingRequest(o.name(), null, "ACTIVE", List.of(draft, active)));
        assertThat(published.status()).isEqualTo("ACTIVE");
    }

    @Test
    void publicViewShowsOnlyActiveOfferingsAndActiveProducts() {
        OfferingDto live = offerings.create("sub", "a@test", new OfferingRequest(name(), null, "DRAFT", List.of(active, draft)));
        OfferingDto hidden = offerings.create("sub", "a@test", new OfferingRequest(name(), null, null, List.of(active)));
        offerings.update("sub", "a@test", live.id(), new OfferingRequest(live.name(), null, "ACTIVE", List.of(active, draft)));

        assertThat(offerings.publicList()).extracting(OfferingDto::id).contains(live.id()).doesNotContain(hidden.id());
        assertThat(offerings.publicGet(live.id()).products()).extracting(ProductRef::id).containsExactly(active);
        assertThatThrownBy(() -> offerings.publicGet(hidden.id())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void onlyADraftOfferingCanBeDeleted() {
        OfferingDto o = offerings.create("sub", "a@test", new OfferingRequest(name(), null, null, List.of(active)));
        offerings.update("sub", "a@test", o.id(), new OfferingRequest(o.name(), null, "ACTIVE", List.of(active)));
        assertThatThrownBy(() -> offerings.delete("sub", "a@test", o.id())).isInstanceOf(InvalidStateException.class);
        offerings.update("sub", "a@test", o.id(), new OfferingRequest(o.name(), null, "DRAFT", List.of(active)));
        offerings.delete("sub", "a@test", o.id());
        assertThatThrownBy(() -> offerings.adminGet(o.id())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void aProductInAnOfferingCannotBeDeleted() {
        offerings.create("sub", "a@test", new OfferingRequest(name(), null, null, List.of(active)));
        assertThat(offerings.blockingReason(active)).isPresent();
        assertThat(offerings.blockingReason(other)).isEmpty();
    }

    @Test
    void aProductIsOpenToEveryoneUntilARuleIsSet() {
        assertThat(eligibility.isEligible(active, true)).isTrue();
        assertThat(eligibility.isEligible(active, false)).isTrue();
    }

    @Test
    void audienceRuleBlocksTheOtherBuyerType() {
        eligibility.setRules("sub", "a@test", active, new ProductRuleRequest("ORGANIZATION", List.of()));
        assertThat(eligibility.isEligible(active, true)).isTrue();
        assertThat(eligibility.isEligible(active, false)).isFalse();
        assertThatThrownBy(() -> eligibility.assertEligible(active, false))
            .isInstanceOf(InvalidStateException.class).hasMessageContaining("organizations only");

        eligibility.setRules("sub", "a@test", active, new ProductRuleRequest("INDIVIDUAL", List.of()));
        assertThat(eligibility.isEligible(active, true)).isFalse();

        eligibility.setRules("sub", "a@test", active, new ProductRuleRequest("BOTH", List.of()));
        assertThat(eligibility.isEligible(active, true)).isTrue();
        assertThat(eligibility.isEligible(active, false)).isTrue();
    }

    @Test
    void anIndividualCannotSubscribeDirectlyToAnOrganizationOnlyProduct() {
        eligibility.setRules("sub", "a@test", active, new ProductRuleRequest("ORGANIZATION", List.of()));
        assertThatThrownBy(() -> subscriptions.subscribe(1L, active))
            .isInstanceOf(InvalidStateException.class).hasMessageContaining("organizations only");
        assertThatThrownBy(() -> subscriptions.subscribeFromCart(1L, active, 1L))
            .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void worksWithListIsReplacedAndPublicShowsActiveProductsOnly() {
        eligibility.setRules("sub", "a@test", active, new ProductRuleRequest("BOTH", List.of(other, draft)));
        assertThat(eligibility.worksWith(active)).extracting(WorksWithDto::id).containsExactly(other);
        eligibility.setRules("sub", "a@test", active, new ProductRuleRequest("BOTH", List.of()));
        assertThat(eligibility.worksWith(active)).isEmpty();
    }

    @Test
    void rulesRejectBadInput() {
        assertThatThrownBy(() -> eligibility.setRules("sub", "a@test", active, new ProductRuleRequest("BOTH", List.of(active))))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> eligibility.setRules("sub", "a@test", active, new ProductRuleRequest("NOBODY", List.of())))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> eligibility.setRules("sub", "a@test", -1L, new ProductRuleRequest("BOTH", List.of())))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rulesFollowADeletedProduct() {
        eligibility.setRules("sub", "a@test", active, new ProductRuleRequest("INDIVIDUAL", List.of(other)));
        eligibility.onProductDeleted(other);
        assertThat(eligibility.rules().stream().filter(r -> r.productId().equals(active)).findFirst().orElseThrow()
            .worksWithProductIds()).isEmpty();
    }
}
