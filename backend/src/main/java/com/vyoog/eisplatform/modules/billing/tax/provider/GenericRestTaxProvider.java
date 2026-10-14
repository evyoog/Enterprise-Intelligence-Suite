package com.vyoog.eisplatform.modules.billing.tax.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** REQ-BIL-002.30: the first adapter. It speaks "External provider contract v1" (docs/06-api/api-requirements/tax-rules.md)
 * over HTTPS with a bearer API key and, when a secret is set, an HMAC-SHA256 signature of the body. A vendor whose API differs
 * gets its own {@link TaxProvider}; nothing else in billing changes. Performs exactly one attempt per call. */
@Component
public class GenericRestTaxProvider implements TaxProvider {

    public static final String CODE = "GENERIC_REST";
    private static final int MAX_COPY = 20_000;

    private final TaxProviderProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public GenericRestTaxProvider(TaxProviderProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public String displayName() {
        return "Generic REST tax provider (contract v1)";
    }

    @Override
    public boolean credentialsPresent() {
        return properties.isConfigured();
    }

    @Override
    public Map<String, Boolean> credentialStatus() {
        Map<String, Boolean> status = new LinkedHashMap<>();
        status.put("TAX_PROVIDER_BASE_URL", properties.hasBaseUrl());
        status.put("TAX_PROVIDER_API_KEY", properties.hasApiKey());
        status.put("TAX_PROVIDER_API_SECRET", properties.hasApiSecret());
        return status;
    }

    @Override
    public String maskedEndpoint() {
        return properties.masked();
    }

    @Override
    public ProviderCapabilities testConnection(ProviderContext context) throws ProviderException {
        requireConfigured();
        HttpResponse<String> response = send("GET", "/capabilities", null, context, null);
        JsonNode node = parse(response.body());
        Set<String> countries = upperSet(node.path("countries"));
        Set<String> currencies = upperSet(node.path("currencies"));
        Set<String> types = upperSet(node.path("transactionTypes"));
        if (countries.isEmpty() || currencies.isEmpty() || types.isEmpty()) {
            throw ProviderException.invalidResponse("The provider's capabilities answer must list countries, currencies and transactionTypes.", null);
        }
        return new ProviderCapabilities(node.path("provider").asText(displayName()), countries, currencies, types);
    }

    @Override
    public ProviderTaxResponse calculate(ProviderContext context, ProviderTaxRequest request) throws ProviderException {
        requireConfigured();
        String body;
        try {
            body = objectMapper.writeValueAsString(toJson(request));
        } catch (IOException e) {
            throw ProviderException.permanent("The transaction could not be written as JSON.");
        }
        HttpResponse<String> response = send("POST", "/calculate", body, context, request.transactionRef());
        JsonNode node = parse(response.body());
        List<ProviderTaxResponse.LineResult> lines = new ArrayList<>();
        for (JsonNode line : node.path("lines")) {
            List<ProviderTaxResponse.Tax> taxes = new ArrayList<>();
            for (JsonNode tax : line.path("taxes")) {
                taxes.add(new ProviderTaxResponse.Tax(text(tax, "code"), text(tax, "name"), text(tax, "taxType"), text(tax, "jurisdiction"),
                    decimal(tax.path("rate")), integer(tax.path("amount"))));
            }
            lines.add(new ProviderTaxResponse.LineResult(text(line, "lineRef"), integer(line.path("taxableAmount")), taxes));
        }
        String copy = response.body().length() > MAX_COPY ? response.body().substring(0, MAX_COPY) : response.body();
        return new ProviderTaxResponse(text(node, "status"), text(node, "transactionRef"), text(node, "providerReference"),
            text(node, "currency"), integer(node.path("totalTax")), lines, copy);
    }

    private void requireConfigured() throws ProviderException {
        if (!properties.isConfigured()) {
            throw ProviderException.permanent("The external tax provider is not configured on the backend.");
        }
    }

    private HttpResponse<String> send(String method, String path, String body, ProviderContext context, String idempotencyKey) throws ProviderException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(properties.baseUrl() + path))
            .timeout(context.timeout())
            .header("Accept", "application/json")
            .header("Authorization", "Bearer " + properties.apiKey());
        if (context.companyCode() != null) {
            builder.header("X-Company-Code", context.companyCode());
        }
        if (context.environment() != null) {
            builder.header("X-Environment", context.environment().name());
        }
        if ("POST".equals(method)) {
            builder.header("Content-Type", "application/json");
            if (idempotencyKey != null) {
                builder.header("Idempotency-Key", idempotencyKey);
            }
            if (properties.hasApiSecret()) {
                builder.header("X-Signature", hmacHex(body, properties.apiSecret()));
            }
            builder.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        } else {
            builder.GET();
        }
        HttpResponse<String> response;
        try {
            response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpTimeoutException e) {
            throw ProviderException.transientFailure("The provider did not answer in time.", e);
        } catch (IOException e) {
            throw ProviderException.transientFailure("The provider could not be reached.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ProviderException.transientFailure("The provider call was interrupted.", e);
        }
        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return response;
        }
        if (status == 408 || status == 429 || status >= 500) {
            throw ProviderException.transientFailure("The provider answered HTTP " + status + ".", null);
        }
        if (status == 401 || status == 403) {
            throw ProviderException.permanent("The provider refused the credentials (HTTP " + status + ").");
        }
        throw ProviderException.permanent("The provider rejected the request (HTTP " + status + ")" + messageFrom(response.body()) + ".");
    }

    private String messageFrom(String body) {
        try {
            String message = objectMapper.readTree(body).path("message").asText("");
            if (!message.isBlank()) {
                String clean = message.replaceAll("[\\p{Cntrl}]", " ").trim();
                return ": " + (clean.length() > 200 ? clean.substring(0, 200) : clean);
            }
        } catch (IOException ignored) {
            // not JSON: report only the status
        }
        return "";
    }

    private JsonNode parse(String body) throws ProviderException {
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node == null || !node.isObject()) {
                throw ProviderException.invalidResponse("The provider's answer is not a JSON object.", null);
            }
            return node;
        } catch (IOException e) {
            throw ProviderException.invalidResponse("The provider's answer is not valid JSON.", e);
        }
    }

    private Map<String, Object> toJson(ProviderTaxRequest r) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("transactionRef", r.transactionRef());
        root.put("transactionType", r.transactionType());
        root.put("companyCode", r.companyCode());
        root.put("date", r.date().toString());
        root.put("currency", r.currency());
        root.put("supplier", party(r.supplier()));
        root.put("customer", party(r.customer()));
        List<Map<String, Object>> lines = new ArrayList<>();
        for (ProviderTaxRequest.Line l : r.lines()) {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("lineRef", l.lineRef());
            line.put("description", l.description());
            line.put("taxClass", l.taxClass());
            line.put("hsnSac", l.hsnSac());
            line.put("amount", l.amount());
            line.put("quantity", l.quantity());
            lines.add(line);
        }
        root.put("lines", lines);
        return root;
    }

    private Map<String, Object> party(ProviderTaxRequest.Party p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", p.name());
        m.put("addressLine1", p.addressLine1());
        m.put("city", p.city());
        m.put("state", p.state());
        m.put("postalCode", p.postalCode());
        m.put("country", p.country());
        List<Map<String, Object>> ids = new ArrayList<>();
        for (ProviderTaxRequest.TaxIdentifier id : p.taxIds()) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("type", id.type());
            e.put("number", id.number());
            e.put("country", id.country());
            e.put("state", id.state());
            ids.add(e);
        }
        m.put("taxIds", ids);
        return m;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() ? value.asText() : null;
    }

    private static Long integer(JsonNode node) {
        return node.isIntegralNumber() ? node.asLong() : null;
    }

    private static BigDecimal decimal(JsonNode node) {
        return node.isNumber() ? node.decimalValue() : null;
    }

    private static Set<String> upperSet(JsonNode array) {
        Set<String> out = new LinkedHashSet<>();
        for (JsonNode item : array) {
            if (item.isTextual() && !item.asText().isBlank()) {
                out.add(item.asText().trim().toUpperCase(Locale.ROOT));
            }
        }
        return out;
    }

    private static String hmacHex(String body, String secret) throws ProviderException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException e) {
            throw ProviderException.permanent("The request could not be signed.");
        }
    }
}
