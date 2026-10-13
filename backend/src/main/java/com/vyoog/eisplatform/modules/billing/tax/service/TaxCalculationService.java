package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.modules.billing.tax.calc.BlockCode;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationRequest;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationResult;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxModeSnapshot;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxCalculation;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxCalculationStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxEngineType;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxCalculationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** REQ-BIL-002.40 to .42: the one entry point billing uses. It pins the mode once ({@link #pin}), runs the engine that mode names
 * and nothing else (BR-3), and turns every unexpected failure into a BLOCKED result so an invoice is never finalized on a guess. */
@Service
@RequiredArgsConstructor
public class TaxCalculationService {

    private static final Logger log = LoggerFactory.getLogger(TaxCalculationService.class);

    private final TaxSettingsService settingsService;
    private final InternalTaxEngine internalEngine;
    private final ExternalTaxEngine externalEngine;
    private final TaxCalculationRepository calculationRepository;

    /** BR-2: read the switch and the provider settings once. The caller keeps the snapshot to the end of its transaction. */
    public TaxModeSnapshot pin() {
        return settingsService.snapshot();
    }

    public TaxCalculationResult calculate(TaxCalculationRequest request, TaxModeSnapshot pinned) {
        try {
            if (request.lines().isEmpty()) {
                return TaxCalculationResult.blocked(pinned.engine(), pinned.configVersion(), BlockCode.ENGINE_ERROR, "There is nothing to tax.", pinned.providerCode(), 0);
            }
            return pinned.engine() == TaxEngineType.EXTERNAL ? externalEngine.calculate(request, pinned) : internalEngine.calculate(request, pinned);
        } catch (RuntimeException e) {
            log.error("Tax calculation {} failed unexpectedly", request.transactionRef(), e);
            return TaxCalculationResult.blocked(pinned.engine(), pinned.configVersion(), BlockCode.ENGINE_ERROR, null, pinned.providerCode(), 1);
        }
    }

    /** A reference that is unique per calculation attempt of an invoice. It is also the provider's idempotency key, so a retry
     * after a fix (a new attempt) is never answered with the earlier failure (BR-32). */
    public String nextTransactionRef(Long invoiceId) {
        return "INV-" + invoiceId + "-" + (calculationRepository.countByInvoiceId(invoiceId) + 1);
    }

    @Transactional(readOnly = true)
    public java.util.List<com.vyoog.eisplatform.modules.billing.tax.dto.TaxCalculationDto> listForInvoice(Long invoiceId) {
        return calculationRepository.findByInvoiceIdOrderByIdDesc(invoiceId).stream().map(c -> new com.vyoog.eisplatform.modules.billing.tax.dto.TaxCalculationDto(
            c.getId(), c.getTransactionRef(), c.getEngine().name(), c.getConfigVersion(), c.getStatus().name(), c.getReasonCode(), c.getReasonMessage(),
            c.getProviderCode(), c.getProviderReference(), c.getAttempts(), c.getTaxTotal(), c.getCalculatedAt())).toList();
    }

    /** Stores the attempt (engine, configuration version, provider reference, a copy of the validated response). */
    @Transactional
    public TaxCalculation record(Long invoiceId, TaxCalculationRequest request, TaxCalculationResult result) {
        TaxCalculation row = new TaxCalculation();
        row.setInvoiceId(invoiceId);
        row.setTransactionRef(request.transactionRef());
        row.setTransactionType(request.type());
        row.setEngine(result.engine());
        row.setConfigVersion(result.configVersion());
        row.setStatus(result.status());
        row.setReasonCode(result.blockCode() == null ? null : result.blockCode().name());
        row.setReasonMessage(result.blockMessage() == null ? null : truncate(result.blockMessage(), 500));
        row.setProviderCode(result.providerCode());
        row.setProviderReference(result.providerReference());
        row.setAttempts(Math.max(1, result.attempts()));
        row.setCurrency(request.currency());
        row.setTaxableTotal(request.taxableTotal());
        row.setTaxTotal(result.status() == TaxCalculationStatus.CALCULATED ? result.totalTax() : null);
        row.setResponseCopy(result.responseCopy());
        return calculationRepository.save(row);
    }

    private static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
