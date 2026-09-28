package com.vyoog.eisplatform.modules.registration.controller;

import com.vyoog.eisplatform.modules.registration.dto.DecideOrderRequest;
import com.vyoog.eisplatform.modules.registration.dto.OrderDto;
import com.vyoog.eisplatform.modules.registration.dto.SubmitOrderRequest;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.registration.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 09 Order & Provisioning Management (sprint 2027.1.1) — an organization
 * member's own orders, and (for an ORG_ADMIN holding MANAGE_ORDERS) their
 * organization's pending orders to decide on. Covered by the existing
 * "/organization/me/**" authenticated() rule in SecurityConfig; the
 * ORG_ADMIN-only actions are gated inside {@link OrderService} itself, same
 * as every other organization self-service action.
 */
@RestController
@RequestMapping("/organization/me/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final OrderService orderService;

    @PostMapping
    public OrderDto submit(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SubmitOrderRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return orderService.submitOrder(customer.getId(), request);
    }

    @GetMapping
    public List<OrderDto> myOrders(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return orderService.listMyOrders(customer.getId());
    }

    @GetMapping("/pending")
    public List<OrderDto> pendingOrders(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return orderService.listPendingOrders(customer.getId());
    }

    @PostMapping("/{id}/approve")
    public OrderDto approve(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long orderId, @RequestBody(required = false) DecideOrderRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return orderService.approveOrder(customer.getId(), orderId, request != null ? request : new DecideOrderRequest(null));
    }

    @PostMapping("/{id}/reject")
    public OrderDto reject(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long orderId, @RequestBody(required = false) DecideOrderRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return orderService.rejectOrder(customer.getId(), orderId, request != null ? request : new DecideOrderRequest(null));
    }

    @PostMapping("/{id}/cancel")
    public OrderDto cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long orderId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return orderService.cancelOrder(customer.getId(), orderId);
    }
}
