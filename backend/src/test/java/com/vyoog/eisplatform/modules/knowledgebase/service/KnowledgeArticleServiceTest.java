package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleRequest;
import com.vyoog.eisplatform.modules.knowledgebase.model.ArticleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 11.01.01 Knowledge Articles (sprint 2027.1.1). */
@SpringBootTest
@ActiveProfiles("test")
class KnowledgeArticleServiceTest {

    @Autowired
    private KnowledgeArticleService articleService;

    @Test
    void createdArticleStartsAsDraftAndIsInvisibleToPublicSearch() {
        KnowledgeArticleDto created = articleService.createArticle(new KnowledgeArticleRequest("Getting started", "How to sign in."));
        assertThat(created.status()).isEqualTo(ArticleStatus.DRAFT);
        assertThat(created.version()).isEqualTo(1);

        assertThat(articleService.searchPublished("sign in")).noneMatch(a -> a.id().equals(created.id()));
        assertThatThrownBy(() -> articleService.getPublished(created.id())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void publishingMakesItSearchableThenUnpublishingHidesItAgain() {
        KnowledgeArticleDto created = articleService.createArticle(new KnowledgeArticleRequest("Resetting your password", "Use the forgot-password link."));
        articleService.setPublished(created.id(), true);

        assertThat(articleService.searchPublished("password")).anyMatch(a -> a.id().equals(created.id()));
        assertThat(articleService.getPublished(created.id()).status()).isEqualTo(ArticleStatus.PUBLISHED);

        articleService.setPublished(created.id(), false);
        assertThatThrownBy(() -> articleService.getPublished(created.id())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void editingIncrementsTheVersionCounter() {
        KnowledgeArticleDto created = articleService.createArticle(new KnowledgeArticleRequest("Title", "Body"));
        KnowledgeArticleDto edited = articleService.editArticle(created.id(), new KnowledgeArticleRequest("New title", "New body"));
        assertThat(edited.version()).isEqualTo(2);
        assertThat(edited.title()).isEqualTo("New title");
    }

    @Test
    void searchMatchesTitleOrBodyCaseInsensitively() {
        KnowledgeArticleDto article = articleService.createArticle(new KnowledgeArticleRequest("Billing FAQ", "Invoices are emailed monthly."));
        articleService.setPublished(article.id(), true);

        assertThat(articleService.searchPublished("BILLING")).anyMatch(a -> a.id().equals(article.id()));
        assertThat(articleService.searchPublished("invoices")).anyMatch(a -> a.id().equals(article.id()));
        assertThat(articleService.searchPublished("nonexistent-term-xyz")).noneMatch(a -> a.id().equals(article.id()));
    }

    @Test
    void deletingAnArticleRemovesItFromTheAdminList() {
        KnowledgeArticleDto article = articleService.createArticle(new KnowledgeArticleRequest("Temp", "Temp body"));
        articleService.deleteArticle(article.id());
        assertThatThrownBy(() -> articleService.getForAdmin(article.id())).isInstanceOf(ResourceNotFoundException.class);
    }
}
