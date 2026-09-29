package com.vyoog.eisplatform.modules.registration.controller;

import com.vyoog.eisplatform.modules.registration.dto.ChangePlanRequest;
import com.vyoog.eisplatform.modules.registration.dto.MyProductDto;
import com.vyoog.eisplatform.modules.registration.dto.SubscribeRequest;
import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * An individual customer's own products/subscriptions — every method here
 * resolves "who is calling" from the JWT via {@link CurrentCustomerResolver},
 * never from anything the client sends; requires only a valid, authenticated
 * token (any Vyoog customer, no special role).
 */
@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class SubscriptionController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final SubscriptionService subscriptionService;

    @GetMapping("/products")
    public List<MyProductDto> myProducts(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return subscriptionService.listMyProducts(customer.getId());
    }

    @GetMapping("/subscriptions")
    public List<SubscriptionDto> mySubscriptions(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return subscriptionService.listMySubscriptions(customer.getId());
    }

    @PostMapping("/subscriptions")
    public SubscriptionDto subscribe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SubscribeRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return subscriptionService.subscribe(customer.getId(), request.productId());
    }

    @PostMapping("/subscriptions/{id}/suspend")
    public SubscriptionDto suspend(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long subscriptionId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return subscriptionService.suspendSubscription(customer.getId(), subscriptionId);
    }

    @PostMapping("/subscriptions/{id}/reactivate")
    public SubscriptionDto reactivate(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long subscriptionId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return subscriptionService.reactivateSubscription(customer.getId(), subscriptionId);
    }

    @PostMapping("/subscriptions/{id}/cancel")
    public SubscriptionDto cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long subscriptionId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return subscriptionService.cancelSubscription(customer.getId(), subscriptionId);
    }

    @PostMapping("/subscriptions/{id}/renew")
    public SubscriptionDto renew(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long subscriptionId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return subscriptionService.renewSubscription(customer.getId(), subscriptionId);
    }

    @PatchMapping("/subscriptions/{id}/plan")
    public SubscriptionDto changePlan(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long subscriptionId,
                                       @Valid @RequestBody ChangePlanRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return subscriptionService.changePlan(customer.getId(), subscriptionId, request);
    }
}
