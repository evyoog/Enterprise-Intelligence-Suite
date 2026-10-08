package com.vyoog.eisplatform.modules.offering.controller;

import com.vyoog.eisplatform.modules.offering.dto.OfferingDtos.*;
import com.vyoog.eisplatform.modules.offering.service.EligibilityService;
import com.vyoog.eisplatform.modules.offering.service.OfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REQ-CAT-005 public side: active offerings, and the "works with" list of a product. No prices here. */
@RestController
@RequiredArgsConstructor
public class PublicOfferingController {

    private final OfferingService offerings;
    private final EligibilityService eligibility;

    @GetMapping("/offerings")
    public List<OfferingDto> list() {
        return offerings.publicList();
    }

    @GetMapping("/offerings/{id}")
    public OfferingDto get(@PathVariable Long id) {
        return offerings.publicGet(id);
    }

    @GetMapping("/products/{id}/works-with")
    public List<WorksWithDto> worksWith(@PathVariable Long id) {
        return eligibility.worksWith(id);
    }
}
