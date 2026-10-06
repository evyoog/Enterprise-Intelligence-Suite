package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleRequest;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeArticleService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.search.model.SearchDocument;
import com.vyoog.eisplatform.modules.search.model.SearchIndexRun;
import com.vyoog.eisplatform.modules.search.model.SearchSourceType;
import com.vyoog.eisplatform.modules.search.model.SearchVisibility;
import com.vyoog.eisplatform.modules.search.repository.SearchDocumentRepository;
import com.vyoog.eisplatform.modules.support.dto.CreateTicketRequest;
import com.vyoog.eisplatform.modules.support.service.SupportTicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** C70: records are indexed after every committed change, whatever service
 * made it, on any database (here H2), with the right visibility. */
@SpringBootTest
@ActiveProfiles("test")
class SearchIndexListenerTest {

    @Autowired private ProductRepository productRepository;
    @Autowired private KnowledgeArticleService articleService;
    @Autowired private SupportTicketService ticketService;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private SearchDocumentRepository documentRepository;
    @Autowired private SearchIndexService indexService;
    @Autowired private TransactionTemplate transactionTemplate;

    private SearchDocument doc(SearchSourceType type, Long id) {
        return documentRepository.findBySourceTypeAndSourceId(type, id).orElse(null);
    }

    @Test
    void productChangesAreIndexedAfterCommit() {
        Product product = new Product();
        product.setName("Índice Analytics " + System.nanoTime());
        product.setPrice(BigDecimal.ONE);
        product.setStatus(ProductStatus.ACTIVE);
        Product saved = productRepository.save(product);

        SearchDocument doc = doc(SearchSourceType.PRODUCT, saved.getId());
        assertThat(doc).isNotNull();
        assertThat(doc.getVisibility()).isEqualTo(SearchVisibility.PUBLIC);
        assertThat(doc.getTitleFolded()).startsWith("indice analytics");
        assertThat(doc.getReference()).isEqualTo("#" + saved.getId());

        saved.setStatus(ProductStatus.RETIRED);
        productRepository.save(saved);
        assertThat(doc(SearchSourceType.PRODUCT, saved.getId())).isNull();
    }

    @Test
    void rolledBackChangesAreNotIndexed() {
        Long[] id = new Long[1];
        transactionTemplate.executeWithoutResult(status -> {
            Product product = new Product();
            product.setName("Rolled back " + System.nanoTime());
            product.setPrice(BigDecimal.ONE);
            id[0] = productRepository.save(product).getId();
            status.setRollbackOnly();
        });
        assertThat(doc(SearchSourceType.PRODUCT, id[0])).isNull();
    }

    @Test
    void onlyPublishedArticlesAreIndexed() {
        var article = articleService.createArticle(new KnowledgeArticleRequest("Draft " + System.nanoTime(), "Body"));
        assertThat(doc(SearchSourceType.KNOWLEDGE, article.id())).isNull();
        articleService.setPublished(article.id(), true);
        assertThat(doc(SearchSourceType.KNOWLEDGE, article.id())).isNotNull();
    }

    @Test
    void ticketsAreVisibleToTheirRequesterOnly() {
        Customer customer = new Customer();
        customer.setEmail("listener-" + System.nanoTime() + "@test.example");
        customer.setFirstName("T");
        customer.setLastName("U");
        Long customerId = customerRepository.save(customer).getId();
        var ticket = ticketService.createTicket(customerId, new CreateTicketRequest("Login issue", "Cannot sign in."));
        SearchDocument doc = doc(SearchSourceType.TICKET, ticket.id());
        assertThat(doc.getVisibility()).isEqualTo(SearchVisibility.OWNER);
        assertThat(doc.getOwnerCustomerId()).isEqualTo(customerId);
    }

    @Test
    void rebuildWorksWithoutThePostgresIndex() {
        SearchIndexRun run = indexService.rebuildAll(SearchIndexRun.Trigger.ADMIN);
        assertThat(run.getStatus()).isEqualTo(SearchIndexRun.Status.DONE);
        assertThat(run.getChunks()).isZero();
    }
}
