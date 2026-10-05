package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.knowledgebase.model.ArticleStatus;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.platform.model.Platform;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.search.model.SearchDocument;
import com.vyoog.eisplatform.modules.search.model.SearchSourceType;
import com.vyoog.eisplatform.modules.search.model.SearchVisibility;
import com.vyoog.eisplatform.modules.support.model.SupportTicket;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Turns a source record into its search document (C70), or empty when the
 * record must not be searchable. The visibility rules are the same as the
 * search before C70: ACTIVE products (the public catalog), PUBLISHED
 * knowledge articles, and support tickets for their requester only.
 */
final class SearchDocumentBuilder {

    private SearchDocumentBuilder() {
    }

    static Optional<SearchDocument> fromProduct(Product product) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            return Optional.empty();
        }
        String platforms = product.getPlatforms().stream().map(Platform::getName).filter(Objects::nonNull)
            .collect(Collectors.joining(" "));
        String keywords = join(product.getCategory(), product.getVariantLabel(),
            product.getFeatureTags() == null ? null : product.getFeatureTags().replace(',', ' '), platforms);
        return Optional.of(build(SearchSourceType.PRODUCT, product.getId(), SearchVisibility.PUBLIC, null,
            product.getName(), product.getDescription(), keywords, product.getUpdatedAt()));
    }

    static Optional<SearchDocument> fromArticle(KnowledgeArticle article) {
        if (article.getStatus() != ArticleStatus.PUBLISHED) {
            return Optional.empty();
        }
        return Optional.of(build(SearchSourceType.KNOWLEDGE, article.getId(), SearchVisibility.PUBLIC, null,
            article.getTitle(), article.getBody(), null, article.getUpdatedAt()));
    }

    static Optional<SearchDocument> fromTicket(SupportTicket ticket) {
        return Optional.of(build(SearchSourceType.TICKET, ticket.getId(), SearchVisibility.OWNER,
            ticket.getRequestedByCustomerId(), ticket.getSubject(), ticket.getDescription(),
            join(ticket.getCategory(), ticket.getStatus() == null ? null : ticket.getStatus().name()),
            ticket.getUpdatedAt()));
    }

    private static SearchDocument build(SearchSourceType type, Long id, SearchVisibility visibility, Long owner,
                                        String title, String body, String keywords, java.time.Instant updatedAt) {
        SearchDocument doc = new SearchDocument();
        doc.setSourceType(type);
        doc.setSourceId(id);
        doc.setVisibility(visibility);
        doc.setOwnerCustomerId(owner);
        doc.setReference("#" + id);
        String safeTitle = title == null || title.isBlank() ? "#" + id : truncate(title, 300);
        doc.setTitle(safeTitle);
        doc.setBody(body);
        doc.setKeywords(truncate(keywords, 2000));
        doc.setTitleFolded(TextFolding.searchable(safeTitle));
        doc.setBodyFolded(TextFolding.searchable(body));
        doc.setKeywordsFolded(TextFolding.searchable(doc.getKeywords()));
        doc.setContentUpdatedAt(updatedAt);
        return doc;
    }

    private static String join(String... parts) {
        String joined = Stream.of(parts).filter(p -> p != null && !p.isBlank()).collect(Collectors.joining(" "));
        return joined.isEmpty() ? null : joined;
    }

    private static String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
