package com.vyoog.eisplatform.modules.toolsync.tracking;

/**
 * An aggregate the platform synchronizes to the tools (REQ-INT-003): an organization, a hierarchy node, a person, a membership, a
 * subscription or a product access. {@code syncVersion} only goes up: {@link ToolSyncListener} adds one in the transaction of every
 * change, and the platform ↔ tool contract carries it as the message's {@code version} (contract v1 §8).
 */
public interface ToolSyncAggregate {

    Long getId();

    long getSyncVersion();

    void setSyncVersion(long syncVersion);

    /**
     * The version the row had when it was loaded or last written in this session (not persisted). A detached copy that is saved again
     * carries an old {@code syncVersion}; the next version is always built from this value, so a version never goes back.
     */
    Long getLoadedSyncVersion();

    void setLoadedSyncVersion(Long loadedSyncVersion);
}
