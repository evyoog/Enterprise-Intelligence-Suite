package com.vyoog.eisplatform.modules.toolsync.tracking;

import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNode;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.toolsync.service.ToolSyncPublisher;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects what changed in the current transaction (reported by {@link ToolSyncListener}) and, just BEFORE the transaction commits,
 * turns it into platform events in the same transaction ({@link ToolSyncPublisher}), so an event exists exactly when its change
 * commits (the outbox rule of REQ-INT-002). It is the one place that sees every change of the six synchronized aggregates, however
 * and wherever it is made: no service has to remember to publish.
 *
 * <p>Before the collected changes are read, the session is flushed: a change that was only made in memory runs its JPA callbacks
 * (and gets its new version) at that moment. Nothing is done for a read-only transaction or one that rolls back.
 */
@Component
public class ToolSyncChangeRecorder {

    /** What changed. {@code organizationId} is only known for a deleted hierarchy node (the row is gone when the event is built). */
    public record Change(String aggregateType, Long id, boolean deleted, long version, Long organizationId) {
    }

    private static final Object KEY = new Object();

    /**
     * The JPA callback class is created by Hibernate, not Spring, so it reaches the recorder through here: one recorder per persistence
     * unit (per Spring context: the tests keep several), found through the EntityManager that the current transaction uses.
     */
    private static final Map<Object, ToolSyncChangeRecorder> RECORDERS = new ConcurrentHashMap<>();

    @PersistenceContext
    private EntityManager entityManager;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    private final ToolSyncPublisher publisher;

    @Autowired
    public ToolSyncChangeRecorder(ToolSyncPublisher publisher) {
        this.publisher = publisher;
    }

    @PostConstruct
    void register() {
        RECORDERS.put(nativeKey(entityManagerFactory), this);
    }

    @PreDestroy
    void unregister() {
        RECORDERS.remove(nativeKey(entityManagerFactory), this);
    }

    private static Object nativeKey(EntityManagerFactory emf) {
        return emf instanceof EntityManagerFactoryInfo info ? info.getNativeEntityManagerFactory() : emf;
    }

    /** The recorder of the persistence unit whose EntityManager the current transaction has bound; null outside a transaction. */
    private static ToolSyncChangeRecorder current() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return null;
        }
        for (Object bound : TransactionSynchronizationManager.getResourceMap().values()) {
            if (bound instanceof EntityManagerHolder holder && holder.getEntityManager() != null) {
                ToolSyncChangeRecorder recorder = RECORDERS.get(holder.getEntityManager().getEntityManagerFactory());
                if (recorder != null) {
                    return recorder;
                }
            }
        }
        return null;
    }

    /** Reported by {@link ToolSyncListener}. */
    static void record(ToolSyncAggregate aggregate, boolean deleted) {
        ToolSyncChangeRecorder self = current();
        if (self == null) {
            return;
        }
        String type = typeOf(aggregate);
        if (type == null) {
            return;
        }
        Long organizationId = aggregate instanceof OrgNode n ? n.getOrganizationId() : null;
        self.track(new Change(type, aggregate.getId(), deleted, aggregate.getSyncVersion(), organizationId));
    }

    /**
     * Reported by {@link ToolSyncListener} when an aggregate is loaded or about to be inserted in a transaction. A detached entity that
     * is saved again is merged, and the merge only runs its update callbacks in the flush at commit, which is after the "before commit"
     * hook. Arming the transaction as soon as an aggregate is touched guarantees that hook runs and flushes first.
     */
    static void arm() {
        ToolSyncChangeRecorder self = current();
        if (self == null || TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            return;
        }
        self.changesOfThisTransaction();
    }

    private void track(Change change) {
        changesOfThisTransaction().put(change.aggregateType() + "#" + change.id(), change);     // the last report of an aggregate wins (highest version)
    }

    private Map<String, Change> changesOfThisTransaction() {
        @SuppressWarnings("unchecked")
        Map<String, Change> changes = (Map<String, Change>) TransactionSynchronizationManager.getResource(KEY);
        if (changes == null) {
            changes = new LinkedHashMap<>();
            TransactionSynchronizationManager.bindResource(KEY, changes);
            TransactionSynchronizationManager.registerSynchronization(new Publish());
        }
        return changes;
    }

    private final class Publish implements TransactionSynchronization {

        @Override
        public void beforeCommit(boolean readOnly) {
            @SuppressWarnings("unchecked")
            Map<String, Change> changes = (Map<String, Change>) TransactionSynchronizationManager.getResource(KEY);
            if (readOnly || changes == null) {
                return;
            }
            if (entityManager != null) {
                entityManager.flush();         // runs the callbacks of changes that were only made in memory
            }
            List<Change> collected = new ArrayList<>(changes.values());
            if (!collected.isEmpty()) {
                publisher.publish(collected);
            }
        }

        @Override
        public void afterCompletion(int status) {
            if (TransactionSynchronizationManager.hasResource(KEY)) {
                TransactionSynchronizationManager.unbindResource(KEY);
            }
        }
    }

    static String typeOf(ToolSyncAggregate a) {
        if (a instanceof Organization) return "Organization";
        if (a instanceof OrgNode) return "OrgNode";
        if (a instanceof Customer) return "Customer";
        if (a instanceof OrganizationMember) return "Member";
        if (a instanceof ProductSubscription) return "Subscription";
        if (a instanceof OrganizationProductAccess) return "Access";
        return null;
    }
}
