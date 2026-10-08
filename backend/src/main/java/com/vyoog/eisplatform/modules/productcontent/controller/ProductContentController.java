package com.vyoog.eisplatform.modules.productcontent.controller;

import com.vyoog.eisplatform.modules.productcontent.dto.ProductContentDtos.PublicContent;
import com.vyoog.eisplatform.modules.productcontent.dto.ProductContentDtos.TemporaryUrl;
import com.vyoog.eisplatform.modules.productcontent.service.ProductContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public product content (REQ-CAT-004.9): covered by the existing
 * {@code GET /products/**} permitAll rule. Only published items of an Active
 * application; files only through short-lived signed links (BR-PCON-005, -006).
 */
@RestController
@RequestMapping("/products/{productId}/content")
@RequiredArgsConstructor
public class ProductContentController {

    private final ProductContentService service;

    @GetMapping
    public PublicContent content(@PathVariable Long productId) {
        return service.publicContent(productId);
    }

    @GetMapping("/{itemId}/download")
    public TemporaryUrl download(@PathVariable Long productId, @PathVariable Long itemId) {
        return service.download(productId, itemId);
    }
}
