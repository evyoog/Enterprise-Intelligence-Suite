package com.vyoog.eisplatform.modules.platform.controller;

import com.vyoog.eisplatform.modules.platform.dto.CatalogPlatformDto;
import com.vyoog.eisplatform.modules.platform.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** C66: the public Product Catalog's platforms (read-only, no sign-in
 * needed — the same audience as the public app listing). */
@RestController
@RequestMapping("/catalog/platforms")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public List<CatalogPlatformDto> list() {
        return catalogService.listPlatforms();
    }

    @GetMapping("/{id}")
    public CatalogPlatformDto get(@PathVariable Long id) {
        return catalogService.getPlatform(id);
    }
}
