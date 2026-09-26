package com.vyoog.eisplatform.modules.product.controller;

import com.vyoog.eisplatform.modules.product.dto.ProductCreateRequest;
import com.vyoog.eisplatform.modules.product.dto.ProductDto;
import com.vyoog.eisplatform.modules.product.dto.ProductSearchResponse;
import com.vyoog.eisplatform.modules.product.service.ProductImageService;
import com.vyoog.eisplatform.modules.product.service.ProductImageService.LoadedImage;
import com.vyoog.eisplatform.modules.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductImageService productImageService;

    @GetMapping
    public List<ProductDto> listProducts() {
        return productService.listProducts();
    }

    // ADMIN-only — enforced by SecurityConfig (must be matched before the
    // permitAll GET /products/** rule; see SecurityConfig's ordering comment).
    @GetMapping("/admin")
    public List<ProductDto> listAllProducts() {
        return productService.listAllProducts();
    }

    // Public — falls under the general GET /products/** permitAll rule, same
    // scope as listProducts (ACTIVE-only). Spring MVC matches this exact
    // literal path ahead of the "/{id}" pattern below, same as "/admin" already does.
    @GetMapping("/search")
    public ProductSearchResponse searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long platformId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        return productService.searchProducts(q, category, platformId, sortBy, sortDir, false);
    }

    // ADMIN-only — matched explicitly in SecurityConfig alongside "/admin"
    // itself, since it doesn't fall under that exact-path rule.
    @GetMapping("/admin/search")
    public ProductSearchResponse searchAllProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long platformId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        return productService.searchProducts(q, category, platformId, sortBy, sortDir, true);
    }

    @GetMapping("/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    // ADMIN-only — enforced by SecurityConfig, not by anything in this method.
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        ProductDto created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ADMIN-only — enforced by SecurityConfig. Reuses ProductCreateRequest since
    // an update needs the exact same fields as a create; a separate
    // ProductUpdateRequest record would just be a duplicate of this one.
    @PutMapping("/{id}")
    public ProductDto updateProduct(@PathVariable Long id, @Valid @RequestBody ProductCreateRequest request) {
        return productService.updateProduct(id, request);
    }

    // ADMIN-only — enforced by SecurityConfig. Blocked with a 409 (see
    // ProductService#deleteProduct) if the product has real subscription/
    // access/usage history — deactivate it instead in that case.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // 02.01.01.04 Publish product (sprint 2026.4.1). ADMIN-only — enforced by
    // SecurityConfig (explicit rule, since this isn't the exact-path POST /products).
    @PostMapping("/{id}/publish")
    public ProductDto publishProduct(@PathVariable Long id) {
        return productService.publishProduct(id);
    }

    // 02.01.01.05 Retire product (sprint 2026.4.1). ADMIN-only — enforced by SecurityConfig.
    @PostMapping("/{id}/retire")
    public ProductDto retireProduct(@PathVariable Long id) {
        return productService.retireProduct(id);
    }

    // ADMIN-only — enforced by SecurityConfig.
    @PostMapping("/images")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        String filename = productImageService.store(file);
        return Map.of("url", "/api/products/images/" + filename);
    }

    // Public — an uploaded product image needs to load for every storefront
    // visitor, not just admins.
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        LoadedImage image = productImageService.load(filename);
        return ResponseEntity.ok().contentType(image.contentType()).body(image.resource());
    }
}
