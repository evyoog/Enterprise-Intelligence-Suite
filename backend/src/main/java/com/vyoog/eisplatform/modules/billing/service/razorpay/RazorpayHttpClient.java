package com.vyoog.eisplatform.modules.billing.service.razorpay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.common.exception.RazorpayApiException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

/** Talks to the real Razorpay REST API (https://api.razorpay.com/v1) using
 * HTTP Basic auth (key id : key secret), the same authentication Razorpay's
 * own server-side SDKs use — no SDK dependency added, since these are the
 * only handful of calls this module needs (REQ-BIL-001). Only ever invoked
 * when {@link RazorpayProperties#isConfigured()} is true; the caller (the
 * billing service layer) is responsible for that check (BR-10). */
@Component
public class RazorpayHttpClient implements RazorpayClient {

    private static final String BASE_URL = "https://api.razorpay.com/v1";

    private final RazorpayProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public RazorpayHttpClient(RazorpayProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String createOrder(long amountMinorUnits, String currencyCode, String receipt) {
        try {
            Map<String, Object> requestBody = Map.of(
                "amount", amountMinorUnits,
                "currency", currencyCode,
                "receipt", receipt
            );
            HttpResponse<String> response = send("POST", "/orders", objectMapper.writeValueAsString(requestBody));
            return objectMapper.readTree(response.body()).path("id").asText();
        } catch (RazorpayApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RazorpayApiException("Could not create the payment order.", e);
        }
    }

    @Override
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        return hmacSha256Hex(orderId + "|" + paymentId, properties.getKeySecret()).equals(signature);
    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signatureHeader) {
        return hmacSha256Hex(rawBody, properties.getWebhookSecret()).equals(signatureHeader);
    }

    @Override
    public RazorpayPaymentInfo fetchPayment(String paymentId) {
        try {
            HttpResponse<String> response = send("GET", "/payments/" + paymentId, null);
            JsonNode node = objectMapper.readTree(response.body());
            return new RazorpayPaymentInfo(
                node.path("id").asText(null),
                node.path("status").asText(null),
                node.path("method").asText(null),
                node.path("card").path("network").asText(null),
                node.path("card").path("last4").asText(null),
                node.path("error_description").asText(null)
            );
        } catch (RazorpayApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RazorpayApiException("Could not fetch the payment from Razorpay.", e);
        }
    }

    @Override
    public String createRefund(String paymentId, long amountMinorUnits, String reason) {
        try {
            Map<String, Object> requestBody = Map.of(
                "amount", amountMinorUnits,
                "notes", Map.of("reason", reason)
            );
            HttpResponse<String> response = send("POST", "/payments/" + paymentId + "/refund", objectMapper.writeValueAsString(requestBody));
            return objectMapper.readTree(response.body()).path("id").asText();
        } catch (RazorpayApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RazorpayApiException("Could not process the refund at Razorpay.", e);
        }
    }

    @Override
    public void deleteToken(String providerTokenRef) {
        try {
            send("DELETE", "/tokens/" + providerTokenRef, null);
        } catch (Exception e) {
            // Best-effort (screen requirement): removing the local record still
            // succeeds even if the token was already gone at Razorpay.
        }
    }

    @Override
    public void testConnection() {
        try {
            send("GET", "/orders?count=1", null);
        } catch (RazorpayApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RazorpayApiException("Could not reach Razorpay.", e);
        }
    }

    private HttpResponse<String> send(String method, String path, String jsonBody) throws Exception {
        String credentials = Base64.getEncoder().encodeToString(
            (properties.getKeyId() + ":" + properties.getKeySecret()).getBytes(StandardCharsets.UTF_8));
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .timeout(Duration.ofSeconds(15))
            .header("Authorization", "Basic " + credentials)
            .header("Content-Type", "application/json");
        builder = switch (method) {
            case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody == null ? "" : jsonBody));
            case "DELETE" -> builder.DELETE();
            default -> builder.GET();
        };
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RazorpayApiException("Razorpay returned an error (HTTP " + response.statusCode() + ").");
        }
        return response;
    }

    private String hmacSha256Hex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Could not compute HMAC signature", e);
        }
    }
}
