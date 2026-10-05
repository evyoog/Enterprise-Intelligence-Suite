package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.platform.model.Platform;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.search.model.SearchSourceType;
import com.vyoog.eisplatform.modules.search.service.SearchIndexService.SourceKey;
import com.vyoog.eisplatform.modules.support.model.SupportTicket;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.AbstractCollectionEvent;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostCollectionRecreateEvent;
import org.hibernate.event.spi.PostCollectionRecreateEventListener;
import org.hibernate.event.spi.PostCollectionUpdateEvent;
import org.hibernate.event.spi.PostCollectionUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Automatic re-indexing (C70). Listens to every database write Hibernate
 * makes to products (and their platforms), knowledge articles and support
 * tickets, whatever service made it, and re-indexes those records once the
 * transaction has committed. A rolled-back change is never indexed. Passages
 * are embedded on a background thread, so saving a record never waits for
 * the embedding model.
 */
@Component
@Slf4j
public class SearchChangeListener implements PostInsertEventListener, PostUpdateEventListener,
    PostDeleteEventListener, PostCollectionUpdateEventListener, PostCollectionRecreateEventListener {

    private static final Object PENDING_KEY = new Object();

    private final EntityManagerFactory entityManagerFactory;
    private final ObjectProvider<SearchIndexService> indexService;
    private final ObjectProvider<SemanticIndexService> semanticIndexService;
    private final ExecutorService embedExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "search-embed");
        thread.setDaemon(true);
        return thread;
    });

    public SearchChangeListener(EntityManagerFactory entityManagerFactory,
                                ObjectProvider<SearchIndexService> indexService,
                                ObjectProvider<SemanticIndexService> semanticIndexService) {
        this.entityManagerFactory = entityManagerFactory;
        this.indexService = indexService;
        this.semanticIndexService = semanticIndexService;
    }

    @PostConstruct
    void register() {
        EventListenerRegistry registry = entityManagerFactory.unwrap(SessionFactoryImplementor.class)
            .getServiceRegistry().getService(EventListenerRegistry.class);
        registry.appendListeners(EventType.POST_INSERT, this);
        registry.appendListeners(EventType.POST_UPDATE, this);
        registry.appendListeners(EventType.POST_DELETE, this);
        registry.appendListeners(EventType.POST_COLLECTION_UPDATE, this);
        registry.appendListeners(EventType.POST_COLLECTION_RECREATE, this);
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        track(event.getEntity());
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        track(event.getEntity());
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        track(event.getEntity());
    }

    @Override
    public void onPostUpdateCollection(PostCollectionUpdateEvent event) {
        trackOwner(event);
    }

    @Override
    public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
        trackOwner(event);
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }

    private void trackOwner(AbstractCollectionEvent event) {
        track(event.getAffectedOwnerOrNull());
    }

    private void track(Object entity) {
        Set<SourceKey> keys = keysFor(entity);
        if (keys.isEmpty() || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        @SuppressWarnings("unchecked")
        Set<SourceKey> pending = (Set<SourceKey>) TransactionSynchronizationManager.getResource(PENDING_KEY);
        if (pending == null) {
            Set<SourceKey> created = new LinkedHashSet<>();
            pending = created;
            TransactionSynchronizationManager.bindResource(PENDING_KEY, created);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    apply(created);
                }

                @Override
                public void afterCompletion(int status) {
                    if (TransactionSynchronizationManager.hasResource(PENDING_KEY)) {
                        TransactionSynchronizationManager.unbindResource(PENDING_KEY);
                    }
                }
            });
        }
        pending.addAll(keys);
    }

    private Set<SourceKey> keysFor(Object entity) {
        Set<SourceKey> keys = new LinkedHashSet<>();
        if (entity instanceof Product product && product.getId() != null) {
            keys.add(new SourceKey(SearchSourceType.PRODUCT, product.getId()));
        } else if (entity instanceof KnowledgeArticle article && article.getId() != null) {
            keys.add(new SourceKey(SearchSourceType.KNOWLEDGE, article.getId()));
        } else if (entity instanceof SupportTicket ticket && ticket.getId() != null) {
            keys.add(new SourceKey(SearchSourceType.TICKET, ticket.getId()));
        } else if (entity instanceof Platform platform && platform.getId() != null) {
            // A platform's name is part of its products' keywords.
            keys.add(new SourceKey(null, platform.getId()));
        }
        return keys;
    }

    private void apply(Set<SourceKey> keys) {
        try {
            SearchIndexService service = indexService.getObject();
            Set<SourceKey> resolved = new LinkedHashSet<>();
            for (SourceKey key : keys) {
                if (key.type() == null) {
                    resolved.addAll(service.productsOfPlatform(key.id()));
                } else {
                    resolved.add(key);
                }
            }
            List<Long> toEmbed = service.reindex(resolved);
            if (!toEmbed.isEmpty()) {
                SemanticIndexService semantic = semanticIndexService.getObject();
                embedExecutor.submit(() -> toEmbed.forEach(id -> {
                    try {
                        semantic.embedDocument(id);
                    } catch (RuntimeException e) {
                        log.warn("Could not embed search document {}: {}", id, e.getMessage());
                    }
                }));
            }
        } catch (RuntimeException e) {
            log.warn("Search re-indexing failed: {}", e.getMessage());
        }
    }
}
