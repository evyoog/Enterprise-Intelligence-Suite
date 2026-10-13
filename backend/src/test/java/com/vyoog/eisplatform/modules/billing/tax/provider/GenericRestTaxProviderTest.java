package com.vyoog.eisplatform.modules.billing.tax.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.vyoog.eisplatform.modules.billing.tax.model.ProviderEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-BIL-002.30/.32/.33/.35: the generic REST adapter against a real local HTTP server (no vendor, no internet). */
class GenericRestTaxProviderTest {

    private static final String KEY = "super-secret-api-key";
    private static final String SECRET = "super-secret-signing-secret";

    private HttpServer server;
    private final Map<String, String> seenHeaders = new ConcurrentHashMap<>();
    private final AtomicInteger status = new AtomicInteger(200);
    private volatile String body = "{}";
    private volatile String lastRequestBody = "";
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getRequestHeaders().forEach((k, v) -> seenHeaders.put(k.toLowerCase(), v.get(0)));
            seenHeaders.put(":path", exchange.getRequestURI().getPath());
            seenHeaders.put(":method", exchange.getRequestMethod());
            lastRequestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status.get(), bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private GenericRestTaxProvider provider(String baseUrl, String key, String secret, boolean allowHttp) {
        return new GenericRestTaxProvider(new TaxProviderProperties(baseUrl, key, secret, allowHttp), mapper);
    }

    private GenericRestTaxProvider provider() {
        return provider("http://127.0.0.1:" + server.getAddress().getPort() + "/api/v1/", KEY, SECRET, true);
    }

    private ProviderContext context() {
        return new ProviderContext("CO1", ProviderEnvironment.SANDBOX, Duration.ofSeconds(5));
    }

    private ProviderTaxRequest request() {
        var supplier = new ProviderTaxRequest.Party("eVyoog", "1 St", "Chennai", "Tamil Nadu", "600001", "IN",
            List.of(new ProviderTaxRequest.TaxIdentifier("GST", "33ABCDE1234F1Z5", "IN", "TN")));
        var customer = new ProviderTaxRequest.Party("Acme", "2 Rd", "Bengaluru", "Karnataka", "560001", "IN", List.of());
        return new ProviderTaxRequest("INV-9-1", "SALE", "CO1", LocalDate.of(2026, 10, 13), "INR", supplier, customer,
            List.of(new ProviderTaxRequest.Line("101", "App", "SAAS", "998439", 99900, 1)));
    }

    @Test
    void capabilitiesAreReadAndTheCredentialsAreSentOnlyAsHeaders() throws Exception {
        body = "{\"provider\":\"Example Tax\",\"countries\":[\"in\",\"US\"],\"currencies\":[\"INR\",\"USD\"],\"transactionTypes\":[\"SALE\"]}";

        ProviderCapabilities capabilities = provider().testConnection(context());

        assertThat(capabilities.providerName()).isEqualTo("Example Tax");
        assertThat(capabilities.countries()).containsExactlyInAnyOrder("IN", "US");
        assertThat(capabilities.supportsCurrency("INR")).isTrue();
        assertThat(capabilities.supportsTransactionType("SALE")).isTrue();
        assertThat(seenHeaders.get(":path")).isEqualTo("/api/v1/capabilities");
        assertThat(seenHeaders.get("authorization")).isEqualTo("Bearer " + KEY);
        assertThat(seenHeaders.get("x-company-code")).isEqualTo("CO1");
        assertThat(seenHeaders.get("x-environment")).isEqualTo("SANDBOX");
    }

    @Test
    void aCapabilitiesAnswerWithoutTheListsIsInvalid() {
        body = "{\"provider\":\"x\",\"countries\":[]}";
        assertThatThrownBy(() -> provider().testConnection(context()))
            .isInstanceOfSatisfying(ProviderException.class, e -> assertThat(e.isInvalidResponse()).isTrue());
    }

    @Test
    void aCalculationIsPostedWithAnIdempotencyKeyAndASignatureAndParsed() throws Exception {
        body = "{\"status\":\"SUCCESS\",\"transactionRef\":\"INV-9-1\",\"providerReference\":\"txn_77\",\"currency\":\"INR\",\"totalTax\":17982,"
            + "\"lines\":[{\"lineRef\":\"101\",\"taxableAmount\":99900,\"taxes\":[{\"code\":\"IGST\",\"name\":\"Integrated GST\",\"taxType\":\"GST\","
            + "\"jurisdiction\":\"India\",\"rate\":18,\"amount\":17982}]}]}";

        ProviderTaxResponse response = provider().calculate(context(), request());

        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.providerReference()).isEqualTo("txn_77");
        assertThat(response.totalTax()).isEqualTo(17982L);
        assertThat(response.lines()).singleElement().satisfies(l -> {
            assertThat(l.lineRef()).isEqualTo("101");
            assertThat(l.taxableAmount()).isEqualTo(99900L);
            assertThat(l.taxes()).singleElement().satisfies(t -> {
                assertThat(t.code()).isEqualTo("IGST");
                assertThat(t.rate()).isEqualByComparingTo("18");
                assertThat(t.amount()).isEqualTo(17982L);
            });
        });
        assertThat(seenHeaders.get(":method")).isEqualTo("POST");
        assertThat(seenHeaders.get(":path")).isEqualTo("/api/v1/calculate");
        assertThat(seenHeaders.get("idempotency-key")).isEqualTo("INV-9-1");
        assertThat(seenHeaders.get("x-signature")).hasSize(64);     // hex HMAC-SHA256
        JsonNode sent = mapper.readTree(lastRequestBody);
        assertThat(sent.path("transactionRef").asText()).isEqualTo("INV-9-1");
        assertThat(sent.path("supplier").path("taxIds").get(0).path("number").asText()).isEqualTo("33ABCDE1234F1Z5");
        assertThat(sent.path("lines").get(0).path("amount").asLong()).isEqualTo(99900L);
        assertThat(lastRequestBody).doesNotContain(KEY).doesNotContain(SECRET);
    }

    @Test
    void withoutASecretThereIsNoSignatureHeader() throws Exception {
        body = "{\"status\":\"SUCCESS\"}";
        provider("http://127.0.0.1:" + server.getAddress().getPort(), KEY, "", true).calculate(context(), request());
        assertThat(seenHeaders).doesNotContainKey("x-signature");
    }

    @Test
    void decimalAmountsAreNotAcceptedAsIntegers() throws Exception {
        body = "{\"status\":\"SUCCESS\",\"totalTax\":179.82,\"lines\":[{\"lineRef\":\"101\",\"taxableAmount\":999.0,\"taxes\":[{\"code\":\"X\",\"amount\":1.5}]}]}";
        ProviderTaxResponse response = provider().calculate(context(), request());
        // The adapter reports what it could not read as null, and the engine's validation refuses null amounts.
        assertThat(response.totalTax()).isNull();
        assertThat(response.lines().get(0).taxableAmount()).isNull();
        assertThat(response.lines().get(0).taxes().get(0).amount()).isNull();
    }

    @Test
    void serverErrorsAndRateLimitsAreTransient() {
        for (int code : new int[] {500, 502, 503, 429, 408}) {
            status.set(code);
            assertThatThrownBy(() -> provider().calculate(context(), request()))
                .as("HTTP " + code).isInstanceOfSatisfying(ProviderException.class, e -> assertThat(e.isTransient()).isTrue());
        }
    }

    @Test
    void clientErrorsArePermanentAndAuthFailuresSayTheCredentialsWereRefused() {
        status.set(400);
        body = "{\"message\":\"Unknown tax class SAAS\"}";
        assertThatThrownBy(() -> provider().calculate(context(), request()))
            .isInstanceOfSatisfying(ProviderException.class, e -> {
                assertThat(e.isTransient()).isFalse();
                assertThat(e.getMessage()).contains("HTTP 400").contains("Unknown tax class SAAS");
            });
        status.set(401);
        assertThatThrownBy(() -> provider().testConnection(context()))
            .isInstanceOfSatisfying(ProviderException.class, e -> {
                assertThat(e.isTransient()).isFalse();
                assertThat(e.getMessage()).contains("refused the credentials");
            });
    }

    @Test
    void anAnswerThatIsNotJsonIsInvalid() {
        body = "<html>oops</html>";
        assertThatThrownBy(() -> provider().calculate(context(), request()))
            .isInstanceOfSatisfying(ProviderException.class, e -> assertThat(e.isInvalidResponse()).isTrue());
    }

    @Test
    void anUnreachableProviderIsATransientFailure() {
        GenericRestTaxProvider unreachable = provider("http://127.0.0.1:1", KEY, SECRET, true);
        assertThatThrownBy(() -> unreachable.calculate(context(), request()))
            .isInstanceOfSatisfying(ProviderException.class, e -> assertThat(e.isTransient()).isTrue());
    }

    @Test
    void noMessageEverContainsACredential() {
        status.set(500);
        assertThatThrownBy(() -> provider().calculate(context(), request())).satisfies(e ->
            assertThat(e.getMessage()).doesNotContain(KEY).doesNotContain(SECRET));
        GenericRestTaxProvider unreachable = provider("http://127.0.0.1:1", KEY, SECRET, true);
        assertThatThrownBy(() -> unreachable.testConnection(context())).satisfies(e ->
            assertThat(e.getMessage()).doesNotContain(KEY).doesNotContain(SECRET));
    }

    @Test
    void theProviderIsConfiguredOnlyWithAnHttpsUrlAndAKey() {
        assertThat(provider("https://tax.example.com/api", KEY, "", false).credentialsPresent()).isTrue();
        assertThat(provider("http://tax.example.com/api", KEY, "", false).credentialsPresent()).as("plain http is refused").isFalse();
        assertThat(provider("https://tax.example.com/api", "", "", false).credentialsPresent()).as("no key").isFalse();
        assertThat(provider("", KEY, "", false).credentialsPresent()).as("no url").isFalse();
        assertThat(provider("not a url", KEY, "", false).credentialsPresent()).isFalse();
        assertThatThrownBy(() -> provider("", KEY, "", false).testConnection(context())).isInstanceOf(ProviderException.class);
    }

    @Test
    void theStatusShowsOnlyWhetherEachValueIsSetAndTheHost() {
        GenericRestTaxProvider configured = provider("https://tax.example.com/api/v1", KEY, SECRET, false);
        assertThat(configured.credentialStatus()).containsEntry("TAX_PROVIDER_BASE_URL", true).containsEntry("TAX_PROVIDER_API_KEY", true)
            .containsEntry("TAX_PROVIDER_API_SECRET", true);
        assertThat(configured.maskedEndpoint()).isEqualTo("tax.example.com");
        assertThat(configured.credentialStatus().toString()).doesNotContain(KEY).doesNotContain(SECRET);
        assertThat(provider("", "", "", false).maskedEndpoint()).isNull();
    }
}
