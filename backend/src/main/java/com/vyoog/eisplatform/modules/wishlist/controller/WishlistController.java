package com.vyoog.eisplatform.modules.wishlist.controller;

import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** C94, REQ-MKT-004 ({@code docs/06-api/api-requirements/wishlist.md}): the signed-in user's own saved products,
 * covered by the authenticated {@code /me/**} rule; there is no other user's list to address (BR-1). */
@RestController
@RequestMapping("/me/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final WishlistService wishlistService;

    @GetMapping
    public List<Long> list(@AuthenticationPrincipal Jwt jwt) {
        return wishlistService.list(customerId(jwt));
    }

    @PutMapping("/{productId}")
    public List<Long> add(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId) {
        return wishlistService.add(customerId(jwt), productId);
    }

    @DeleteMapping("/{productId}")
    public List<Long> remove(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId) {
        return wishlistService.remove(customerId(jwt), productId);
    }

    private Long customerId(Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return customer.getId();
    }
}
