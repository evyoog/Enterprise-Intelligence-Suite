package com.vyoog.eisplatform.modules.search.repository;

/**
 * What the database offers the search module (C70).
 *
 * @param postgres      the database is PostgreSQL
 * @param keywordIndex  search_document with its tsv column and pg_trgm are installed
 * @param semantic      pgvector and the search_chunk table are installed
 */
public record SearchCapabilities(boolean postgres, boolean keywordIndex, boolean semantic) {

    public static final SearchCapabilities NONE = new SearchCapabilities(false, false, false);
}
