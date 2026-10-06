package com.vyoog.eisplatform.modules.knowledgebase.dto;

import java.time.Instant;

/** Publish now or schedule (REQ-KNW-002.5/.6). versionBump: MINOR (default) or MAJOR. */
public record KnowledgePublishRequest(String versionBump, Instant scheduleAt) {
}
