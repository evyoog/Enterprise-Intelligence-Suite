package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeIndexSource;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.search.model.SearchDocument;
import com.vyoog.eisplatform.modules.search.model.SearchIndexRun;
import com.vyoog.eisplatform.modules.search.model.SearchSourceType;
import com.vyoog.eisplatform.modules.search.repository.SearchDocumentRepository;
import com.vyoog.eisplatform.modules.search.repository.SearchIndexRunRepository;
import com.vyoog.eisplatform.modules.search.repository.SearchSqlRepository;
import com.vyoog.eisplatform.modules.support.repository.SupportTicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Keeps the search index in step with products, knowledge articles and
 * support tickets (C70). {@link SearchChangeListener} calls
 * {@link #reindex} after every committed change; {@link #rebuildAll} rebuilds
 * everything (startup backfill and the admin "Rebuild index"). Each record is
 * written in its own new transaction, so an indexing problem never undoes
 * the change that triggered it.
 */
@Service
@Slf4j
public class SearchIndexService {

    public record SourceKey(SearchSourceType type, Long id) {
    }

    private static final int PAGE = 200;

    private final SearchDocumentRepository documentRepository;
    private final SearchIndexRunRepository runRepository;
    private final SearchSqlRepository sqlRepository;
    private final ProductRepository productRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeIndexSource knowledgeIndexSource;
    private final SupportTicketRepository ticketRepository;
    private final SemanticIndexService semanticIndexService;
    private final SearchCapabilityService capabilityService;
    private final TransactionTemplate newTransaction;
    private final AtomicBoolean rebuilding = new AtomicBoolean(false);
    private final AtomicBoolean termsDirty = new AtomicBoolean(true);

    public SearchIndexService(SearchDocumentRepository documentRepository, SearchIndexRunRepository runRepository,
                              SearchSqlRepository sqlRepository, ProductRepository productRepository,
                              KnowledgeArticleRepository articleRepository, KnowledgeIndexSource knowledgeIndexSource,
                              SupportTicketRepository ticketRepository,
                              SemanticIndexService semanticIndexService, SearchCapabilityService capabilityService,
                              PlatformTransactionManager transactionManager) {
        this.documentRepository = documentRepository;
        this.runRepository = runRepository;
        this.sqlRepository = sqlRepository;
        this.productRepository = productRepository;
        this.articleRepository = articleRepository;
        this.knowledgeIndexSource = knowledgeIndexSource;
        this.ticketRepository = ticketRepository;
        this.semanticIndexService = semanticIndexService;
        this.capabilityService = capabilityService;
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** Re-indexes changed records. Never throws: a failure is logged, and the
     * next rebuild repairs the record. Returns the ids of documents whose
     * passages need embedding. */
    public List<Long> reindex(Set<SourceKey> keys) {
        List<Long> toEmbed = new ArrayList<>();
        for (SourceKey key : keys) {
            try {
                Long documentId = newTransaction.execute(status -> indexOne(key));
                if (documentId != null) {
                    toEmbed.add(documentId);
                }
            } catch (RuntimeException e) {
                log.warn("Could not index {} {}: {}", key.type(), key.id(), e.getMessage());
            }
        }
        termsDirty.set(true);
        return toEmbed;
    }

    public SearchIndexRun rebuildAll(SearchIndexRun.Trigger trigger) {
        if (!rebuilding.compareAndSet(false, true)) {
            throw new InvalidStateException("The search index is already being rebuilt.");
        }
        SearchIndexRun run = new SearchIndexRun();
        run.setTriggerType(trigger);
        run.setStatus(SearchIndexRun.Status.RUNNING);
        run.setStartedAt(Instant.now());
        run = runRepository.save(run);
        try {
            capabilityService.refresh();
            int[] counts = new int[2];
            rebuildType(SearchSourceType.PRODUCT, p -> productRepository.findAll(p).getContent(),
                p -> SearchDocumentBuilder.fromProduct((Product) p), counts);
            rebuildType(SearchSourceType.KNOWLEDGE, p -> articleRepository.findAll(p).getContent(),
                a -> SearchDocumentBuilder.fromKnowledge(knowledgeIndexSource.indexable(
                    ((com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle) a).getId())), counts);
            rebuildType(SearchSourceType.TICKET, p -> ticketRepository.findAll(p).getContent(),
                t -> SearchDocumentBuilder.fromTicket((com.vyoog.eisplatform.modules.support.model.SupportTicket) t), counts);
            if (capabilityService.get().semantic()) {
                newTransaction.executeWithoutResult(s -> sqlRepository.deleteChunksOfNonPublicDocuments());
            }
            int embedded = Math.max(0, semanticIndexService.embedPending(Integer.MAX_VALUE));
            refreshTerms();
            run.setDocuments(counts[0]);
            run.setChunks(counts[1]);
            run.setEmbedded(embedded);
            run.setStatus(SearchIndexRun.Status.DONE);
        } catch (RuntimeException e) {
            log.error("Search index rebuild failed", e);
            run.setStatus(SearchIndexRun.Status.FAILED);
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            run.setError(message.length() > 1000 ? message.substring(0, 1000) : message);
        } finally {
            run.setFinishedAt(Instant.now());
            runRepository.save(run);
            rebuilding.set(false);
        }
        return run;
    }

    /** Every product on a platform (a platform rename changes their keywords). */
    public Set<SourceKey> productsOfPlatform(Long platformId) {
        Set<SourceKey> keys = new java.util.LinkedHashSet<>();
        newTransaction.executeWithoutResult(status -> productRepository.findAll().stream()
            .filter(p -> p.getPlatforms().stream().anyMatch(pl -> platformId.equals(pl.getId())))
            .forEach(p -> keys.add(new SourceKey(SearchSourceType.PRODUCT, p.getId()))));
        return keys;
    }

    public boolean isRebuilding() {
        return rebuilding.get();
    }

    public boolean isEmpty() {
        return documentRepository.count() == 0;
    }

    /** Rebuilds the "Did you mean" word list if anything changed since the last time. */
    public void refreshTermsIfDirty() {
        if (termsDirty.compareAndSet(true, false)) {
            try {
                refreshTerms();
            } catch (RuntimeException e) {
                termsDirty.set(true);
                log.warn("Could not refresh search terms: {}", e.getMessage());
            }
        }
    }

    public void refreshTerms() {
        if (capabilityService.get().keywordIndex()) {
            newTransaction.executeWithoutResult(s -> sqlRepository.refreshTerms());
        }
        termsDirty.set(false);
    }

    private Long indexOne(SourceKey key) {
        Optional<SearchDocument> built = switch (key.type()) {
            case PRODUCT -> productRepository.findById(key.id()).flatMap(SearchDocumentBuilder::fromProduct);
            case KNOWLEDGE -> SearchDocumentBuilder.fromKnowledge(knowledgeIndexSource.indexable(key.id()));
            case TICKET -> ticketRepository.findById(key.id()).flatMap(SearchDocumentBuilder::fromTicket);
        };
        if (built.isEmpty()) {
            documentRepository.findBySourceTypeAndSourceId(key.type(), key.id())
                .ifPresent(doc -> {
                    semanticIndexService.removeChunks(doc.getId());
                    documentRepository.delete(doc);
                });
            return null;
        }
        SearchDocument doc = upsert(built.get());
        return semanticIndexService.syncChunks(doc) > 0 ? doc.getId() : null;
    }

    private SearchDocument upsert(SearchDocument fresh) {
        SearchDocument doc = documentRepository.findBySourceTypeAndSourceId(fresh.getSourceType(), fresh.getSourceId())
            .orElseGet(SearchDocument::new);
        doc.setSourceType(fresh.getSourceType());
        doc.setSourceId(fresh.getSourceId());
        doc.setVisibility(fresh.getVisibility());
        doc.setOwnerCustomerId(fresh.getOwnerCustomerId());
        doc.setReference(fresh.getReference());
        doc.setTitle(fresh.getTitle());
        doc.setBody(fresh.getBody());
        doc.setKeywords(fresh.getKeywords());
        doc.setTitleFolded(fresh.getTitleFolded());
        doc.setBodyFolded(fresh.getBodyFolded());
        doc.setKeywordsFolded(fresh.getKeywordsFolded());
        doc.setContentUpdatedAt(fresh.getContentUpdatedAt());
        doc.setIndexedAt(Instant.now());
        return documentRepository.saveAndFlush(doc);
    }

    private <T> void rebuildType(SearchSourceType type, Function<PageRequest, List<T>> page,
                                 Function<Object, Optional<SearchDocument>> builder, int[] counts) {
        Instant started = Instant.now();
        for (int number = 0; ; number++) {
            PageRequest request = PageRequest.of(number, PAGE, Sort.by("id"));
            List<Integer> pageCounts = newTransaction.execute(status -> {
                List<T> rows = page.apply(request);
                int docs = 0;
                int chunks = 0;
                for (T row : rows) {
                    Optional<SearchDocument> built = builder.apply(row);
                    if (built.isPresent()) {
                        SearchDocument doc = upsert(built.get());
                        chunks += semanticIndexService.syncChunks(doc);
                        docs++;
                    }
                }
                return List.of(rows.size(), docs, chunks);
            });
            counts[0] += pageCounts.get(1);
            counts[1] += pageCounts.get(2);
            if (pageCounts.get(0) < PAGE) {
                break;
            }
        }
        // Documents not written by this rebuild belong to records that were
        // deleted or are no longer searchable. Passages go with them (ON DELETE CASCADE).
        newTransaction.executeWithoutResult(status -> documentRepository.deleteIndexedBefore(type, started));
    }
}
