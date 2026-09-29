package com.vyoog.eisplatform.modules.knowledgebase.controller;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleRequest;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 11.01.01 Knowledge Articles admin CRUD — MANAGE_KNOWLEDGE_BASE-gated in SecurityConfig. */
@RestController
@RequestMapping("/admin/knowledge-base")
@RequiredArgsConstructor
public class AdminKnowledgeArticleController {

    private final KnowledgeArticleService articleService;

    @GetMapping("/articles")
    public List<KnowledgeArticleDto> listAll() {
        return articleService.listAll();
    }

    @GetMapping("/articles/{id}")
    public KnowledgeArticleDto get(@PathVariable("id") Long id) {
        return articleService.getForAdmin(id);
    }

    @PostMapping("/articles")
    public KnowledgeArticleDto create(@Valid @RequestBody KnowledgeArticleRequest request) {
        return articleService.createArticle(request);
    }

    @PutMapping("/articles/{id}")
    public KnowledgeArticleDto edit(@PathVariable("id") Long id, @Valid @RequestBody KnowledgeArticleRequest request) {
        return articleService.editArticle(id, request);
    }

    @PostMapping("/articles/{id}/publish")
    public KnowledgeArticleDto publish(@PathVariable("id") Long id) {
        return articleService.setPublished(id, true);
    }

    @PostMapping("/articles/{id}/unpublish")
    public KnowledgeArticleDto unpublish(@PathVariable("id") Long id) {
        return articleService.setPublished(id, false);
    }

    @DeleteMapping("/articles/{id}")
    public void delete(@PathVariable("id") Long id) {
        articleService.deleteArticle(id);
    }
}
