package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeCompareDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentRequest;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeItemDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgePublishRequest;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeWorkflowState;
import com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.ANONYMOUS;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.CONTRIBUTOR;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.PUBLISHER;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.json;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.member;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.request;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-KNW-002 content model, workflow and versions; BR-KVS-001 visibility. */
@SpringBootTest
@ActiveProfiles("test")
@Import(KnowledgeTestConfig.class)
class KnowledgeContentServiceTest {

    @Autowired
    private KnowledgeContentService contentService;
    @Autowired
    private KnowledgeReaderService readerService;

    private KnowledgeContentDto published(KnowledgeContentRequest request) {
        KnowledgeContentDto created = contentService.create(CONTRIBUTOR, request);
        contentService.submit(CONTRIBUTOR, created.id());
        contentService.approve(PUBLISHER, created.id());
        return contentService.publish(PUBLISHER, created.id(), new KnowledgePublishRequest("MINOR", null));
    }

    @Test
    void workflowRunsDraftReviewApprovedPublishedWithVersions() {
        KnowledgeContentDto created = contentService.create(CONTRIBUTOR,
            request(KnowledgeContentType.PRODUCT_GUIDE, "Create a purchase order", "Open Purchase and choose New."));
        assertThat(created.workflowState()).isEqualTo(KnowledgeWorkflowState.DRAFT);
        assertThat(created.live()).isFalse();
        assertThat(created.slug()).isEqualTo("create-a-purchase-order");

        assertThatThrownBy(() -> contentService.publish(PUBLISHER, created.id(), null))
            .isInstanceOf(KnowledgeException.class).hasMessageContaining("not allowed");

        contentService.submit(CONTRIBUTOR, created.id());
        KnowledgeContentDto returned = contentService.returnToDraft(PUBLISHER, created.id(), "Add a screenshot");
        assertThat(returned.workflowState()).isEqualTo(KnowledgeWorkflowState.DRAFT);
        assertThat(returned.reviewComment()).isEqualTo("Add a screenshot");

        contentService.submit(CONTRIBUTOR, created.id());
        contentService.approve(PUBLISHER, created.id());
        KnowledgeContentDto live = contentService.publish(PUBLISHER, created.id(), null);
        assertThat(live.live()).isTrue();
        assertThat(live.liveVersion()).isEqualTo("1.0");
        assertThat(readerService.get(ANONYMOUS, String.valueOf(created.id()), "k1").versionLabel()).isEqualTo("1.0");

        // Editing published content starts a draft; readers still see 1.0.
        KnowledgeContentRequest edit = request(KnowledgeContentType.PRODUCT_GUIDE, "Create a purchase order",
            "Open Purchase, choose New and add lines.");
        KnowledgeContentDto draft = contentService.update(CONTRIBUTOR, created.id(), edit);
        assertThat(draft.workflowState()).isEqualTo(KnowledgeWorkflowState.DRAFT);
        assertThat(draft.live()).isTrue();
        assertThat(readerService.get(ANONYMOUS, created.slug(), "k1").blocks().get(0).get("text").asText())
            .isEqualTo("Open Purchase and choose New.");

        contentService.submit(CONTRIBUTOR, created.id());
        contentService.approve(PUBLISHER, created.id());
        assertThat(contentService.publish(PUBLISHER, created.id(), new KnowledgePublishRequest("MINOR", null)).liveVersion())
            .isEqualTo("1.1");
        contentService.update(PUBLISHER, created.id(), edit);
        contentService.submit(PUBLISHER, created.id());
        contentService.approve(PUBLISHER, created.id());
        assertThat(contentService.publish(PUBLISHER, created.id(), new KnowledgePublishRequest("MAJOR", null)).liveVersion())
            .isEqualTo("2.0");
        assertThat(contentService.versions(created.id())).extracting(v -> v.versionLabel()).containsExactly("2.0", "1.1", "1.0");

        KnowledgeCompareDto compare = contentService.compare(created.id(), "1.0", "1.1");
        assertThat(compare.changes()).extracting(KnowledgeCompareDto.Change::kind).containsExactly("CHANGED");

        KnowledgeContentDto restored = contentService.restoreVersion(PUBLISHER, created.id(), "1.0");
        assertThat(restored.workflowState()).isEqualTo(KnowledgeWorkflowState.DRAFT);
        assertThat(restored.blocks().get(0).get("text").asText()).isEqualTo("Open Purchase and choose New.");
        assertThat(contentService.versions(created.id())).hasSize(3);
    }

    @Test
    void contributorCannotChangeContentInReview() {
        KnowledgeContentDto created = contentService.create(CONTRIBUTOR,
            request(KnowledgeContentType.FAQ, "How do I reset MFA?", "Ask your administrator."));
        contentService.submit(CONTRIBUTOR, created.id());
        assertThatThrownBy(() -> contentService.update(CONTRIBUTOR, created.id(),
                request(KnowledgeContentType.FAQ, "Changed", "x")))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("PERMISSION_DENIED");
    }

    @Test
    void deprecatedStaysVisibleWithBannerAndArchivedIsHidden() {
        KnowledgeContentDto live = published(request(KnowledgeContentType.ARTICLE, "Old screen", "Use the old screen."));
        contentService.deprecate(PUBLISHER, live.id());
        KnowledgeItemDto item = readerService.get(ANONYMOUS, String.valueOf(live.id()), "k2");
        assertThat(item.deprecated()).isTrue();
        contentService.archive(PUBLISHER, live.id());
        assertThatThrownBy(() -> readerService.get(ANONYMOUS, String.valueOf(live.id()), "k2"))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThat(contentService.restore(PUBLISHER, live.id()).workflowState()).isEqualTo(KnowledgeWorkflowState.DRAFT);
    }

    @Test
    void audienceIsAppliedBeforeAnythingIsReturned() {
        KnowledgeContentDto orgOnly = published(request(KnowledgeContentType.ARTICLE, "Org A private guide",
            "Only for organization 501.", KnowledgeAudience.ORGANIZATION, List.of(501L), null, null, null));
        KnowledgeContentDto signedIn = published(request(KnowledgeContentType.ARTICLE, "Customers only guide",
            "Signed in readers.", KnowledgeAudience.CUSTOMER, null, null, null, null));

        assertThat(readerService.get(member(1, 501), String.valueOf(orgOnly.id()), "a").title()).isEqualTo("Org A private guide");
        assertThatThrownBy(() -> readerService.get(member(2, 502), String.valueOf(orgOnly.id()), "b"))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> readerService.get(ANONYMOUS, String.valueOf(signedIn.id()), "c"))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThat(readerService.get(member(2, 502), String.valueOf(signedIn.id()), "d").title()).isEqualTo("Customers only guide");

        assertThat(readerService.list(member(2, 502), null, null, null, null, null, "Org A private", null, 0, 50).items())
            .isEmpty();
        assertThat(readerService.list(member(1, 501), null, null, null, null, null, "Org A private", null, 0, 50).items())
            .extracting(s -> s.id()).containsExactly(orgOnly.id());
        assertThat(readerService.search(member(2, 502), "Org A private guide", null).results()).isEmpty();
        assertThat(readerService.search(member(1, 501), "Org A private guide", null).results())
            .extracting(s -> s.id()).contains(orgOnly.id());
    }

    @Test
    void expiredAndNotYetEffectiveContentIsHidden() {
        KnowledgeContentRequest base = request(KnowledgeContentType.ARTICLE, "Seasonal notice", "Holiday hours.");
        KnowledgeContentRequest expiring = new KnowledgeContentRequest(base.contentType(), base.title(), null, null, null,
            null, null, null, null, null, null, null, null, null, null, Instant.now().minus(2, ChronoUnit.DAYS), null,
            Instant.now().minus(1, ChronoUnit.DAYS), null, null, null, null, null, base.blocks(), null);
        KnowledgeContentDto expired = published(expiring);
        assertThat(expired.expired()).isTrue();
        assertThatThrownBy(() -> readerService.get(ANONYMOUS, String.valueOf(expired.id()), "e"))
            .isInstanceOf(ResourceNotFoundException.class);

        KnowledgeContentRequest future = new KnowledgeContentRequest(base.contentType(), "Next year notice", null, null,
            null, null, null, null, null, null, null, null, null, null, null, Instant.now().plus(10, ChronoUnit.DAYS), null,
            null, null, null, null, null, null, base.blocks(), null);
        KnowledgeContentDto notYet = published(future);
        assertThatThrownBy(() -> readerService.get(ANONYMOUS, String.valueOf(notYet.id()), "f"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void scheduledPublishingHappensWhenItsTimeComes() {
        KnowledgeContentDto created = contentService.create(CONTRIBUTOR,
            request(KnowledgeContentType.RELEASE_NOTE, "Release 4.2", "New dashboards."));
        contentService.submit(CONTRIBUTOR, created.id());
        contentService.approve(PUBLISHER, created.id());
        Instant at = Instant.now().plus(1, ChronoUnit.HOURS);
        KnowledgeContentDto scheduled = contentService.publish(PUBLISHER, created.id(), new KnowledgePublishRequest(null, at));
        assertThat(scheduled.workflowState()).isEqualTo(KnowledgeWorkflowState.SCHEDULED);
        assertThat(contentService.publishDue(Instant.now())).isZero();
        assertThat(contentService.publishDue(at.plusSeconds(1))).isGreaterThanOrEqualTo(1);
        assertThat(contentService.get(created.id()).liveVersion()).isEqualTo("1.0");
    }

    @Test
    void blocksAreValidatedAndNeverHoldHtmlLinks() {
        KnowledgeContentRequest bad = new KnowledgeContentRequest(KnowledgeContentType.ARTICLE, "Bad link", null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, json("[{\"type\":\"link\",\"label\":\"Click\",\"url\":\"javascript:alert(1)\"}]"), null);
        assertThatThrownBy(() -> contentService.create(CONTRIBUTOR, bad))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("INVALID_CONTENT");
        KnowledgeContentRequest unknown = new KnowledgeContentRequest(KnowledgeContentType.ARTICLE, "Unknown", null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, json("[{\"type\":\"script\",\"text\":\"x\"}]"), null);
        assertThatThrownBy(() -> contentService.create(CONTRIBUTOR, unknown)).isInstanceOf(KnowledgeException.class);
        KnowledgeContentRequest route = new KnowledgeContentRequest(KnowledgeContentType.ARTICLE, "Direct action", null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            "https://evil.example", null, null, null, null);
        assertThatThrownBy(() -> contentService.create(CONTRIBUTOR, route)).isInstanceOf(KnowledgeException.class);
    }

    @Test
    void courseTypeWaitsForTheAcademy() {
        assertThatThrownBy(() -> contentService.create(CONTRIBUTOR,
                request(KnowledgeContentType.COURSE, "Valam.ai Fundamentals", "Lesson 1")))
            .isInstanceOf(KnowledgeException.class);
    }
}
