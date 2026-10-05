package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.search.model.SearchIndexRun;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background work for the search index (C70):
 * <ul>
 *   <li>Backfill: on startup, if the index is installed but empty, build it
 *       from every product, article and ticket (on a background thread).</li>
 *   <li>Every minute: embed passages still waiting for the model (for
 *       example after ai-service was down) and refresh the "Did you mean"
 *       word list if content changed.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchIndexJobs {

    private static final int PENDING_PER_RUN = 500;

    private final SearchIndexService indexService;
    private final SemanticIndexService semanticIndexService;
    private final SearchCapabilityService capabilityService;
    private final SearchSettings settings;

    @EventListener(ApplicationReadyEvent.class)
    public void backfillOnStartup() {
        if (!settings.isBackfillOnStartup()) {
            return;
        }
        Thread thread = new Thread(() -> {
            try {
                if (capabilityService.refresh().keywordIndex() && indexService.isEmpty()) {
                    log.info("Search index is empty; building it now");
                    SearchIndexRun run = indexService.rebuildAll(SearchIndexRun.Trigger.BACKFILL);
                    log.info("Search index backfill {}: {} documents, {} passages", run.getStatus(),
                        run.getDocuments(), run.getChunks());
                }
            } catch (RuntimeException e) {
                log.warn("Search index backfill did not run: {}", e.getMessage());
            }
        }, "search-backfill");
        thread.setDaemon(true);
        thread.start();
    }

    @Scheduled(fixedDelayString = "${app.search.maintenance-interval-ms:60000}", initialDelay = 60000)
    public void maintain() {
        if (indexService.isRebuilding() || !capabilityService.get().keywordIndex()) {
            return;
        }
        try {
            semanticIndexService.embedPending(PENDING_PER_RUN);
            indexService.refreshTermsIfDirty();
        } catch (RuntimeException e) {
            log.warn("Search index maintenance failed: {}", e.getMessage());
        }
    }
}
