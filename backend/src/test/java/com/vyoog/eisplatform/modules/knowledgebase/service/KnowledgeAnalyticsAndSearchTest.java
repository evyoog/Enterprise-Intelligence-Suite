package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentRequest;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeReaderDtos;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeTestConfig;
import com.vyoog.eisplatform.modules.search.service.GlobalSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.ANONYMOUS;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.CONTRIBUTOR;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.PUBLISHER;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.member;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.request;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-KNW-005.10/.13, REQ-KNW-006: search over knowledge types, feedback, views and gaps. */
@SpringBootTest
@ActiveProfiles("test")
@Import(KnowledgeTestConfig.class)
class KnowledgeAnalyticsAndSearchTest {

    @Autowired
    private KnowledgeContentService contentService;
    @Autowired
    private KnowledgeReaderService readerService;
    @Autowired
    private KnowledgeAnalyticsService analyticsService;
    @Autowired
    private GlobalSearchService globalSearchService;

    private KnowledgeContentDto publish(KnowledgeContentRequest r) {
        KnowledgeContentDto c = contentService.create(CONTRIBUTOR, r);
        contentService.submit(CONTRIBUTOR, c.id());
        contentService.approve(PUBLISHER, c.id());
        return contentService.publish(PUBLISHER, c.id(), null);
    }

    @Test
    void publicKnowledgeOfEveryTypeIsSearchableButRestrictedIsNotInGlobalSearch() {
        KnowledgeContentDto faq = publish(request(KnowledgeContentType.FAQ, "Zanzibar supplier approval FAQ",
            "Approve suppliers from the vendor screen."));
        KnowledgeContentDto restricted = publish(request(KnowledgeContentType.TROUBLESHOOTING, "Zanzibar internal fix",
            "Only staff.", KnowledgeAudience.ADMIN, null, null, null, null));

        var global = globalSearchService.search("Zanzibar", null, null);
        assertThat(global.knowledgeArticles()).extracting(i -> i.id()).contains(faq.id()).doesNotContain(restricted.id());

        KnowledgeReaderDtos.SearchResult forReader = readerService.search(ANONYMOUS, "Zanzibar", null);
        assertThat(forReader.byType()).containsKey(KnowledgeContentType.FAQ);
        assertThat(forReader.results()).extracting(r -> r.id()).doesNotContain(restricted.id());
        KnowledgeReaderDtos.SearchResult forStaff = readerService.search(
            new KnowledgeReader(true, "sub-staff", null, null, java.util.Set.of(), true), "Zanzibar", null);
        assertThat(forStaff.results()).extracting(r -> r.id()).contains(restricted.id());

        // Products and modules are search results too.
        assertThat(readerService.search(ANONYMOUS, "inventory", null).modules()).isNotEmpty();
    }

    @Test
    void feedbackIsOneVotePerReaderAndNoNeedsAReason() {
        KnowledgeContentDto guide = publish(request(KnowledgeContentType.ARTICLE, "Feedback guide", "Body."));
        var reader = member(31, 901);
        assertThatThrownBy(() -> analyticsService.feedback(reader, guide.id(),
                new KnowledgeReaderDtos.FeedbackRequest("VOTE", false, null, null)))
            .isInstanceOf(KnowledgeException.class);
        analyticsService.feedback(reader, guide.id(), new KnowledgeReaderDtos.FeedbackRequest("VOTE", false, "UNCLEAR", "Hard"));
        analyticsService.feedback(reader, guide.id(), new KnowledgeReaderDtos.FeedbackRequest("VOTE", true, null, null));
        for (int i = 0; i < 5; i++) {
            analyticsService.feedback(member(40 + i, 901), guide.id(),
                new KnowledgeReaderDtos.FeedbackRequest("VOTE", false, "OUTDATED", null));
        }
        var analytics = analyticsService.analytics(30);
        assertThat(analytics.lowestRated()).anyMatch(r -> r.contentId().equals(guide.id()) && r.votes() == 6
            && r.helpfulPercent() == 17);
        assertThatThrownBy(() -> analyticsService.feedback(ANONYMOUS, guide.id(),
                new KnowledgeReaderDtos.FeedbackRequest("VOTE", true, null, null)))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("SIGN_IN_REQUIRED");
    }

    @Test
    void viewsAreCountedOncePerReaderPerHalfHourAndGapsAreDetected() {
        KnowledgeContentDto guide = publish(request(KnowledgeContentType.GETTING_STARTED, "Views guide", "Body."));
        readerService.get(ANONYMOUS, String.valueOf(guide.id()), "viewer-1");
        readerService.get(ANONYMOUS, String.valueOf(guide.id()), "viewer-1");
        readerService.get(ANONYMOUS, String.valueOf(guide.id()), "viewer-2");
        assertThat(analyticsService.analytics(1).mostViewed())
            .anyMatch(r -> r.contentId().equals(guide.id()) && r.count() == 2);

        for (int i = 0; i < 5; i++) {
            readerService.search(ANONYMOUS, "qwertyuiop configuration", null);
        }
        assertThat(analyticsService.gaps(30)).anyMatch(g -> g.query().equals("qwertyuiop configuration") && g.searches() == 5);
        assertThat(analyticsService.dashboard().gaps()).isNotEmpty();
    }

    @Test
    void clientEventsAreOnlyForVisibleContentAndKnownTypes() {
        KnowledgeContentDto staffOnly = publish(request(KnowledgeContentType.ARTICLE, "Staff note", "x",
            KnowledgeAudience.ADMIN, null, null, null, null));
        assertThatThrownBy(() -> analyticsService.recordClientEvent(ANONYMOUS,
                new KnowledgeReaderDtos.EventRequest("VIDEO_PLAYED", staffOnly.id(), null, null), "k"))
            .isInstanceOf(com.vyoog.eisplatform.common.exception.ResourceNotFoundException.class);
        assertThatThrownBy(() -> analyticsService.recordClientEvent(ANONYMOUS,
                new KnowledgeReaderDtos.EventRequest("CONTENT_VIEWED", staffOnly.id(), null, null), "k"))
            .isInstanceOf(KnowledgeException.class);
        assertThat(List.of(1)).hasSize(1);
    }
}
