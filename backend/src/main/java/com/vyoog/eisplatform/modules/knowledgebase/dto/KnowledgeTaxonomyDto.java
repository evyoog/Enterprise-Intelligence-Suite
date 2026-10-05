package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;

import java.util.List;

/** Knowledge taxonomy (C74). */
public final class KnowledgeTaxonomyDto {

    private KnowledgeTaxonomyDto() {
    }

    public record Product(Long id, String name, String slug, String description, Long catalogProductId,
                          int displayOrder, boolean active, List<Module> modules, long contentCount) {
    }

    public record Module(Long id, Long productId, String name, String slug, int displayOrder, boolean active,
                         long contentCount) {
    }

    public record Category(Long id, String name, String slug, KnowledgeContentType scope, int displayOrder,
                           boolean active) {
    }

    public record Taxonomy(List<Product> products, List<Category> categories) {
    }

    public record ProductRequest(String name, String slug, String description, Long catalogProductId,
                                 Integer displayOrder, Boolean active) {
    }

    public record ModuleRequest(Long productId, String name, String slug, Integer displayOrder, Boolean active) {
    }

    public record CategoryRequest(String name, String slug, KnowledgeContentType scope, Integer displayOrder,
                                  Boolean active) {
    }
}
