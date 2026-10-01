package com.vyoog.eisplatform.modules.cart.controller;

import com.vyoog.eisplatform.modules.cart.dto.AddCartItemRequest;
import com.vyoog.eisplatform.modules.cart.dto.CartCheckoutResultDto;
import com.vyoog.eisplatform.modules.cart.dto.CartDto;
import com.vyoog.eisplatform.modules.cart.dto.CartValidationDto;
import com.vyoog.eisplatform.modules.cart.dto.UpdateCartItemRequest;
import com.vyoog.eisplatform.modules.cart.service.CartService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** C59, REQ-MKT-003 ({@code docs/06-api/api-requirements/cart-checkout.md}):
 * the signed-in user's own cart. Covered by the authenticated {@code /me/**}
 * rule; there is no other user's cart to address (BR-1). */
@RestController
@RequestMapping("/me/cart")
@RequiredArgsConstructor
public class CartController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final CartService cartService;

    @GetMapping
    public CartDto get(@AuthenticationPrincipal Jwt jwt) {
        return cartService.getCart(customerId(jwt));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> add(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(customerId(jwt), request));
    }

    @PatchMapping("/items/{itemId}")
    public CartDto update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long itemId, @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(customerId(jwt), itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    public CartDto remove(@AuthenticationPrincipal Jwt jwt, @PathVariable Long itemId) {
        return cartService.removeItem(customerId(jwt), itemId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(@AuthenticationPrincipal Jwt jwt) {
        cartService.clear(customerId(jwt));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    public CartValidationDto validate(@AuthenticationPrincipal Jwt jwt) {
        return cartService.validate(customerId(jwt));
    }

    @PostMapping("/checkout")
    public ResponseEntity<CartCheckoutResultDto> checkout(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.checkout(customerId(jwt)));
    }

    private Long customerId(Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return customer.getId();
    }
}
