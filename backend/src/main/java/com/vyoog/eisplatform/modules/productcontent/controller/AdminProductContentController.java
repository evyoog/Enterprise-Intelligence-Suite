package com.vyoog.eisplatform.modules.productcontent.controller;

import com.vyoog.eisplatform.modules.productcontent.dto.ProductContentDtos.*;
import com.vyoog.eisplatform.modules.productcontent.model.ProductContentStatus;
import com.vyoog.eisplatform.modules.productcontent.service.ProductContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Product content administration (REQ-CAT-004.8). Platform administrators
 * only: SecurityConfig requires MANAGE_CATALOG for {@code /admin/products/**}
 * (BR-PCON-001).
 */
@RestController
@RequestMapping("/admin/products/{productId}/content")
@RequiredArgsConstructor
public class AdminProductContentController {

    private final ProductContentService service;

    @GetMapping
    public AdminContent list(@PathVariable Long productId) {
        return service.adminContent(productId);
    }

    @PostMapping("/upload-url")
    public UploadUrl uploadUrl(@PathVariable Long productId, @RequestBody UploadUrlRequest request) {
        return service.uploadUrl(productId, request);
    }

    @PostMapping
    public ResponseEntity<AdminItem> create(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId,
                                            @RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(actor(jwt), productId, request));
    }

    @PutMapping("/{itemId}")
    public AdminItem update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId, @PathVariable Long itemId,
                            @RequestBody ItemRequest request) {
        return service.update(actor(jwt), productId, itemId, request);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId, @PathVariable Long itemId) {
        service.delete(actor(jwt), productId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{itemId}/publish")
    public AdminItem publish(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId, @PathVariable Long itemId) {
        return service.setStatus(actor(jwt), productId, itemId, ProductContentStatus.PUBLISHED);
    }

    @PostMapping("/{itemId}/unpublish")
    public AdminItem unpublish(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId, @PathVariable Long itemId) {
        return service.setStatus(actor(jwt), productId, itemId, ProductContentStatus.DRAFT);
    }

    @PutMapping("/order")
    public AdminContent reorder(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId, @RequestBody OrderRequest request) {
        return service.reorder(actor(jwt), productId, request);
    }

    @GetMapping("/{itemId}/preview-url")
    public TemporaryUrl preview(@PathVariable Long productId, @PathVariable Long itemId,
                                @RequestParam(defaultValue = "false") boolean logo) {
        return service.previewUrl(productId, itemId, logo);
    }

    @GetMapping("/documentation-options")
    public List<DocumentationOption> documentationOptions(@PathVariable Long productId) {
        return service.documentationOptions(productId);
    }

    private static ProductContentService.Actor actor(Jwt jwt) {
        return new ProductContentService.Actor(jwt.getSubject(), jwt.getClaimAsString("email"));
    }
}
