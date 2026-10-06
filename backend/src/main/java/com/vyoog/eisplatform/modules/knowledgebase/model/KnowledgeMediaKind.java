package com.vyoog.eisplatform.modules.knowledgebase.model;

/** REQ-KNW-003 media kinds; each has its own allowed types, size limit and S3 prefix. */
public enum KnowledgeMediaKind {
    IMAGE,
    DOCUMENT,
    AUDIO,
    TEMPLATE,
    VIDEO_FILE,
    THUMBNAIL,
    TRANSCRIPT,
    SUBTITLE
}
