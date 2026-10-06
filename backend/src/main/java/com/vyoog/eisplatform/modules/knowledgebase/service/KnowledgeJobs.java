package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeContentVersionRepository;
import com.vyoog.eisplatform.modules.search.model.SearchSourceType;
import com.vyoog.eisplatform.modules.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/** Scheduled publishing (REQ-KNW-002.5), search updates when an effective or
 * expiry date passes (BR-KCON-003) and orphan upload cleanup (REQ-KNW-003.9). */
@Component
@ConditionalOnProperty(name = "eis.knowledge.jobs.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class KnowledgeJobs {

    private final KnowledgeContentService contentService;
    private final KnowledgeMediaService mediaService;
    private final KnowledgeContentVersionRepository versionRepository;
    private final SearchIndexService searchIndexService;
    private volatile Instant lastTimeCheck = Instant.now();

    @Scheduled(fixedDelayString = "${eis.knowledge.jobs.publish-interval-ms:60000}", initialDelay = 30000)
    public void publishScheduled() {
        try {
            Instant now = Instant.now();
            int published = contentService.publishDue(now);
            if (published > 0) {
                log.info("Published {} scheduled knowledge item(s)", published);
            }
            Set<SearchIndexService.SourceKey> keys = new LinkedHashSet<>();
            versionRepository.contentWithTimeChanges(lastTimeCheck, now)
                .forEach(id -> keys.add(new SearchIndexService.SourceKey(SearchSourceType.KNOWLEDGE, id)));
            lastTimeCheck = now;
            if (!keys.isEmpty()) {
                searchIndexService.reindex(keys);
            }
        } catch (RuntimeException e) {
            log.warn("Scheduled knowledge publishing failed: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${eis.knowledge.jobs.cleanup-interval-ms:3600000}", initialDelay = 120000)
    public void cleanupOrphans() {
        if (!mediaService.storageStatus().configured()) {
            return;
        }
        try {
            int removed = mediaService.cleanupOrphans(Instant.now());
            if (removed > 0) {
                log.info("Removed {} unfinished knowledge upload(s)", removed);
            }
        } catch (RuntimeException e) {
            log.warn("Knowledge upload cleanup failed: {}", e.getMessage());
        }
    }
}
