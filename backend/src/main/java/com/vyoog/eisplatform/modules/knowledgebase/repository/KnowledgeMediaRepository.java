package com.vyoog.eisplatform.modules.knowledgebase.repository;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMedia;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface KnowledgeMediaRepository extends JpaRepository<KnowledgeMedia, Long> {

    List<KnowledgeMedia> findByStatusInOrderByCreatedAtDesc(Collection<KnowledgeMediaStatus> statuses);

    List<KnowledgeMedia> findByStatusAndCreatedAtBefore(KnowledgeMediaStatus status, Instant before);

    List<KnowledgeMedia> findByStatus(KnowledgeMediaStatus status);
}
