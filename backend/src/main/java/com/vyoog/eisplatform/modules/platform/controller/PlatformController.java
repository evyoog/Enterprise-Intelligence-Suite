package com.vyoog.eisplatform.modules.platform.controller;

import com.vyoog.eisplatform.modules.platform.dto.PlatformCreateRequest;
import com.vyoog.eisplatform.modules.platform.dto.PlatformDto;
import com.vyoog.eisplatform.modules.platform.service.PlatformService;
import com.vyoog.eisplatform.modules.product.service.ProductImageService;
import com.vyoog.eisplatform.modules.product.service.ProductImageService.LoadedImage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * ADMIN-only everywhere except the uploaded-logo GET (enforced by
 * SecurityConfig, not here) — unlike products, platforms have no public
 * storefront-facing listing today, so nothing here needs to be open.
 */
@RestController
@RequestMapping("/platforms")
@RequiredArgsConstructor
public class PlatformController {

    private final PlatformService platformService;
    // Shared with ProductController: generic UUID-named file storage under
    // app.file-upload-dir, nothing product-specific about it despite the name.
    private final ProductImageService platformImageService;

    @GetMapping
    public List<PlatformDto> listPlatforms() {
        return platformService.listPlatforms();
    }

    @GetMapping("/{id}")
    public PlatformDto getPlatform(@PathVariable Long id) {
        return platformService.getPlatform(id);
    }

    @PostMapping
    public ResponseEntity<PlatformDto> createPlatform(@Valid @RequestBody PlatformCreateRequest request) {
        PlatformDto created = platformService.createPlatform(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public PlatformDto updatePlatform(@PathVariable Long id, @Valid @RequestBody PlatformCreateRequest request) {
        return platformService.updatePlatform(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlatform(@PathVariable Long id) {
        platformService.deletePlatform(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/images")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        String filename = platformImageService.store(file);
        return Map.of("url", "/api/platforms/images/" + filename);
    }

    // Public — an <img> tag can't attach an Authorization header, so this one
    // path must stay open even though every other /platforms/** route is
    // ADMIN-only (see SecurityConfig's ordering comment for why this rule has
    // to come before the general /platforms/** rule).
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        LoadedImage image = platformImageService.load(filename);
        return ResponseEntity.ok().contentType(image.contentType()).body(image.resource());
    }
}
