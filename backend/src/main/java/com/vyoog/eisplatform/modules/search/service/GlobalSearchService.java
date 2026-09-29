package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleDto;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeArticleService;
import com.vyoog.eisplatform.modules.product.dto.ProductDto;
import com.vyoog.eisplatform.modules.product.service.ProductService;
import com.vyoog.eisplatform.modules.search.dto.GlobalSearchResultDto;
import com.vyoog.eisplatform.modules.search.dto.SearchResultItemDto;
import com.vyoog.eisplatform.modules.support.dto.SupportTicketDto;
import com.vyoog.eisplatform.modules.support.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * 01.03.01 Unified Search (sprint 2027.1.3): keyword search across the
 * public catalog, published knowledge articles, and (signed in only) the
 * caller's own support tickets. No cross-entity aggregator existed before
 * this — each of those three already had its own independent query method
 * ({@code ProductService#searchProducts}, {@code
 * KnowledgeArticleService#searchPublished}, {@code
 * SupportTicketService#listMyTickets}); this is the first place they're
 * combined into one result.
 *
 * <p>Semantic search (01.03.01.02) is deliberately NOT built — see
 * decision C41: no embeddings/vector-store infrastructure exists anywhere in
 * this codebase (same gap as 11.01.02 AI Knowledge and 04a/04b). Every
 * result below is a plain keyword match. "Filter results" is the {@code
 * type} parameter; "Sort results" is each source's own existing natural
 * order (name for products, most-recently-updated for articles, most
 * recent for tickets) — there is no cross-type relevance score to sort by.
 * "View search history" reuses the existing {@code SearchHistoryService}
 * (Phase 17) — recording happens on the frontend, exactly as the product
 * catalog search page already does, not duplicated here.
 */
@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private final ProductService productService;
    private final KnowledgeArticleService knowledgeArticleService;
    private final SupportTicketService ticketService;

    public GlobalSearchResultDto search(String query, String type, Long customerId) {
        boolean wantsProducts = matchesType(type, "PRODUCT");
        boolean wantsKnowledge = matchesType(type, "KNOWLEDGE");
        boolean wantsTickets = matchesType(type, "TICKET");

        List<SearchResultItemDto> products = !wantsProducts ? List.of()
            : productService.searchProducts(query, null, null, null, null, false).items().stream()
                .map(this::toItem)
                .toList();

        List<SearchResultItemDto> articles = !wantsKnowledge ? List.of()
            : knowledgeArticleService.searchPublished(query).stream()
                .map(this::toItem)
                .toList();

        List<SearchResultItemDto> tickets = (!wantsTickets || customerId == null) ? List.of()
            : ticketService.listMyTickets(customerId).stream()
                .filter(t -> matchesQuery(query, t.subject(), t.description()))
                .map(this::toItem)
                .toList();

        return new GlobalSearchResultDto(products, articles, tickets);
    }

    private boolean matchesType(String type, String candidate) {
        return type == null || type.isBlank() || type.equalsIgnoreCase(candidate);
    }

    private boolean matchesQuery(String query, String... fields) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String needle = query.toLowerCase(Locale.ROOT);
        for (String field : fields) {
            if (field != null && field.toLowerCase(Locale.ROOT).contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private SearchResultItemDto toItem(ProductDto product) {
        return new SearchResultItemDto("PRODUCT", product.getId(), product.getName(), product.getCategory());
    }

    private SearchResultItemDto toItem(KnowledgeArticleDto article) {
        String snippet = article.body() == null ? null
            : article.body().substring(0, Math.min(article.body().length(), 200));
        return new SearchResultItemDto("KNOWLEDGE", article.id(), article.title(), snippet);
    }

    private SearchResultItemDto toItem(SupportTicketDto ticket) {
        return new SearchResultItemDto("TICKET", ticket.id(), ticket.subject(), ticket.status().name());
    }
}
