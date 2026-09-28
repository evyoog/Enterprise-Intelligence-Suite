package com.vyoog.eisplatform.modules.partner.controller;

import com.vyoog.eisplatform.modules.partner.dto.CreateOrUpdateContractRequest;
import com.vyoog.eisplatform.modules.partner.dto.PartnerContractDto;
import com.vyoog.eisplatform.modules.partner.dto.ProviderDto;
import com.vyoog.eisplatform.modules.partner.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 14.01.01.02-.04 Verify/Approve/Activate provider, 14.01.02 Contracts —
 * MANAGE_PARTNERS-gated in SecurityConfig. */
@RestController
@RequestMapping("/admin/partners")
@RequiredArgsConstructor
public class AdminProviderController {

    private final PartnerService partnerService;

    @GetMapping
    public List<ProviderDto> listAll() {
        return partnerService.listAll();
    }

    @GetMapping("/{id}")
    public ProviderDto get(@PathVariable("id") Long id) {
        return partnerService.get(id);
    }

    @PostMapping("/{id}/verify")
    public ProviderDto verify(@PathVariable("id") Long id) {
        return partnerService.verify(id);
    }

    @PostMapping("/{id}/approve")
    public ProviderDto approve(@PathVariable("id") Long id) {
        return partnerService.approve(id);
    }

    @PostMapping("/{id}/activate")
    public ProviderDto activate(@PathVariable("id") Long id) {
        return partnerService.activate(id);
    }

    @PostMapping("/{id}/reject")
    public ProviderDto reject(@PathVariable("id") Long id) {
        return partnerService.reject(id);
    }

    @PutMapping("/{id}/contract")
    public PartnerContractDto saveContract(@PathVariable("id") Long id, @Valid @RequestBody CreateOrUpdateContractRequest request) {
        return partnerService.createOrUpdateContract(id, request);
    }

    @GetMapping("/{id}/contract")
    public PartnerContractDto getContract(@PathVariable("id") Long id) {
        return partnerService.getContract(id);
    }
}
