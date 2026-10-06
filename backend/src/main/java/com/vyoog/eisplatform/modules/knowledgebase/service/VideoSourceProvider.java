package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeVideoDtos;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeVideo;
import com.vyoog.eisplatform.modules.knowledgebase.model.VideoSourceType;

/** One way a video can be played (C73). New sources implement this. */
public interface VideoSourceProvider {

    VideoSourceType type();

    /** Fills and checks the source fields of {@code video} from the request (BR-KVID-001). */
    void apply(KnowledgeVideo video, KnowledgeVideoDtos.VideoRequest request);

    /** Playback information for a reader already authorized to see the video. */
    KnowledgeVideoDtos.PlayInfo playInfo(KnowledgeVideo video);
}
