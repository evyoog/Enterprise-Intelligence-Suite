package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.modules.billing.model.DocumentNumberSequence;
import com.vyoog.eisplatform.modules.billing.repository.DocumentNumberSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** C97 BI-2: document numbers like {@code EIS/26-27/000123}: the prefix from Billing settings, the Indian financial year
 * (April to March) and a six-digit number that starts again at 1 each financial year. Credit notes have their own series
 * ({@code EIS-CN/26-27/000001}). A number is never reused or changed once given (BR-3). */
@Service
@RequiredArgsConstructor
public class InvoiceNumberService {

    public static final String INVOICE_SERIES = "INV";
    public static final String CREDIT_NOTE_SERIES = "CN";
    private static final ZoneId INDIA = ZoneId.of("Asia/Kolkata");

    private final DocumentNumberSequenceRepository repository;
    private final PlatformTransactionManager transactionManager;

    /** The financial year an instant falls in, for example {@code 26-27} for 1 April 2026 to 31 March 2027 (India time). */
    public static String financialYear(Instant at) {
        ZonedDateTime t = at.atZone(INDIA);
        int start = t.getMonthValue() >= 4 ? t.getYear() : t.getYear() - 1;
        return String.format("%02d-%02d", start % 100, (start + 1) % 100);
    }

    public static String format(String series, String prefix, String financialYear, long value) {
        String p = prefix == null || prefix.isBlank() ? "EIS" : prefix;
        String head = CREDIT_NOTE_SERIES.equals(series) ? p + "-CN" : p;
        return head + "/" + financialYear + "/" + String.format("%06d", value);
    }

    /** Takes the next number of a series. Runs inside the caller's transaction; the sequence row is locked until it ends. */
    @Transactional(propagation = Propagation.MANDATORY)
    public String next(String series, String prefix, Instant at) {
        String fy = financialYear(at);
        DocumentNumberSequence sequence = repository.lockBySeriesAndYear(series, fy).orElse(null);
        if (sequence == null) {
            try {
                new TransactionTemplate(transactionManager, requiresNew()).executeWithoutResult(status -> {
                    DocumentNumberSequence created = new DocumentNumberSequence();
                    created.setSeries(series);
                    created.setFinancialYear(fy);
                    created.setNextValue(1);
                    repository.saveAndFlush(created);
                });
            } catch (DataIntegrityViolationException alreadyCreatedByAnotherRequest) {
                // fine: read it below
            }
            sequence = repository.lockBySeriesAndYear(series, fy).orElseThrow();
        }
        long value = sequence.getNextValue();
        sequence.setNextValue(value + 1);
        repository.save(sequence);
        return format(series, prefix, fy, value);
    }

    private static org.springframework.transaction.support.DefaultTransactionDefinition requiresNew() {
        var definition = new org.springframework.transaction.support.DefaultTransactionDefinition();
        definition.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return definition;
    }
}
