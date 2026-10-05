package com.vyoog.eisplatform.modules.knowledgebase.repository;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface KnowledgeVideoRepository extends JpaRepository<KnowledgeVideo, Long> {

    Optional<KnowledgeVideo> findByContentId(Long contentId);

    List<KnowledgeVideo> findByContentIdIn(Collection<Long> contentIds);

    List<KnowledgeVideo> findByMediaIdOrThumbnailMediaId(Long mediaId, Long thumbnailMediaId);
}
