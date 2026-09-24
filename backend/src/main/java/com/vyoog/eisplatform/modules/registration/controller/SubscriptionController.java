package com.vyoog.eisplatform.modules.registration.controller;

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
}
