package com.vyoog.eisplatform.modules.toolsync.tracking;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

/**
 * JPA callbacks on the six synchronized aggregates. They do two things, in the same transaction as the change, wherever in the
 * application the change is made (a service, a job, an import):
 * <ol>
 *   <li>add one to {@link ToolSyncAggregate#getSyncVersion()} before an update is written (only a changed row is updated, so only a
 *       changed aggregate gets a new version);</li>
 *   <li>tell {@link ToolSyncChangeRecorder} that the aggregate changed, so that before the transaction commits the change becomes a
 *       platform event (and from it, a message to each tool that needs it).</li>
 * </ol>
 * A new row starts at version 1 (field default and column default).
 */
public class ToolSyncListener {

    @PostLoad
    public void loaded(Object entity) {
        if (entity instanceof ToolSyncAggregate a) {
            a.setLoadedSyncVersion(a.getSyncVersion());
            ToolSyncChangeRecorder.arm();
        }
    }

    @PrePersist
    public void inserting(Object entity) {
        if (entity instanceof ToolSyncAggregate) {
            ToolSyncChangeRecorder.arm();
        }
    }

    @PreUpdate
    public void bumpVersion(Object entity) {
        if (entity instanceof ToolSyncAggregate a) {
            a.setSyncVersion(baseVersion(a) + 1);
        }
    }

    @PostPersist
    @PostUpdate
    public void changed(Object entity) {
        if (entity instanceof ToolSyncAggregate a) {
            a.setLoadedSyncVersion(a.getSyncVersion());
            ToolSyncChangeRecorder.record(a, false);
        }
    }

    @PreRemove
    public void removing(Object entity) {
        if (entity instanceof ToolSyncAggregate a) {
            a.setSyncVersion(baseVersion(a) + 1);      // the tombstone's version
            ToolSyncChangeRecorder.record(a, true);
        }
    }

    private static long baseVersion(ToolSyncAggregate a) {
        return a.getLoadedSyncVersion() != null ? Math.max(a.getLoadedSyncVersion(), 0) : a.getSyncVersion();
    }
}
