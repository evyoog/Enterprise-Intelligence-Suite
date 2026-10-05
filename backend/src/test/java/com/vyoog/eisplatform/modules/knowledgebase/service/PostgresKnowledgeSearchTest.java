package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentRequest;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeVideoDtos;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.model.VideoSourceType;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeCategoryRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeModuleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeProductRepository;
import com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeTestConfig;
import com.vyoog.eisplatform.modules.search.service.GlobalSearchService;
import com.vyoog.eisplatform.modules.search.support.PostgresSearchTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.CONTRIBUTOR;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.PUBLISHER;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.request;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * C76 on real PostgreSQL: knowledge content of every type, video transcripts
 * and chapters are found through the platform search index (REQ-PRT-002),
 * and restricted content never enters the shared index. Runs only when
 * EIS_PG_TEST_URL is set; also checks that schema.sql holds the knowledge tables.
 */
@SpringBootTest
@ActiveProfiles({"test", "pgtest"})
@EnabledIfEnvironmentVariable(named = "EIS_PG_TEST_URL", matches = ".+")
@Import(KnowledgeTestConfig.class)
class PostgresKnowledgeSearchTest {

    @BeforeAll
    static void prepare() throws Exception {
        PostgresSearchTestSupport.prepareSchema();
    }

    @Autowired private KnowledgeContentService contentService;
    @Autowired private KnowledgeVideoService videoService;
    @Autowired private GlobalSearchService searchService;
    @Autowired private KnowledgeProductRepository productRepository;
    @Autowired private KnowledgeModuleRepository moduleRepository;
    @Autowired private KnowledgeCategoryRepository categoryRepository;
    @Autowired private JdbcTemplate jdbc;

    private void publish(Long id) {
        contentService.submit(CONTRIBUTOR, id);
        contentService.approve(PUBLISHER, id);
        contentService.publish(PUBLISHER, id, null);
    }

    @Test
    void transcriptsAndAllPublicTypesAreSearchableAndRestrictedContentIsNotIndexed() {
        Long product = productRepository.findBySlug("thiran").orElseThrow().getId();
        Long module = moduleRepository.findByProductIdOrderByDisplayOrderAscNameAsc(product).get(0).getId();
        Long category = categoryRepository.findAll().get(0).getId();
        KnowledgeContentRequest meta = new KnowledgeContentRequest(KnowledgeContentType.VIDEO, "Shop floor walkthrough",
            null, null, product, module, category, null, null, null, KnowledgeAudience.PUBLIC, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null);
        KnowledgeContentDto video = videoService.create(CONTRIBUTOR, new KnowledgeVideoDtos.VideoRequest(meta,
            VideoSourceType.YOUTUBE, "dQw4w9WgXcQ", null, null, 300, null, null, null,
            "Today we calibrate the torquewrench station before the shift.", "00:00 Intro\n01:30 Calibration"));
        publish(video.id());
        assertThat(searchService.search("torquewrench", "KNOWLEDGE", null, GlobalSearchService.Mode.KEYWORD, 20, false)
            .results()).extracting(r -> r.id()).contains(video.id());

        KnowledgeContentDto errorCode = contentService.create(CONTRIBUTOR, request(KnowledgeContentType.ERROR_CODE,
            "EIS-PO-777 Purchase order locked", "Another user is editing the purchase order."));
        publish(errorCode.id());
        assertThat(searchService.search("purchase order locked", null, null, GlobalSearchService.Mode.KEYWORD, 20, false)
            .knowledgeArticles()).extracting(r -> r.id()).contains(errorCode.id());

        KnowledgeContentDto staff = contentService.create(CONTRIBUTOR, request(KnowledgeContentType.ARTICLE,
            "Quixotic staff runbook", "Internal only.", KnowledgeAudience.ADMIN, null, null, null, null));
        publish(staff.id());
        Integer indexed = jdbc.queryForObject(
            "SELECT count(*) FROM search_document WHERE source_type = 'KNOWLEDGE' AND source_id = ?", Integer.class, staff.id());
        assertThat(indexed).isZero();
        Integer chunks = jdbc.queryForObject("SELECT count(*) FROM search_chunk c JOIN search_document d ON d.id = c.document_id "
            + "WHERE d.source_type = 'KNOWLEDGE' AND d.source_id = ?", Integer.class, staff.id());
        assertThat(chunks).isZero();
        assertThat(searchService.search("Quixotic", null, null, GlobalSearchService.Mode.KEYWORD, 20, false).results())
            .extracting(r -> r.id()).doesNotContain(staff.id());

        // Unpublishing removes the item from the index.
        contentService.unpublish(PUBLISHER, video.id());
        assertThat(searchService.search("torquewrench", "KNOWLEDGE", null, GlobalSearchService.Mode.KEYWORD, 20, false)
            .results()).extracting(r -> r.id()).doesNotContain(video.id());
    }
}
