package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.repository.AuditLogRepository;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.dto.GatewayDtos;
import com.vyoog.eisplatform.modules.billing.model.PaymentGatewayConfig;
import com.vyoog.eisplatform.modules.billing.model.PaymentStatus;
import com.vyoog.eisplatform.modules.billing.repository.PaymentGatewayConfigRepository;
import com.vyoog.eisplatform.modules.billing.repository.PaymentRepository;
import com.vyoog.eisplatform.modules.billing.repository.PaymentWebhookEventRepository;
import com.vyoog.eisplatform.modules.billing.service.gateway.ConfigField;
import com.vyoog.eisplatform.modules.billing.service.gateway.ConnectionResult;
import com.vyoog.eisplatform.modules.billing.service.gateway.ConnectionStatus;
import com.vyoog.eisplatform.modules.billing.service.gateway.GatewayActor;
import com.vyoog.eisplatform.modules.billing.service.gateway.GatewayContext;
import com.vyoog.eisplatform.modules.billing.service.gateway.GatewayCredentialStore;
import com.vyoog.eisplatform.modules.billing.service.gateway.GatewayDescriptor;
import com.vyoog.eisplatform.modules.billing.service.gateway.GatewayRegistry;
import com.vyoog.eisplatform.modules.billing.service.gateway.PaymentGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * C98 (REQ-BIL-003): the Payment Gateways page's backend. Everything is derived from the provider registry and the {@code payment_gateway_config}
 * table; nothing is Razorpay-specific. Configured, connected (the last test of the current configuration), active and default are four separate
 * facts. Secrets are write-only: no method here returns one.
 */
@Service
public class GatewayManagementService {

    private static final int MAX_FIELD_LENGTH = 500;

    private final GatewayRegistry registry;
    private final PaymentGatewayConfigRepository configRepository;
    private final GatewayCredentialStore credentialStore;
    private final PaymentRepository paymentRepository;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;
    private final TransactionTemplate tx;
    private final TransactionTemplate newTransaction;
    private final String backendUrl;

    public GatewayManagementService(GatewayRegistry registry, PaymentGatewayConfigRepository configRepository, GatewayCredentialStore credentialStore,
                                    PaymentRepository paymentRepository, PaymentWebhookEventRepository webhookEventRepository,
                                    AuditLogRepository auditLogRepository, AuditService auditService, PlatformTransactionManager txManager,
                                    @Value("${app.backend-url}") String backendUrl) {
        this.registry = registry;
        this.configRepository = configRepository;
        this.credentialStore = credentialStore;
        this.paymentRepository = paymentRepository;
        this.webhookEventRepository = webhookEventRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
        this.tx = new TransactionTemplate(txManager);
        this.newTransaction = new TransactionTemplate(txManager);
        this.newTransaction.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.backendUrl = backendUrl;
    }

    // ------------------------------------------------------------------ reads

    public List<GatewayDtos.Gateway> list() {
        return registry.descriptors().stream().map(d -> toGateway(d, configOf(d))).toList();
    }

    public GatewayDtos.Summary summary() {
        List<GatewayDtos.Gateway> all = list();
        int implemented = (int) all.stream().filter(g -> "IMPLEMENTED".equals(g.implementation())).count();
        int configured = (int) all.stream().filter(g -> "CONFIGURED".equals(g.configurationStatus())).count();
        int active = (int) all.stream().filter(GatewayDtos.Gateway::activated).count();
        int needs = (int) all.stream().filter(g -> "IMPLEMENTED".equals(g.implementation()) && !"CONFIGURED".equals(g.configurationStatus())).count();
        return new GatewayDtos.Summary(all.size(), implemented, configured, active, needs);
    }

    @Transactional(readOnly = true)
    public GatewayDtos.Detail detail(String code) {
        GatewayDescriptor d = descriptor(code);
        PaymentGatewayConfig config = configOf(d);
        Map<String, String> resolved = resolved(d, config);
        Map<String, String> stored = credentialStore.decrypt(config);
        PaymentGateway adapter = registry.adapter(d.code()).orElse(null);
        List<GatewayDtos.Field> fields = d.fields().stream().map(f -> {
            String value = resolved.get(f.key());
            boolean set = value != null && !value.isBlank();
            String source = !set ? "NONE" : stored.containsKey(f.key()) && !stored.get(f.key()).isBlank() ? "STORED" : "ENVIRONMENT";
            return new GatewayDtos.Field(f.key(), f.label(), f.secret(), f.required(), f.webhook(), f.help(), set, source,
                set && !f.secret() && adapter != null ? adapter.mask(f.key(), value) : null);
        }).toList();
        GatewayDtos.Gateway gateway = toGateway(d, config);
        boolean webhookSecretSet = d.fields().stream().filter(ConfigField::webhook).allMatch(f -> resolved.getOrDefault(f.key(), "").length() > 0)
            && d.fields().stream().anyMatch(ConfigField::webhook);
        String url = backendUrl.replaceAll("/+$", "") + d.webhookPath();
        GatewayDtos.Webhook webhook = new GatewayDtos.Webhook(url, d.webhookSupport(), webhookSecretSet,
            !d.webhookSupport() ? "NOT_APPLICABLE" : config.getLastWebhookAt() != null ? "VERIFIED" : "NOT_SEEN",
            config.getLastWebhookAt(), config.getLastWebhookEventType(), config.getWebhookFailureCount(),
            config.getLastWebhookFailureAt(), config.getLastWebhookFailureReason(), d.webhookEvents(), !isLocal(url));
        GatewayDtos.Evidence evidence = new GatewayDtos.Evidence(
            "CONNECTION_SUCCESSFUL".equals(gateway.connectionStatus()),
            config.getLastWebhookAt() != null,
            paymentRepository.existsByProviderAndStatus(d.code(), PaymentStatus.CAPTURED),
            paymentRepository.existsByProviderAndRefundedAmountGreaterThan(d.code(), 0));
        List<GatewayDtos.WebhookEvent> events = webhookEventRepository.findTop20ByProviderOrderByReceivedAtDesc(d.code()).stream()
            .map(e -> new GatewayDtos.WebhookEvent(e.getEventType(), e.getReceivedAt(), e.getProcessedAt(), e.getPaymentId())).toList();
        List<GatewayDtos.HistoryEntry> history = auditLogRepository.findTop10ByTargetTypeAndTargetIdOrderByTimestampDesc("PaymentGateway", d.code()).stream()
            .map(a -> new GatewayDtos.HistoryEntry(a.getTimestamp(), a.getAction(), a.getActorKeycloakSub() != null ? a.getActorKeycloakSub() : a.getActorCustomerId() == null ? null : "customer " + a.getActorCustomerId(), a.getDetail())).toList();
        return new GatewayDtos.Detail(gateway, d.transactionTypes(), d.environments(), d.webhookSupport(), d.refundSupport(), d.recurringSupport(),
            fields, d.setupSteps(), webhook, evidence, config.getConfigVersion(), config.getCredentialsUpdatedAt(), credentialStore.encryptionAvailable(),
            credentialStore.unreadable(config), events, history);
    }

    // ------------------------------------------------------------------ writes

    /** Stores credentials and environment. A blank field keeps the stored value. If the gateway is active the new configuration is tested in the
     * same transaction and, when the test fails, nothing is saved. */
    public GatewayDtos.Detail saveConfiguration(GatewayActor actor, String code, GatewayDtos.SaveConfiguration request) {
        GatewayDescriptor d = requireImplemented(code);
        PaymentGateway adapter = registry.adapter(d.code()).orElseThrow();
        Map<String, String> submitted = new LinkedHashMap<>();
        if (request.fields() != null) {
            for (Map.Entry<String, String> e : request.fields().entrySet()) {
                ConfigField field = d.fields().stream().filter(f -> f.key().equals(e.getKey())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown field: " + e.getKey()));
                String value = e.getValue() == null ? "" : e.getValue().trim();
                if (value.length() > MAX_FIELD_LENGTH || value.chars().anyMatch(Character::isISOControl)) {
                    throw new IllegalArgumentException(field.label() + " is not valid.");
                }
                if (!value.isEmpty()) {
                    submitted.put(field.key(), value);
                }
            }
        }
        String environment = request.environment() == null || request.environment().isBlank() ? null : request.environment().trim().toUpperCase(Locale.ROOT);
        if (environment != null && !d.environments().contains(environment)) {
            throw new IllegalArgumentException("Environment must be one of " + String.join(", ", d.environments()) + ".");
        }
        if (!submitted.isEmpty() && !credentialStore.encryptionAvailable()) {
            throw new BillingConflictException("Credentials cannot be stored because the encryption key is not configured on the server (PAYMENT_GATEWAY_ENCRYPTION_KEY).");
        }
        String failure;
        {
            failure = tx.execute(status -> {
                PaymentGatewayConfig config = configOf(d);
                Map<String, String> merged = new LinkedHashMap<>(credentialStore.decrypt(config));
                merged.putAll(submitted);
                String newEnvironment = environment != null ? environment : config.getEnvironment();
                Map<String, String> env = new LinkedHashMap<>(adapter.environmentCredentials());
                env.putAll(merged);
                String invalid = adapter.validateConfiguration(new GatewayContext(d.code(), newEnvironment, env));
                if (invalid != null) {
                    throw new IllegalArgumentException(invalid);
                }
                if (!submitted.isEmpty()) {
                    credentialStore.write(config, merged);
                    config.setCredentialsUpdatedAt(Instant.now());
                }
                config.setEnvironment(newEnvironment);
                config.setConfigVersion(config.getConfigVersion() + 1);
                config.setUpdatedAt(Instant.now());
                config.setUpdatedByCustomerId(actor.customerId());
                config = configRepository.saveAndFlush(config);
                if (config.isActivated()) {
                    ConnectionResult result = runTest(d, adapter, config);
                    applyTestResult(config, result);
                    if (!result.successful()) {
                        status.setRollbackOnly();
                        return result.status() + ": " + result.message();
                    }
                }
                return null;
            });
        }
        if (failure != null) {
            auditService.recordFailure("PAYMENT_GATEWAY_CONFIGURATION_REJECTED", actor.keycloakSub(), d.code() + ": " + failure);
            throw new BillingConflictException("Not saved: " + failure + " The previous configuration is unchanged.");
        }
        auditService.recordSuccess("PAYMENT_GATEWAY_CONFIGURED", actor.keycloakSub(), actor.customerId(), null, "PaymentGateway", d.code(), null,
            "Configuration saved (" + (submitted.isEmpty() ? "no credential changed" : "changed: " + String.join(", ", submitted.keySet()))
                + (environment != null ? "; environment " + environment : "") + ")");
        return detail(d.code());
    }

    /** A real connection test of what is stored now. Not configured or incomplete is reported without calling the provider. */
    public GatewayDtos.Connection test(GatewayActor actor, String code) {
        GatewayDescriptor d = requireImplemented(code);
        PaymentGateway adapter = registry.adapter(d.code()).orElseThrow();
        PaymentGatewayConfig config = configOf(d);
        ConnectionStatus missing = missingStatus(d, resolved(d, config));
        if (missing != null) {
            return toConnection(ConnectionResult.of(missing, missing == ConnectionStatus.NOT_CONFIGURED
                ? "No credentials are set." : "Some required fields are not set."));
        }
        ConnectionResult result = runTest(d, adapter, config);
        tx.executeWithoutResult(s -> {
            PaymentGatewayConfig fresh = configOf(d);
            applyTestResult(fresh, result);
            configRepository.save(fresh);
        });
        auditService.recordSuccess("PAYMENT_GATEWAY_TESTED", actor.keycloakSub(), actor.customerId(), null, "PaymentGateway", d.code(), null,
            "Connection test: " + result.status());
        return toConnection(result);
    }

    /** Activates after a fresh, passing connection test. Several gateways can be active; the first one activated becomes the default. */
    public GatewayDtos.Detail activate(GatewayActor actor, String code) {
        GatewayDescriptor d = requireImplemented(code);
        PaymentGateway adapter = registry.adapter(d.code()).orElseThrow();
        PaymentGatewayConfig config = configOf(d);
        ConnectionStatus missing = missingStatus(d, resolved(d, config));
        if (missing != null) {
            throw new BillingConflictException(d.name() + " is not fully configured yet.");
        }
        ConnectionResult result = runTest(d, adapter, config);
        tx.executeWithoutResult(s -> {
            PaymentGatewayConfig fresh = configOf(d);
            applyTestResult(fresh, result);
            if (result.successful()) {
                fresh.setActivated(true);
                if (configRepository.findAll().stream().noneMatch(c -> c.isDefaultGateway() && c.isActivated() && !c.getProviderCode().equals(d.code()))) {
                    fresh.setDefaultGateway(true);
                }
                fresh.setUpdatedByCustomerId(actor.customerId());
            }
            fresh.setUpdatedAt(Instant.now());
            configRepository.save(fresh);
        });
        if (!result.successful()) {
            throw new BillingConflictException(d.name() + " was not activated: " + result.message());
        }
        auditService.recordSuccess("PAYMENT_GATEWAY_ACTIVATED", actor.keycloakSub(), actor.customerId(), null, "PaymentGateway", d.code(), null, d.name() + " activated");
        return detail(d.code());
    }

    @Transactional
    public GatewayDtos.Detail deactivate(GatewayActor actor, String code) {
        GatewayDescriptor d = requireImplemented(code);
        PaymentGatewayConfig config = configOf(d);
        config.setActivated(false);
        config.setDefaultGateway(false);
        config.setUpdatedAt(Instant.now());
        config.setUpdatedByCustomerId(actor.customerId());
        configRepository.save(config);
        auditService.recordSuccess("PAYMENT_GATEWAY_DEACTIVATED", actor.keycloakSub(), actor.customerId(), null, "PaymentGateway", d.code(), null,
            d.name() + " deactivated. Payments already made with it are unchanged.");
        return detail(d.code());
    }

    @Transactional
    public GatewayDtos.Detail setDefault(GatewayActor actor, String code) {
        GatewayDescriptor d = requireImplemented(code);
        PaymentGatewayConfig config = configOf(d);
        if (!config.isActivated()) {
            throw new BillingConflictException("Only an active gateway can be the default.");
        }
        for (PaymentGatewayConfig other : configRepository.findAll()) {
            if (other.isDefaultGateway() && !other.getProviderCode().equals(d.code())) {
                other.setDefaultGateway(false);
                configRepository.save(other);
            }
        }
        config.setDefaultGateway(true);
        config.setUpdatedAt(Instant.now());
        config.setUpdatedByCustomerId(actor.customerId());
        configRepository.save(config);
        auditService.recordSuccess("PAYMENT_GATEWAY_DEFAULT_CHANGED", actor.keycloakSub(), actor.customerId(), null, "PaymentGateway", d.code(), null,
            d.name() + " is now the default gateway for new payments");
        return detail(d.code());
    }

    @Transactional
    public GatewayDtos.Detail setMethods(GatewayActor actor, String code, List<String> methods) {
        GatewayDescriptor d = requireImplemented(code);
        List<String> wanted = methods == null ? List.of() : methods.stream().map(m -> m.trim().toLowerCase(Locale.ROOT)).distinct().toList();
        if (wanted.isEmpty() || !d.methods().containsAll(wanted)) {
            throw new IllegalArgumentException("Choose at least one of the methods " + String.join(", ", d.methods()) + ".");
        }
        PaymentGatewayConfig config = configOf(d);
        config.setEnabledMethods(wanted.size() == d.methods().size() ? null : String.join(",", wanted));
        config.setUpdatedAt(Instant.now());
        config.setUpdatedByCustomerId(actor.customerId());
        configRepository.save(config);
        auditService.recordSuccess("PAYMENT_GATEWAY_METHODS_CHANGED", actor.keycloakSub(), actor.customerId(), null, "PaymentGateway", d.code(), null,
            "Payment methods: " + String.join(", ", wanted));
        return detail(d.code());
    }

    // ------------------------------------------------------------ webhook status

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordWebhookReceived(String code, String eventType) {
        registry.descriptor(code).ifPresent(d -> {
            PaymentGatewayConfig config = configOf(d);
            config.setLastWebhookAt(Instant.now());
            config.setLastWebhookEventType(eventType);
            configRepository.save(config);
        });
    }

    /** One counter and the latest reason, not a row per attempt, so an unauthenticated caller cannot grow the database. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordWebhookFailure(String code, String reason) {
        registry.descriptor(code).ifPresent(d -> {
            PaymentGatewayConfig config = configOf(d);
            config.setWebhookFailureCount(config.getWebhookFailureCount() + 1);
            config.setLastWebhookFailureAt(Instant.now());
            config.setLastWebhookFailureReason(reason == null ? null : reason.substring(0, Math.min(reason.length(), 300)));
            configRepository.save(config);
        });
    }

    // ------------------------------------------------------------ used by routing and payments

    /** The credentials in force for a provider: what the environment supplies, overridden by stored values. */
    public Map<String, String> resolvedCredentials(String code) {
        GatewayDescriptor d = descriptor(code);
        return resolved(d, configOf(d));
    }

    public GatewayContext context(String code) {
        GatewayDescriptor d = descriptor(code);
        PaymentGatewayConfig config = configOf(d);
        return new GatewayContext(d.code(), config.getEnvironment(), resolved(d, config));
    }

    /** Implemented, active and fully configured: the only state in which new payments may be routed to a gateway. */
    public boolean operational(String code) {
        Optional<GatewayDescriptor> d = registry.descriptor(code);
        if (d.isEmpty() || !d.get().implemented()) {
            return false;
        }
        PaymentGatewayConfig config = configOf(d.get());
        return config.isActivated() && missingStatus(d.get(), resolved(d.get(), config)) == null;
    }

    /** True when the provider's credentials are complete (a deactivated gateway can still verify webhooks and refund old payments). */
    public boolean configured(String code) {
        Optional<GatewayDescriptor> d = registry.descriptor(code);
        return d.isPresent() && d.get().implemented() && missingStatus(d.get(), resolved(d.get(), configOf(d.get()))) == null;
    }

    public PaymentGatewayConfig config(String code) {
        return configOf(descriptor(code));
    }

    public List<String> enabledMethods(String code) {
        GatewayDescriptor d = descriptor(code);
        PaymentGatewayConfig config = configOf(d);
        return config.getEnabledMethods() == null || config.getEnabledMethods().isBlank() ? d.methods()
            : Arrays.stream(config.getEnabledMethods().split(",")).map(String::trim).filter(d.methods()::contains).toList();
    }

    // ------------------------------------------------------------------ helpers

    /** The row for a provider, created on first use. A Razorpay row created this way is active and default, so an installation that predates this
     * screen keeps taking payments (migration V038 does the same for existing databases); every other provider starts inactive. */
    private PaymentGatewayConfig configOf(GatewayDescriptor d) {
        return configRepository.findByProviderCode(d.code()).orElseGet(() -> {
            PaymentGatewayConfig created = new PaymentGatewayConfig();
            created.setProviderCode(d.code());
            boolean legacy = d.implemented() && "RAZORPAY".equals(d.code());
            created.setActivated(legacy);
            created.setDefaultGateway(legacy);
            // Committed in its own transaction: a row created inside the caller's transaction would stay locked until the caller ends, and the
            // REQUIRES_NEW webhook-status updates below would wait for it.
            newTransaction.executeWithoutResult(s -> {
                if (configRepository.findByProviderCode(d.code()).isEmpty()) {
                    try {
                        configRepository.saveAndFlush(created);
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        // created by a concurrent request
                    }
                }
            });
            return configRepository.findByProviderCode(d.code()).orElseThrow();
        });
    }

    private Map<String, String> resolved(GatewayDescriptor d, PaymentGatewayConfig config) {
        Map<String, String> values = new LinkedHashMap<>();
        registry.adapter(d.code()).ifPresent(a -> values.putAll(a.environmentCredentials()));
        credentialStore.decrypt(config).forEach((k, v) -> {
            if (v != null && !v.isBlank()) {
                values.put(k, v);
            }
        });
        return values;
    }

    private ConnectionStatus missingStatus(GatewayDescriptor d, Map<String, String> resolved) {
        List<ConfigField> required = d.fields().stream().filter(ConfigField::required).toList();
        long set = required.stream().filter(f -> resolved.getOrDefault(f.key(), "").length() > 0).count();
        if (set == required.size()) {
            return null;
        }
        return d.fields().stream().noneMatch(f -> resolved.getOrDefault(f.key(), "").length() > 0)
            ? ConnectionStatus.NOT_CONFIGURED : ConnectionStatus.CONFIGURATION_INCOMPLETE;
    }

    private ConnectionResult runTest(GatewayDescriptor d, PaymentGateway adapter, PaymentGatewayConfig config) {
        GatewayContext ctx = new GatewayContext(d.code(), config.getEnvironment(), resolved(d, config));
        String invalid = adapter.validateConfiguration(ctx);
        if (invalid != null) {
            return ConnectionResult.of(ConnectionStatus.UNSUPPORTED_CONFIGURATION, invalid);
        }
        try {
            return adapter.testConnection(ctx);
        } catch (RuntimeException e) {
            return ConnectionResult.of(ConnectionStatus.TEST_FAILED, "The connection test failed.");
        }
    }

    private void applyTestResult(PaymentGatewayConfig config, ConnectionResult result) {
        config.setTestStatus(result.status().name());
        config.setTestMessage(result.message() == null ? null : result.message().substring(0, Math.min(result.message().length(), 300)));
        config.setTestedAt(result.testedAt());
        config.setTestedConfigVersion(config.getConfigVersion());
    }

    private GatewayDtos.Connection toConnection(ConnectionResult r) {
        return new GatewayDtos.Connection(r.status().name(), r.message(), r.testedAt(), r.successful());
    }

    private GatewayDtos.Gateway toGateway(GatewayDescriptor d, PaymentGatewayConfig config) {
        boolean implemented = d.implemented();
        Map<String, String> resolved = implemented ? resolved(d, config) : Map.of();
        ConnectionStatus missing = implemented ? missingStatus(d, resolved) : null;
        String configurationStatus = !implemented ? "NOT_AVAILABLE"
            : missing == null ? "CONFIGURED" : missing == ConnectionStatus.NOT_CONFIGURED ? "NOT_CONFIGURED" : "CONFIGURATION_INCOMPLETE";
        String connection;
        String message = null;
        Instant testedAt = null;
        if (!implemented) {
            connection = ConnectionStatus.NOT_CONFIGURED.name();
            message = "No integration exists for this provider yet.";
        } else if (missing != null) {
            connection = missing.name();
        } else if (config.getTestStatus() == null || config.getTestedConfigVersion() == null || config.getTestedConfigVersion() != config.getConfigVersion()) {
            connection = ConnectionStatus.NOT_TESTED.name();
            message = config.getTestStatus() == null ? null : "The configuration changed since the last test.";
        } else {
            connection = config.getTestStatus();
            message = config.getTestMessage();
            testedAt = config.getTestedAt();
        }
        boolean configured = implemented && missing == null;
        List<String> enabled = implemented ? enabledMethods(d.code()) : d.methods();
        GatewayDtos.Actions actions = new GatewayDtos.Actions(implemented, configured, configured && !config.isActivated(),
            implemented && config.isActivated(), config.isActivated() && !config.isDefaultGateway());
        return new GatewayDtos.Gateway(d.code(), d.name(), d.description(), d.logo(), d.implementation().name(), d.methods(), enabled, d.currencies(),
            d.countries(), implemented ? config.getEnvironment() : null, configurationStatus, connection, message, testedAt,
            implemented && config.isActivated(), implemented && config.isDefaultGateway(), actions);
    }

    private GatewayDescriptor descriptor(String code) {
        return registry.descriptor(code).orElseThrow(() -> new ResourceNotFoundException("Payment gateway not found"));
    }

    private GatewayDescriptor requireImplemented(String code) {
        GatewayDescriptor d = descriptor(code);
        if (!d.implemented()) {
            throw new BillingConflictException("The " + d.name() + " integration is planned and not available yet.");
        }
        return d;
    }

    private static boolean isLocal(String url) {
        try {
            String host = URI.create(url).getHost();
            return host == null || host.equals("localhost") || host.startsWith("127.") || host.equals("0.0.0.0") || host.equals("::1") || host.endsWith(".local");
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
