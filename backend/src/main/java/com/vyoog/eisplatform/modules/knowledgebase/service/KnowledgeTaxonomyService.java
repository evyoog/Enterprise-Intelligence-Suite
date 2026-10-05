package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeTaxonomyDto;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeCategory;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeModule;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeProduct;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeCategoryRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeModuleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Knowledge taxonomy as data (C74, REQ-KNW-002.4): products, modules and
 * categories managed by publishers. Rows in use cannot be deleted, only
 * deactivated (BR-KCON-007).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeTaxonomyService {

    private final KnowledgeProductRepository productRepository;
    private final KnowledgeModuleRepository moduleRepository;
    private final KnowledgeCategoryRepository categoryRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final AuditService auditService;

    public KnowledgeTaxonomyDto.Taxonomy taxonomy() {
        List<KnowledgeModule> modules = moduleRepository.findAllByOrderByProductIdAscDisplayOrderAscNameAsc();
        List<KnowledgeTaxonomyDto.Product> products = productRepository.findAllByOrderByDisplayOrderAscNameAsc().stream()
            .map(p -> new KnowledgeTaxonomyDto.Product(p.getId(), p.getName(), p.getSlug(), p.getDescription(),
                p.getCatalogProductId(), p.getDisplayOrder(), p.isActive(),
                modules.stream().filter(m -> m.getProductId().equals(p.getId())).map(this::moduleDto).toList(),
                articleRepository.countByProductId(p.getId())))
            .toList();
        List<KnowledgeTaxonomyDto.Category> categories = categoryRepository.findAllByOrderByScopeAscDisplayOrderAscNameAsc()
            .stream().map(KnowledgeTaxonomyService::categoryDto).toList();
        return new KnowledgeTaxonomyDto.Taxonomy(products, categories);
    }

    @Transactional
    public KnowledgeTaxonomyDto.Product saveProduct(KnowledgeActor actor, Long id, KnowledgeTaxonomyDto.ProductRequest r) {
        KnowledgeProduct p = id == null ? new KnowledgeProduct()
            : productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        p.setName(required(r.name(), "name"));
        String slug = KnowledgeContentService.slugify(r.slug() == null || r.slug().isBlank() ? r.name() : r.slug());
        if (!slug.equals(p.getSlug()) && productRepository.existsBySlug(slug)) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Another product already uses this slug.");
        }
        p.setSlug(slug);
        p.setDescription(r.description());
        p.setCatalogProductId(r.catalogProductId());
        p.setDisplayOrder(r.displayOrder() == null ? p.getDisplayOrder() : r.displayOrder());
        p.setActive(r.active() == null || r.active());
        p = productRepository.save(p);
        audit(actor, id == null ? "KNOWLEDGE_TAXONOMY_CREATED" : "KNOWLEDGE_TAXONOMY_UPDATED", "Product " + p.getName());
        return new KnowledgeTaxonomyDto.Product(p.getId(), p.getName(), p.getSlug(), p.getDescription(),
            p.getCatalogProductId(), p.getDisplayOrder(), p.isActive(), List.of(), articleRepository.countByProductId(p.getId()));
    }

    @Transactional
    public KnowledgeTaxonomyDto.Module saveModule(KnowledgeActor actor, Long id, KnowledgeTaxonomyDto.ModuleRequest r) {
        KnowledgeModule m = id == null ? new KnowledgeModule()
            : moduleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Module not found"));
        if (r.productId() == null || !productRepository.existsById(r.productId())) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Choose the product this module belongs to.");
        }
        if (id != null && !r.productId().equals(m.getProductId()) && articleRepository.countByModuleId(id) > 0) {
            throw KnowledgeException.conflict("A module with content cannot move to another product.");
        }
        m.setProductId(r.productId());
        m.setName(required(r.name(), "name"));
        String slug = KnowledgeContentService.slugify(r.slug() == null || r.slug().isBlank() ? r.name() : r.slug());
        if ((id == null || !slug.equals(m.getSlug())) && moduleRepository.existsByProductIdAndSlug(r.productId(), slug)) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Another module of this product already uses this slug.");
        }
        m.setSlug(slug);
        m.setDisplayOrder(r.displayOrder() == null ? m.getDisplayOrder() : r.displayOrder());
        m.setActive(r.active() == null || r.active());
        m = moduleRepository.save(m);
        audit(actor, id == null ? "KNOWLEDGE_TAXONOMY_CREATED" : "KNOWLEDGE_TAXONOMY_UPDATED", "Module " + m.getName());
        return moduleDto(m);
    }

    @Transactional
    public KnowledgeTaxonomyDto.Category saveCategory(KnowledgeActor actor, Long id, KnowledgeTaxonomyDto.CategoryRequest r) {
        KnowledgeCategory c = id == null ? new KnowledgeCategory()
            : categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        c.setName(required(r.name(), "name"));
        c.setSlug(KnowledgeContentService.slugify(r.slug() == null || r.slug().isBlank() ? r.name() : r.slug()));
        c.setScope(r.scope());
        c.setDisplayOrder(r.displayOrder() == null ? c.getDisplayOrder() : r.displayOrder());
        c.setActive(r.active() == null || r.active());
        c = categoryRepository.save(c);
        audit(actor, id == null ? "KNOWLEDGE_TAXONOMY_CREATED" : "KNOWLEDGE_TAXONOMY_UPDATED", "Category " + c.getName());
        return categoryDto(c);
    }

    @Transactional
    public void deleteProduct(KnowledgeActor actor, Long id) {
        KnowledgeProduct p = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (articleRepository.countByProductId(id) > 0 || moduleRepository.countByProductId(id) > 0) {
            throw inUse();
        }
        productRepository.delete(p);
        audit(actor, "KNOWLEDGE_TAXONOMY_DELETED", "Product " + p.getName());
    }

    @Transactional
    public void deleteModule(KnowledgeActor actor, Long id) {
        KnowledgeModule m = moduleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Module not found"));
        if (articleRepository.countByModuleId(id) > 0) {
            throw inUse();
        }
        moduleRepository.delete(m);
        audit(actor, "KNOWLEDGE_TAXONOMY_DELETED", "Module " + m.getName());
    }

    @Transactional
    public void deleteCategory(KnowledgeActor actor, Long id) {
        KnowledgeCategory c = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (articleRepository.countByCategoryId(id) > 0) {
            throw inUse();
        }
        categoryRepository.delete(c);
        audit(actor, "KNOWLEDGE_TAXONOMY_DELETED", "Category " + c.getName());
    }

    private static KnowledgeException inUse() {
        return new KnowledgeException(HttpStatus.CONFLICT, "IN_USE",
            "This is used by content. Deactivate it instead of deleting it.");
    }

    private KnowledgeTaxonomyDto.Module moduleDto(KnowledgeModule m) {
        return new KnowledgeTaxonomyDto.Module(m.getId(), m.getProductId(), m.getName(), m.getSlug(), m.getDisplayOrder(),
            m.isActive(), articleRepository.countByModuleId(m.getId()));
    }

    private static KnowledgeTaxonomyDto.Category categoryDto(KnowledgeCategory c) {
        return new KnowledgeTaxonomyDto.Category(c.getId(), c.getName(), c.getSlug(), c.getScope(), c.getDisplayOrder(),
            c.isActive());
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "The " + field + " is required.");
        }
        return value.trim();
    }

    private void audit(KnowledgeActor actor, String action, String detail) {
        auditService.recordSuccess(action, actor.keycloakSub(), null, actor.email(), "KnowledgeTaxonomy", null, null, detail);
    }
}
