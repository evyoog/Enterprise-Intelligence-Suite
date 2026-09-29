package com.vyoog.eisplatform.modules.knowledgebase.controller;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleDto;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public storefront reads — permitAll in SecurityConfig, same reasoning as
 * the public product catalog: a customer needs product knowledge before
 * they buy, whether or not they're signed in yet. */
@RestController
@RequestMapping("/knowledge-base")
@RequiredArgsConstructor
public class KnowledgeArticleController {

    private final KnowledgeArticleService articleService;

    @GetMapping("/articles")
    public List<KnowledgeArticleDto> search(@RequestParam(value = "q", required = false) String query) {
        return articleService.searchPublished(query);
    }

    @GetMapping("/articles/{id}")
    public KnowledgeArticleDto get(@PathVariable("id") Long id) {
        return articleService.getPublished(id);
    }
}
