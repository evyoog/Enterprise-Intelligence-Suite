package com.vyoog.eisplatform.modules.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/** C55 (REQ-BIL-001.21), extended by C60: the one platform-wide row of
 * billing settings — the business profile printed on invoices, invoicing
 * rules, offline bank details, the payment methods offered at checkout and
 * the Razorpay Checkout appearance. None of these are secrets: Razorpay keys
 * stay in config/secrets.env (BR-SEC-001). */
@Entity
@Table(name = "billing_settings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class BillingSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "offline_account_name", length = 200)
    private String offlineAccountName;

    @Column(name = "offline_bank_name", length = 200)
    private String offlineBankName;

    @Column(name = "offline_account_number", length = 34)
    private String offlineAccountNumber;

    @Column(name = "offline_ifsc", length = 11)
    private String offlineIfsc;

    @Column(name = "offline_swift_bic", length = 11)
    private String offlineSwiftBic;


    // ---- C60: business profile — the invoice issuer printed on every invoice ----
    @Column(name = "business_legal_name", length = 200)
    private String businessLegalName;

    @Column(name = "business_trade_name", length = 200)
    private String businessTradeName;

    @Column(name = "business_gstin", length = 15)
    private String businessGstin;

    @Column(name = "business_pan", length = 10)
    private String businessPan;

    @Column(name = "business_cin", length = 21)
    private String businessCin;

    @Column(name = "business_address_line1", length = 200)
    private String businessAddressLine1;

    @Column(name = "business_address_line2", length = 200)
    private String businessAddressLine2;

    @Column(name = "business_city", length = 100)
    private String businessCity;

    @Column(name = "business_state", length = 100)
    private String businessState;

    @Column(name = "business_postal_code", length = 20)
    private String businessPostalCode;

    @Column(name = "business_country", length = 100)
    private String businessCountry;

    @Column(name = "business_email", length = 255)
    private String businessEmail;

    @Column(name = "business_phone", length = 30)
    private String businessPhone;

    @Column(name = "business_website", length = 255)
    private String businessWebsite;

    // ---- C60: invoicing ----
    /** Prefix of new invoice numbers ({@code <prefix>-<year>-<id>}); existing numbers never change (BR-3). */
    @Column(name = "invoice_prefix", nullable = false, length = 10)
    private String invoicePrefix = "INV";

    /** Days from issue to due date for new invoices. 0 = due on issue (the C47 default). */
    @Column(name = "payment_terms_days", nullable = false)
    private int paymentTermsDays = 0;

    @Column(name = "invoice_footer_note", length = 500)
    private String invoiceFooterNote;

    // ---- C60: more offline payment details ----
    @Column(name = "offline_branch_name", length = 200)
    private String offlineBranchName;

    @Column(name = "offline_iban", length = 34)
    private String offlineIban;

    @Column(name = "offline_micr", length = 9)
    private String offlineMicr;

    @Column(name = "offline_upi_id", length = 100)
    private String offlineUpiId;

    @Column(name = "offline_cheque_payable_to", length = 200)
    private String offlineChequePayableTo;

    @Column(name = "offline_cheque_address", length = 500)
    private String offlineChequeAddress;

    @Column(name = "offline_instructions", length = 1000)
    private String offlineInstructions;

    /** CURRENT or SAVINGS. */
    @Column(name = "offline_account_type", length = 10)
    private String offlineAccountType;

    @Column(name = "offline_bank_transfer_enabled", nullable = false)
    private boolean offlineBankTransferEnabled = true;

    @Column(name = "offline_neft_rtgs_enabled", nullable = false)
    private boolean offlineNeftRtgsEnabled = true;

    @Column(name = "offline_cheque_enabled", nullable = false)
    private boolean offlineChequeEnabled = true;

    // ---- C60: payment methods offered at checkout, Razorpay Checkout appearance (not secrets) ----
    @Column(name = "method_card_enabled", nullable = false)
    private boolean methodCardEnabled = true;

    @Column(name = "method_upi_enabled", nullable = false)
    private boolean methodUpiEnabled = true;

    @Column(name = "method_netbanking_enabled", nullable = false)
    private boolean methodNetbankingEnabled = true;

    @Column(name = "method_wallet_enabled", nullable = false)
    private boolean methodWalletEnabled = true;

    @Column(name = "method_pay_by_invoice_enabled", nullable = false)
    private boolean methodPayByInvoiceEnabled = true;

    @Column(name = "checkout_display_name", length = 100)
    private String checkoutDisplayName;

    @Column(name = "checkout_description", length = 255)
    private String checkoutDescription;

    @Column(name = "checkout_theme_color", length = 7)
    private String checkoutThemeColor;

    /** REQ-SUB-004.4 (C64): platform default days before renewal (1–30). */
    @Column(name = "reminder_lead_days", nullable = false)
    private int reminderLeadDays = 7;

    /** REQ-SUB-004.5: platform default send time, HH:mm (proposed default 09:00 — confirm). */
    @Column(name = "reminder_send_time", nullable = false, length = 5)
    private String reminderSendTime = "09:00";

    /** REQ-SUB-004.7: used when a recipient has no time zone preference (proposed default — confirm). */
    @Column(name = "reminder_time_zone", nullable = false, length = 64)
    private String reminderTimeZone = "Asia/Kolkata";

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by_customer_id")
    private Long updatedByCustomerId;
}
