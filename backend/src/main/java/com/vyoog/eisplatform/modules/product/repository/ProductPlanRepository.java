package com.vyoog.eisplatform.modules.product.repository;

import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 07.01.02 Subscription Changes (sprint 2026.4.3): a direct lookup path to
 * one plan by id, for {@code SubscriptionService#changePlan}/{@code #renew}
 * to validate a plan belongs to the subscription's own product and read its
 * billing period — everywhere else, a plan is still only ever reached
 * through {@link com.vyoog.eisplatform.modules.product.model.Product#getPlans()}.
 */
public interface ProductPlanRepository extends JpaRepository<ProductPlan, Long> {
}
