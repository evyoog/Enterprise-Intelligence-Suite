package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleRequest;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeArticleService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.search.dto.GlobalSearchResultDto;
import com.vyoog.eisplatform.modules.support.dto.CreateTicketRequest;
import com.vyoog.eisplatform.modules.support.service.SupportTicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** 01.03.01 Unified Search (sprint 2027.1.3) — keyword only, no semantic search. */
@SpringBootTest
@ActiveProfiles("test")
class GlobalSearchServiceTest {

    @Autowired
    private GlobalSearchService globalSearchService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private KnowledgeArticleService articleService;
    @Autowired
    private SupportTicketService ticketService;
    @Autowired
    private CustomerRepository customerRepository;

    private Product newProduct(String name) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(BigDecimal.TEN);
        product.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(product);
    }

    private Long newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("search-test-" + System.nanoTime() + "@test.example");
        customer.setFirstName("Test");
        customer.setLastName("User");
        return customerRepository.save(customer).getId();
    }

    @Test
    void findsAMatchingProduct() {
        Product product = newProduct("Zephyr Analytics " + System.nanoTime());
        GlobalSearchResultDto result = globalSearchService.search(product.getName(), null, null);
        assertThat(result.products()).anyMatch(item -> item.id().equals(product.getId()));
        assertThat(result.products()).allMatch(item -> item.type().equals("PRODUCT"));
    }

    @Test
    void findsAMatchingPublishedKnowledgeArticle() {
        String title = "Zorbing FAQ " + System.nanoTime();
        var article = articleService.createArticle(new KnowledgeArticleRequest(title, "How to zorb safely."));
        articleService.setPublished(article.id(), true);

        GlobalSearchResultDto result = globalSearchService.search(title, null, null);
        assertThat(result.knowledgeArticles()).anyMatch(item -> item.id().equals(article.id()));
    }

    @Test
    void findsTheCallersOwnMatchingTicketButNotWithoutACustomerId() {
        Long customerId = newCustomer();
        var ticket = ticketService.createTicket(customerId, new CreateTicketRequest("Zorb login issue", "Cannot sign in."));

        GlobalSearchResultDto withCustomer = globalSearchService.search("Zorb login", null, customerId);
        assertThat(withCustomer.tickets()).anyMatch(item -> item.id().equals(ticket.id()));

        GlobalSearchResultDto withoutCustomer = globalSearchService.search("Zorb login", null, null);
        assertThat(withoutCustomer.tickets()).isEmpty();
    }

    @Test
    void typeFilterNarrowsToOneCategory() {
        Product product = newProduct("Filter Test Product " + System.nanoTime());
        GlobalSearchResultDto onlyKnowledge = globalSearchService.search(product.getName(), "KNOWLEDGE", null);
        assertThat(onlyKnowledge.products()).isEmpty();

        GlobalSearchResultDto onlyProducts = globalSearchService.search(product.getName(), "PRODUCT", null);
        assertThat(onlyProducts.products()).anyMatch(item -> item.id().equals(product.getId()));
    }
}
