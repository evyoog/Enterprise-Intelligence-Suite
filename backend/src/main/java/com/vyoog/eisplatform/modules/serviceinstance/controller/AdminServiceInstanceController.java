package com.vyoog.eisplatform.modules.serviceinstance.controller;

import com.vyoog.eisplatform.modules.serviceinstance.dto.ServiceInstanceDtos;
import com.vyoog.eisplatform.modules.serviceinstance.service.ServiceInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** C101 / REQ-SRM-001: platform admins see every service instance, set one by hand (SR-5 B) and manage the products' reporter keys.
 * Gated by {@code MANAGE_SERVICE_STATUS} in SecurityConfig (the {@code /admin/service-status/**} rule). */
@RestController
@RequestMapping("/admin/service-status")
@RequiredArgsConstructor
public class AdminServiceInstanceController {

    private final ServiceInstanceService service;

    @GetMapping("/instances")
    public List<ServiceInstanceDtos.Instance> list(@RequestParam(required = false) Long productId, @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String health) {
        return service.listAll(productId, status, health);
    }

    @GetMapping("/instances/summary")
    public ServiceInstanceDtos.Summary summary(@RequestParam(required = false) Long productId) {
        return service.summary(productId);
    }

    @GetMapping("/instances/{id}")
    public ServiceInstanceDtos.Instance get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/instances/{id}")
    public ServiceInstanceDtos.Instance change(@PathVariable Long id, @RequestBody ServiceInstanceDtos.ManualChange change, @AuthenticationPrincipal Jwt jwt) {
        return service.changeByHand(jwt.getSubject(), id, change);
    }

    @GetMapping("/reporter-keys")
    public List<ServiceInstanceDtos.ReporterKey> keys() {
        return service.listKeys();
    }

    @PostMapping("/reporter-keys")
    public ServiceInstanceDtos.CreatedReporterKey createKey(@RequestBody ServiceInstanceDtos.CreateReporterKey request, @AuthenticationPrincipal Jwt jwt) {
        return service.createKey(jwt.getSubject(), request);
    }

    @DeleteMapping("/reporter-keys/{id}")
    public ServiceInstanceDtos.ReporterKey revokeKey(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.revokeKey(jwt.getSubject(), id);
    }
}
