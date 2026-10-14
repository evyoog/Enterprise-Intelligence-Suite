package com.vyoog.eisplatform.modules.billing.controller;

import com.vyoog.eisplatform.modules.billing.dto.GatewayDtos;
import com.vyoog.eisplatform.modules.billing.service.GatewayManagementService;
import com.vyoog.eisplatform.modules.billing.service.gateway.GatewayActor;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** C98 / REQ-BIL-003 — Platform Admin → Billing → Payment Gateways. {@code MANAGE_BILLING} is enforced in SecurityConfig for every path under
 * /admin/billing. No response carries a secret. */
@RestController
@RequestMapping("/admin/billing/gateways")
@RequiredArgsConstructor
public class AdminPaymentGatewayController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final GatewayManagementService gateways;

    @GetMapping
    public List<GatewayDtos.Gateway> list() {
        return gateways.list();
    }

    @GetMapping("/summary")
    public GatewayDtos.Summary summary() {
        return gateways.summary();
    }

    /** Starting points for the Add gateway form. */
    @GetMapping("/templates")
    public List<GatewayDtos.Template> templates() {
        return gateways.templates();
    }

    /** Adds a gateway: definition plus (optionally) credential values, stored encrypted. */
    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public GatewayDtos.Detail create(@AuthenticationPrincipal Jwt jwt, @RequestBody GatewayDtos.CreateGateway request) {
        return gateways.create(actor(jwt), request);
    }

    @PutMapping("/{code}/definition")
    public GatewayDtos.Detail updateDefinition(@AuthenticationPrincipal Jwt jwt, @PathVariable String code, @RequestBody GatewayDtos.Definition request) {
        return gateways.updateDefinition(actor(jwt), code, request);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable String code) {
        gateways.delete(actor(jwt), code);
    }

    @GetMapping("/{code}")
    public GatewayDtos.Detail detail(@PathVariable String code) {
        return gateways.detail(code);
    }

    @PutMapping("/{code}/configuration")
    public GatewayDtos.Detail configure(@AuthenticationPrincipal Jwt jwt, @PathVariable String code, @RequestBody GatewayDtos.SaveConfiguration request) {
        return gateways.saveConfiguration(actor(jwt), code, request);
    }

    @PostMapping("/{code}/test")
    public GatewayDtos.Connection test(@AuthenticationPrincipal Jwt jwt, @PathVariable String code) {
        return gateways.test(actor(jwt), code);
    }

    @PostMapping("/{code}/activate")
    public GatewayDtos.Detail activate(@AuthenticationPrincipal Jwt jwt, @PathVariable String code) {
        return gateways.activate(actor(jwt), code);
    }

    @PostMapping("/{code}/deactivate")
    public GatewayDtos.Detail deactivate(@AuthenticationPrincipal Jwt jwt, @PathVariable String code) {
        return gateways.deactivate(actor(jwt), code);
    }

    @PutMapping("/{code}/methods")
    public GatewayDtos.Detail methods(@AuthenticationPrincipal Jwt jwt, @PathVariable String code, @RequestBody GatewayDtos.SetMethods request) {
        return gateways.setMethods(actor(jwt), code, request.methods());
    }

    /** The default gateway for new payments. {@code PUT /gateways/active} (C97) is kept as an alias. */
    @PutMapping({"/default", "/active"})
    public List<GatewayDtos.Gateway> setDefault(@AuthenticationPrincipal Jwt jwt, @RequestBody GatewayDtos.SetDefault request) {
        gateways.setDefault(actor(jwt), request.code());
        return gateways.list();
    }

    /** A platform administrator's Keycloak account may never have gone through registration, so no customer row is required. */
    private GatewayActor actor(Jwt jwt) {
        return new GatewayActor(jwt.getSubject(), currentCustomerResolver.resolveOptional(jwt).map(com.vyoog.eisplatform.modules.registration.model.Customer::getId).orElse(null));
    }
}
