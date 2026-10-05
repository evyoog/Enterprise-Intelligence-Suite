package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleRequest;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeArticleService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.search.dto.GlobalSearchResultDto;
import com.vyoog.eisplatform.modules.search.dto.SearchResultItemDto;
import com.vyoog.eisplatform.modules.search.dto.SearchSuggestionDto;
import com.vyoog.eisplatform.modules.search.model.SearchIndexRun;
import com.vyoog.eisplatform.modules.search.repository.SearchDocumentRepository;
import com.vyoog.eisplatform.modules.search.support.PostgresSearchTestSupport;
import com.vyoog.eisplatform.modules.search.support.StubEmbeddingServer;
import com.vyoog.eisplatform.modules.support.dto.CreateTicketRequest;
import com.vyoog.eisplatform.modules.support.service.SupportTicketService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * C70 (REQ-PRT-002, REQ-PRT-003): the search index on real PostgreSQL
 * (full-text, pg_trgm, pgvector). Runs only when EIS_PG_TEST_URL is set —
 * see application-pgtest.yml. Embeddings come from {@link StubEmbeddingServer}.
 */
@SpringBootTest
@ActiveProfiles({"test", "pgtest"})
@EnabledIfEnvironmentVariable(named = "EIS_PG_TEST_URL", matches = ".+")
class PostgresSearchIntegrationTest {

    private static StubEmbeddingServer embeddingServer;

    @BeforeAll
    static void prepare() throws Exception {
        PostgresSearchTestSupport.prepareSchema();
    }

    @AfterAll
    static void stop() {
        if (embeddingServer != null) {
            embeddingServer.close();
        }
    }

    @DynamicPropertySource
    static void embeddingProperties(DynamicPropertyRegistry registry) throws Exception {
        if (embeddingServer == null) {
            embeddingServer = new StubEmbeddingServer();
        }
        registry.add("app.search.semantic.service-url", embeddingServer::url);
    }

    @Autowired private GlobalSearchService searchService;
    @Autowired private SearchIndexService indexService;
    @Autowired private SemanticIndexService semanticIndexService;
    @Autowired private SynonymService synonymService;
    @Autowired private SearchInsightsService insightsService;
    @Autowired private SearchDocumentRepository documentRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private KnowledgeArticleService articleService;
    @Autowired private SupportTicketService ticketService;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberRepository memberRepository;
    @Autowired private JdbcTemplate jdbc;

    @AfterEach
    void restoreEmbeddingServer() {
        embeddingServer.setAvailable(true);
        embeddingServer.setDelayMs(0);
    }

    // ---- helpers -------------------------------------------------------------

    private static String nonce() {
        StringBuilder sb = new StringBuilder();
        long n = System.nanoTime();
        while (n > 0) {
            sb.append((char) ('a' + n % 26));
            n /= 26;
        }
        return sb.toString();
    }

    private Product product(String name, String description) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(BigDecimal.TEN);
        product.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(product);
    }

    private Long article(String title, String body) {
        var article = articleService.createArticle(new KnowledgeArticleRequest(title, body));
        articleService.setPublished(article.id(), true);
        return article.id();
    }

    private Long customer() {
        Customer customer = new Customer();
        customer.setEmail("pg-search-" + System.nanoTime() + "@test.example");
        customer.setFirstName("Test");
        customer.setLastName("User");
        return customerRepository.save(customer).getId();
    }

    private Long organizationMember(String name) {
        Organization org = new Organization();
        org.setName(name);
        org.setCode("ORG" + System.nanoTime());
        org.setBusinessEmail("org-" + System.nanoTime() + "@test.example");
        org.setCountry("India");
        org = organizationRepository.save(org);
        Long customerId = customer();
        OrganizationMember member = new OrganizationMember();
        member.setOrganizationId(org.getId());
        member.setCustomerId(customerId);
        memberRepository.save(member);
        return customerId;
    }

    private void finishIndexing() {
        semanticIndexService.embedPending(Integer.MAX_VALUE);
        indexService.refreshTerms();
    }

    private GlobalSearchResultDto search(String q) {
        return searchService.search(q, null, null, GlobalSearchService.Mode.KEYWORD, null, false);
    }

    private static SearchResultItemDto first(GlobalSearchResultDto result) {
        assertThat(result.results()).isNotEmpty();
        return result.results().get(0);
    }

    // ---- keyword ---------------------------------------------------------------

    @Test
    void usesTheIndexEngine() {
        assertThat(search("anything").engine()).isEqualTo("POSTGRES");
    }

    @Test
    void matchesEnglishWordForms() {
        String n = nonce();
        Long id = article("Paying " + n + " invoices online", "You can pay every invoice by card or UPI.");
        GlobalSearchResultDto result = search("invoice pays " + n);
        assertThat(result.knowledgeArticles()).anyMatch(r -> r.id().equals(id));
    }

    @Test
    void matchesSpanishWordFormsAndIgnoresAccents() {
        String n = nonce();
        Long id = article("Cómo pagar las facturas " + n, "La facturación mensual se paga con tarjeta.");
        assertThat(search("factura pagos " + n).knowledgeArticles()).anyMatch(r -> r.id().equals(id));
        assertThat(search("facturacion " + n).knowledgeArticles()).anyMatch(r -> r.id().equals(id));
        assertThat(search("cómo " + n).knowledgeArticles()).anyMatch(r -> r.id().equals(id));
    }

    @Test
    void exactPhraseRanksAboveScatteredWords() {
        String n = nonce();
        Long scattered = article("Sign in help " + n, "Use a single account. Then sign on the form. " + n);
        Long phrase = article("Single sign on " + n, "Set up single sign on for your organization. " + n);
        GlobalSearchResultDto result = search("single sign on " + n);
        SearchResultItemDto top = first(result);
        assertThat(top.id()).isEqualTo(phrase);
        assertThat(top.matchType()).isEqualTo("EXACT_PHRASE");
        assertThat(result.results()).anyMatch(r -> r.id().equals(scattered));
    }

    @Test
    void quotedPhraseFindsOnlyThePhrase() {
        String n = nonce();
        article("Billing " + n + " cycle", "Your billing cycle starts on the first day.");
        Long phrase = article("Cycle of billing " + n, "We bill each cycle. The cycle billing " + n + " report.");
        GlobalSearchResultDto result = search("\"cycle billing " + n + "\"");
        assertThat(result.results()).extracting(SearchResultItemDto::id).containsExactly(phrase);
    }

    @Test
    void findsPartialWords() {
        String n = nonce();
        Product p = product("Quantivex" + n + " Analytics", "Dashboards for operations.");
        SearchResultItemDto top = first(search("quantivex" + n.substring(0, 2)));
        assertThat(top.id()).isEqualTo(p.getId());
        assertThat(top.matchType()).isEqualTo("PARTIAL");
    }

    @Test
    void correctsTyposAndSuggestsTheCorrection() {
        String n = nonce();
        Long id = article("Shipping notes " + n, "Our zephyrine connector syncs orders every hour.");
        finishIndexing();
        GlobalSearchResultDto result = search("zephirine connector");
        assertThat(result.didYouMean()).isEqualTo("zephyrine connector");
        assertThat(result.results()).anyMatch(r -> r.id().equals(id) && r.matchType().equals("TYPO"));
    }

    @Test
    void typoInATitleMatchesWithoutCorrection() {
        String n = nonce();
        Product p = product("Marvelinda" + n + " Payroll", "Payroll for teams.");
        assertThat(search("marvelenda" + n + " payroll").results()).anyMatch(r -> r.id().equals(p.getId()));
    }

    @Test
    void exactIdComesFirst() {
        Product p = product("Exact id product " + nonce(), "Description.");
        SearchResultItemDto top = first(search("#" + p.getId()));
        assertThat(top.matchType()).isEqualTo("EXACT_ID");
        assertThat(top.reference()).isEqualTo("#" + p.getId());
    }

    @Test
    void highlightsMatchingWords() {
        String n = nonce();
        Long id = article("Exporting reports " + n, "Export any report as CSV.");
        SearchResultItemDto item = search("export " + n).results().stream().filter(r -> r.id().equals(id))
            .findFirst().orElseThrow();
        assertThat(item.titleHighlights()).isNotEmpty();
        var h = item.titleHighlights().get(0);
        assertThat(item.title().substring(h.start(), h.start() + h.length())).isEqualToIgnoringCase("Exporting");
        assertThat(item.snippetHighlights()).isNotEmpty();
    }

    @Test
    void typeFilterAndUnknownType() {
        String n = nonce();
        Product p = product("Filterable " + n, "x");
        assertThat(searchService.search("filterable " + n, "KNOWLEDGE", null).results()).isEmpty();
        assertThat(searchService.search("filterable " + n, "product", null).products())
            .anyMatch(r -> r.id().equals(p.getId()));
        assertThat(searchService.search("filterable " + n, "INVOICE", null).results()).isEmpty();
    }

    // ---- visibility -----------------------------------------------------------------

    @Test
    void oneOrganizationNeverSeesAnothersRecords() {
        String n = nonce();
        Long memberA = organizationMember("Alpha " + n);
        Long memberB = organizationMember("Beta " + n);
        var ticketA = ticketService.createTicket(memberA, new CreateTicketRequest("Gateway outage " + n, "Alpha's gateway fails."));
        var ticketB = ticketService.createTicket(memberB, new CreateTicketRequest("Gateway outage " + n, "Beta's gateway fails."));
        finishIndexing();

        for (GlobalSearchService.Mode mode : GlobalSearchService.Mode.values()) {
            GlobalSearchResultDto a = searchService.search("gateway outage " + n, null, memberA, mode, null, false);
            assertThat(a.tickets()).extracting(SearchResultItemDto::id).containsExactly(ticketA.id());
            GlobalSearchResultDto b = searchService.search("gateway outage " + n, null, memberB, mode, null, false);
            assertThat(b.tickets()).extracting(SearchResultItemDto::id).containsExactly(ticketB.id());
            GlobalSearchResultDto anonymous = searchService.search("gateway outage " + n, null, null, mode, null, false);
            assertThat(anonymous.tickets()).isEmpty();
            // An exact ID does not reveal another organization's ticket either.
            assertThat(searchService.search("#" + ticketB.id(), "TICKET", memberA, mode, null, false).results()).isEmpty();
        }
        assertThat(searchService.suggest("gateway outage " + n, null, memberA))
            .extracting(SearchSuggestionDto::id).containsExactly(ticketA.id());
    }

    @Test
    void didYouMeanNeverUsesPrivateTicketWords() {
        String n = nonce();
        Long owner = customer();
        Long other = customer();
        ticketService.createTicket(owner, new CreateTicketRequest("Note " + n, "The quilibrator module is broken."));
        finishIndexing();
        GlobalSearchResultDto result = searchService.search("quilibrater", null, other, GlobalSearchService.Mode.KEYWORD, null, false);
        assertThat(result.didYouMean()).isNull();
        assertThat(result.results()).isEmpty();
        Integer private_ = jdbc.queryForObject("SELECT count(*) FROM search_term WHERE term = 'quilibrator'", Integer.class);
        assertThat(private_).isZero();
    }

    @Test
    void privateRecordsAreNeverEmbedded() {
        Long owner = customer();
        ticketService.createTicket(owner, new CreateTicketRequest("Embedding check " + nonce(), "Private text."));
        finishIndexing();
        Integer chunks = jdbc.queryForObject("SELECT count(*) FROM search_chunk c JOIN search_document d "
            + "ON d.id = c.document_id WHERE d.visibility <> 'PUBLIC'", Integer.class);
        assertThat(chunks).isZero();
    }

    // ---- automatic re-indexing ---------------------------------------------------------

    @Test
    void reindexesWhenContentChanges() {
        String oldName = "Oldname" + nonce();
        String newName = "Newname" + nonce();
        Product p = product(oldName, "Product description.");
        assertThat(search(oldName).products()).anyMatch(r -> r.id().equals(p.getId()));

        Product loaded = productRepository.findById(p.getId()).orElseThrow();
        loaded.setName(newName);
        productRepository.save(loaded);
        assertThat(search(newName).products()).anyMatch(r -> r.id().equals(p.getId()));
        assertThat(search(oldName).products()).noneMatch(r -> r.id().equals(p.getId()));

        loaded = productRepository.findById(p.getId()).orElseThrow();
        loaded.setStatus(ProductStatus.RETIRED);
        productRepository.save(loaded);
        assertThat(search(newName).products()).noneMatch(r -> r.id().equals(p.getId()));
    }

    @Test
    void unpublishedAndDeletedArticlesLeaveTheIndex() {
        String n = nonce();
        Long id = article("Temporary article " + n, "Body " + n);
        assertThat(search(n).knowledgeArticles()).anyMatch(r -> r.id().equals(id));
        articleService.setPublished(id, false);
        assertThat(search(n).knowledgeArticles()).noneMatch(r -> r.id().equals(id));
        articleService.setPublished(id, true);
        assertThat(search(n).knowledgeArticles()).anyMatch(r -> r.id().equals(id));
        articleService.deleteArticle(id);
        assertThat(search(n).knowledgeArticles()).noneMatch(r -> r.id().equals(id));
    }

    @Test
    void rebuildRestoresTheIndex() {
        String n = nonce();
        Long id = article("Rebuild check " + n, "Body.");
        jdbc.update("DELETE FROM search_document WHERE source_type = 'KNOWLEDGE' AND source_id = ?", id);
        assertThat(search("rebuild check " + n).results()).isEmpty();
        SearchIndexRun run = indexService.rebuildAll(SearchIndexRun.Trigger.ADMIN);
        assertThat(run.getStatus()).isEqualTo(SearchIndexRun.Status.DONE);
        assertThat(search("rebuild check " + n).knowledgeArticles()).anyMatch(r -> r.id().equals(id));
        assertThat(documentRepository.count()).isPositive();
    }

    // ---- synonyms ------------------------------------------------------------------

    @Test
    void synonymsFindEachOther() {
        String n = nonce();
        Long id = article("Download your " + n + " receipt", "Receipts are emailed.");
        assertThat(search("voucher" + n.substring(0, 1) + " " + n).results()).isEmpty();
        var group = synonymService.create(List.of("Receipt", "voucher" + n.substring(0, 1)));
        try {
            assertThat(search("voucher" + n.substring(0, 1) + " " + n).knowledgeArticles()).anyMatch(r -> r.id().equals(id));
        } finally {
            synonymService.delete(group.id());
        }
    }

    // ---- semantic -------------------------------------------------------------------

    @Test
    void hybridSearchAddsSemanticMatches() {
        String n = nonce();
        Long id = article("Renewal " + n + " guide", "Subscriptions renew automatically at the end of each term.");
        finishIndexing();
        GlobalSearchResultDto result = searchService.search("subscriptions renew automatically " + n + " qqq",
            null, null, GlobalSearchService.Mode.HYBRID, null, false);
        assertThat(result.semanticStatus()).isEqualTo("USED");
        assertThat(result.results()).anyMatch(r -> r.id().equals(id) && r.matchType().equals("SEMANTIC"));
    }

    @Test
    void fallsBackToKeywordWhenTheModelIsDown() {
        String n = nonce();
        Long id = article("Fallback " + n, "Body.");
        finishIndexing();
        embeddingServer.setAvailable(false);
        GlobalSearchResultDto down = searchService.search("fallback " + n, null, null, GlobalSearchService.Mode.HYBRID, null, false);
        assertThat(down.semanticStatus()).isEqualTo("UNAVAILABLE");
        assertThat(down.results()).anyMatch(r -> r.id().equals(id));

        embeddingServer.setAvailable(true);
        embeddingServer.setDelayMs(1500);
        GlobalSearchResultDto slow = searchService.search("fallback " + n, null, null, GlobalSearchService.Mode.HYBRID, null, false);
        assertThat(slow.semanticStatus()).isEqualTo("UNAVAILABLE");
        assertThat(slow.results()).anyMatch(r -> r.id().equals(id));
    }

    @Test
    void keywordModeAndTicketTypeSkipSemantic() {
        assertThat(searchService.search("anything", null, null, GlobalSearchService.Mode.KEYWORD, null, false)
            .semanticStatus()).isEqualTo("NOT_APPLICABLE");
        assertThat(searchService.search("anything", "TICKET", customer(), GlobalSearchService.Mode.HYBRID, null, false)
            .semanticStatus()).isEqualTo("NOT_APPLICABLE");
    }

    // ---- insights ------------------------------------------------------------------

    @Test
    void trackedSearchesAppearInInsights() {
        String n = nonce();
        long before = insightsService.insights(1).zeroResultSearches();
        searchService.search("nothingmatches" + n, null, null, GlobalSearchService.Mode.KEYWORD, null, true);
        var insights = insightsService.insights(1);
        assertThat(insights.zeroResultSearches()).isEqualTo(before + 1);
        assertThat(insights.topZeroResultQueries()).anyMatch(q -> q.query().equals("nothingmatches" + n));
    }
}
