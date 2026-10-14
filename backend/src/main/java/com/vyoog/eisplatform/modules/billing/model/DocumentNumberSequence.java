package com.vyoog.eisplatform.modules.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/** BI-2: the next number of a series in a financial year. Read with a write lock so two invoices never get one number. */
@Entity
@Table(name = "document_number_sequence")
@IdClass(DocumentNumberSequence.Key.class)
@Getter
@Setter
public class DocumentNumberSequence {

    @Id
    @Column(length = 10)
    private String series;

    @Id
    @Column(name = "financial_year", length = 7)
    private String financialYear;

    @Column(name = "next_value", nullable = false)
    private long nextValue = 1;

    public static class Key implements Serializable {
        private String series;
        private String financialYear;

        public Key() {
        }

        public Key(String series, String financialYear) {
            this.series = series;
            this.financialYear = financialYear;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && Objects.equals(series, k.series) && Objects.equals(financialYear, k.financialYear);
        }

        @Override
        public int hashCode() {
            return Objects.hash(series, financialYear);
        }
    }
}
