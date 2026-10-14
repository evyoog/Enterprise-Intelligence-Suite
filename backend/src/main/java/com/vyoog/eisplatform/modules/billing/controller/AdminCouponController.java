package com.vyoog.eisplatform.modules.billing.controller;

import com.vyoog.eisplatform.modules.billing.dto.CouponDtos;
import com.vyoog.eisplatform.modules.billing.service.CouponService;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** C100 (PB-1 note) — Platform Admin → Billing → Coupons. {@code MANAGE_BILLING} is enforced in SecurityConfig for every path under /admin/billing. */
@RestController
@RequestMapping("/admin/billing/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping
    public List<CouponDtos.Coupon> list() {
        return couponService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponDtos.Coupon create(@AuthenticationPrincipal Jwt jwt, @RequestBody CouponDtos.Save request) {
        return couponService.create(jwt.getSubject(), customerId(jwt), request);
    }

    @PutMapping("/{id}")
    public CouponDtos.Coupon update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @RequestBody CouponDtos.Save request) {
        return couponService.update(jwt.getSubject(), customerId(jwt), id, request);
    }

    /** Disables a coupon before its expiry, or enables it again. */
    @PatchMapping("/{id}/enabled")
    public CouponDtos.Coupon setEnabled(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @RequestBody CouponDtos.SetEnabled request) {
        return couponService.setEnabled(jwt.getSubject(), customerId(jwt), id, request.enabled());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        couponService.delete(jwt.getSubject(), customerId(jwt), id);
    }

    /** A platform administrator's Keycloak account may never have gone through registration, so no customer row is required. */
    private Long customerId(Jwt jwt) {
        return currentCustomerResolver.resolveOptional(jwt).map(com.vyoog.eisplatform.modules.registration.model.Customer::getId).orElse(null);
    }
}
