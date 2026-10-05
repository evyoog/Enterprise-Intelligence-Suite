package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleDto;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeArticleService;
import com.vyoog.eisplatform.modules.product.dto.ProductDto;
import com.vyoog.eisplatform.modules.product.service.ProductService;
import com.vyoog.eisplatform.modules.search.dto.SearchResultItemDto;
import com.vyoog.eisplatform.modules.support.dto.SupportTicketDto;
import com.vyoog.eisplatform.modules.support.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * The search that existed before C70 (01.03.01, sprint 2027.1.3): plain
 * case-insensitive "contains" matching through each module's own query —
 * products ({@code ProductService#searchProducts}), published articles
 * ({@code KnowledgeArticleService#searchPublished}) and the caller's own
 * tickets. Used for a blank query (list everything visible) and whenever the
 * PostgreSQL search index is not installed (for example the H2 test database).
 */
@Service
@RequiredArgsConstructor
public class BasicKeywordSearch {

    public record Grouped(List<SearchResultItemDto> products, List<SearchResultItemDto> knowledgeArticles,
                          List<SearchResultItemDto> tickets) {
    }

    private final ProductService productService;
    private final KnowledgeArticleService knowledgeArticleService;
    private final SupportTicketService ticketService;

    public Grouped search(String query, String type, Long customerId) {
        List<String> terms = TextFolding.tokens(TextFolding.fold(query));
        List<SearchResultItemDto> products = !matchesType(type, "PRODUCT") ? List.of()
            : productService.searchProducts(query, null, null, null, null, false).items().stream()
                .map(p -> toItem(p, terms)).toList();
        List<SearchResultItemDto> articles = !matchesType(type, "KNOWLEDGE") ? List.of()
            : knowledgeArticleService.searchPublished(query).stream().map(a -> toItem(a, terms)).toList();
        List<SearchResultItemDto> tickets = (!matchesType(type, "TICKET") || customerId == null) ? List.of()
            : ticketService.listMyTickets(customerId).stream()
                .filter(t -> matchesQuery(query, t.subject(), t.description()))
                .map(t -> toItem(t, terms)).toList();
        return new Grouped(products, articles, tickets);
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

    private SearchResultItemDto toItem(ProductDto product, List<String> terms) {
        return item("PRODUCT", product.getId(), product.getName(), product.getCategory(), terms);
    }

    private SearchResultItemDto toItem(KnowledgeArticleDto article, List<String> terms) {
        String snippet = article.body() == null ? null
            : article.body().substring(0, Math.min(article.body().length(), Highlighter.SNIPPET_LENGTH));
        return item("KNOWLEDGE", article.id(), article.title(), snippet, terms);
    }

    private SearchResultItemDto toItem(SupportTicketDto ticket, List<String> terms) {
        return item("TICKET", ticket.id(), ticket.subject(), ticket.status().name(), terms);
    }

    private SearchResultItemDto item(String type, Long id, String title, String snippet, List<String> terms) {
        return new SearchResultItemDto(type, id, title, snippet, "#" + id, "KEYWORD", 0,
            Highlighter.highlight(title, terms), Highlighter.highlight(snippet, terms));
    }
}
