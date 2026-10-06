package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.modules.search.dto.SearchIndexRunDto;
import com.vyoog.eisplatform.modules.search.dto.SearchIndexStatusDto;
import com.vyoog.eisplatform.modules.search.model.SearchIndexRun;
import com.vyoog.eisplatform.modules.search.model.SearchSourceType;
import com.vyoog.eisplatform.modules.search.repository.SearchCapabilities;
import com.vyoog.eisplatform.modules.search.repository.SearchDocumentRepository;
import com.vyoog.eisplatform.modules.search.repository.SearchIndexRunRepository;
import com.vyoog.eisplatform.modules.search.repository.SearchSqlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/** Admin view and "Rebuild index" for the search index (C70). */
@Service
@RequiredArgsConstructor
public class SearchAdminService {

    private final SearchIndexService indexService;
    private final SearchCapabilityService capabilityService;
    private final SearchDocumentRepository documentRepository;
    private final SearchIndexRunRepository runRepository;
    private final SearchSqlRepository sqlRepository;
    private final EmbeddingClient embeddingClient;
    private final SearchSettings settings;

    public SearchIndexStatusDto status() {
        SearchCapabilities capabilities = capabilityService.refresh();
        Map<String, Long> documents = new LinkedHashMap<>();
        if (capabilities.keywordIndex() || !capabilities.postgres()) {
            for (SearchSourceType type : SearchSourceType.values()) {
                documents.put(type.name(), documentRepository.countBySourceType(type));
            }
        }
        long[] chunks = capabilities.semantic() ? sqlRepository.chunkCounts() : new long[] {0, 0, 0};
        return new SearchIndexStatusDto(capabilities.keywordIndex() ? "POSTGRES" : "BASIC",
            capabilities.keywordIndex(), capabilities.semantic(), documents, chunks[0], chunks[1], chunks[2],
            embeddingClient.status(), runRepository.findTopByOrderByStartedAtDesc().map(SearchAdminService::toDto).orElse(null),
            settings.asMap());
    }

    /** Starts a full rebuild on a background thread and returns at once;
     * {@link #status()} shows its progress. {@code reembed}: also embed every
     * passage again (after the embedding model changed). */
    public void startRebuild(boolean reembed) {
        if (indexService.isRebuilding()) {
            throw new InvalidStateException("The search index is already being rebuilt.");
        }
        if (!capabilityService.refresh().keywordIndex() && capabilityService.get().postgres()) {
            throw new InvalidStateException("The search index tables are not installed. Apply database migration V020 first.");
        }
        if (reembed && capabilityService.get().semantic()) {
            sqlRepository.clearEmbeddings();
        }
        Thread thread = new Thread(() -> indexService.rebuildAll(SearchIndexRun.Trigger.ADMIN), "search-rebuild");
        thread.setDaemon(true);
        thread.start();
    }

    static SearchIndexRunDto toDto(SearchIndexRun run) {
        return new SearchIndexRunDto(run.getId(), run.getTriggerType().name(), run.getStatus().name(),
            run.getDocuments(), run.getChunks(), run.getEmbedded(), run.getError(), run.getStartedAt(),
            run.getFinishedAt());
    }
}
