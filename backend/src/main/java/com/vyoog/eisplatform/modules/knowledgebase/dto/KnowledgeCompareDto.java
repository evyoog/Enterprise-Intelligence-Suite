package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/** Block-by-block comparison of two versions ("draft" = the working copy). */
public record KnowledgeCompareDto(String from, String to, boolean titleChanged, String fromTitle, String toTitle,
                                  List<Change> changes) {

    /** kind: SAME, CHANGED, ADDED, REMOVED. */
    public record Change(int index, String kind, JsonNode before, JsonNode after) {
    }
}
