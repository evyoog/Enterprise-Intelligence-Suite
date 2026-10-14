package com.vyoog.eisplatform.modules.serviceinstance.controller;

import com.vyoog.eisplatform.modules.serviceinstance.dto.ServiceInstanceDtos;
import com.vyoog.eisplatform.modules.serviceinstance.service.ServiceInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** C101 / SR-2 A: a product reports the health of its instances (about once a minute). No user token exists on this call: it is authenticated by the
 * product's reporter key in {@code X-Reporter-Key}, checked in {@link ServiceInstanceService#report}; SecurityConfig leaves the path open like the webhooks. */
@RestController
@RequiredArgsConstructor
public class ProductReportController {

    private final ServiceInstanceService service;

    @PostMapping("/product-reports/health")
    public ServiceInstanceDtos.ReportResult health(@RequestHeader(value = "X-Reporter-Key", required = false) String reporterKey,
                                                   @RequestBody ServiceInstanceDtos.HealthReports body) {
        return service.report(reporterKey, body == null ? null : body.reports());
    }
}
