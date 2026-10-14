package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.dto.CouponDtos;
import com.vyoog.eisplatform.modules.billing.model.Coupon;
import com.vyoog.eisplatform.modules.billing.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * C100 (PB-1 note): coupons. The platform administrator creates them (code, percent or fixed amount, expiry), can disable one before it expires and
 * delete one that was never redeemed. A customer applies one code to a cart; the discount is taken off the cart subtotal before tax. A coupon that would
 * take the cart to zero is refused. A code is checked when it is applied and again when the invoice is created, and only then counted as redeemed.
 */
@Service
@RequiredArgsConstructor
public class CouponService {

    private static final Pattern CODE = Pattern.compile("^[A-Z0-9][A-Z0-9_-]{2,39}$");

    /** The discount a coupon gives on one cart, with the coupon it came from. */
    public record Discount(Long couponId, String code, long amount) {
    }

    private final CouponRepository couponRepository;
    private final AuditService auditService;

    // ------------------------------------------------------------------ administrator

    @Transactional(readOnly = true)
    public List<CouponDtos.Coupon> list() {
        return couponRepository.findAllByOrderByCreatedAtDescIdDesc().stream().map(this::toDto).toList();
    }

    @Transactional
    public CouponDtos.Coupon create(String actorSub, Long actorCustomerId, CouponDtos.Save request) {
        String code = normalise(request.code());
        if (code == null || !CODE.matcher(code).matches()) {
            throw new IllegalArgumentException("The code must be 3 to 40 letters, digits, dashes or underscores.");
        }
        if (couponRepository.findByCode(code).isPresent()) {
            throw new BillingConflictException("A coupon with the code " + code + " already exists.");
        }
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setCreatedByCustomerId(actorCustomerId);
        apply(coupon, request);
        coupon = couponRepository.save(coupon);
        auditService.recordSuccess("COUPON_CREATED", actorSub, actorCustomerId, null, "Coupon", coupon.getId().toString(), null,
            "Coupon " + code + " created, expires " + coupon.getExpiresAt());
        return toDto(coupon);
    }

    /** Changes a coupon (everything but its code). Past redemptions are not affected. */
    @Transactional
    public CouponDtos.Coupon update(String actorSub, Long actorCustomerId, Long id, CouponDtos.Save request) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        apply(coupon, request);
        coupon.setUpdatedAt(Instant.now());
        coupon = couponRepository.save(coupon);
        auditService.recordSuccess("COUPON_UPDATED", actorSub, actorCustomerId, null, "Coupon", id.toString(), null, "Coupon " + coupon.getCode() + " updated");
        return toDto(coupon);
    }

    /** Disables a coupon before its expiry (or enables it again). */
    @Transactional
    public CouponDtos.Coupon setEnabled(String actorSub, Long actorCustomerId, Long id, boolean enabled) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        coupon.setEnabled(enabled);
        coupon.setUpdatedAt(Instant.now());
        coupon = couponRepository.save(coupon);
        auditService.recordSuccess(enabled ? "COUPON_ENABLED" : "COUPON_DISABLED", actorSub, actorCustomerId, null, "Coupon", id.toString(), null,
            "Coupon " + coupon.getCode() + (enabled ? " enabled" : " disabled"));
        return toDto(coupon);
    }

    @Transactional
    public void delete(String actorSub, Long actorCustomerId, Long id) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        if (coupon.getRedeemedCount() > 0) {
            throw new BillingConflictException("A coupon that has been redeemed cannot be deleted. Disable it instead.");
        }
        couponRepository.delete(coupon);
        auditService.recordSuccess("COUPON_DELETED", actorSub, actorCustomerId, null, "Coupon", id.toString(), null, "Coupon " + coupon.getCode() + " deleted");
    }

    private void apply(Coupon coupon, CouponDtos.Save request) {
        String description = request.description() == null ? null : request.description().trim();
        if (description != null && description.length() > 200) {
            throw new IllegalArgumentException("The description can have up to 200 characters.");
        }
        coupon.setDescription(description == null || description.isEmpty() ? null : description);
        Coupon.Type type;
        try {
            type = Coupon.Type.valueOf(request.discountType() == null ? "" : request.discountType().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Choose a percentage or a fixed amount.");
        }
        if (type == Coupon.Type.PERCENT) {
            BigDecimal percent = request.percentOff();
            if (percent == null || percent.compareTo(BigDecimal.ONE) < 0 || percent.compareTo(new BigDecimal("99")) > 0) {
                throw new IllegalArgumentException("A percentage coupon takes between 1% and 99% off.");
            }
            coupon.setPercentOff(percent.setScale(2, RoundingMode.HALF_UP));
            coupon.setAmountOff(null);
            coupon.setCurrency(null);
        } else {
            String currency = request.currency() == null ? "" : request.currency().trim().toUpperCase(Locale.ROOT);
            if (request.amountOff() == null || request.amountOff() <= 0 || !currency.matches("[A-Z]{3}")) {
                throw new IllegalArgumentException("A fixed coupon needs an amount above zero and a three-letter currency.");
            }
            coupon.setAmountOff(request.amountOff());
            coupon.setCurrency(currency);
            coupon.setPercentOff(null);
        }
        coupon.setDiscountType(type);
        if (request.expiresAt() == null) {
            throw new IllegalArgumentException("Set an expiry date and time.");
        }
        if (request.expiresAt().isBefore(Instant.now()) && coupon.getId() == null) {
            throw new IllegalArgumentException("The expiry must be in the future.");
        }
        coupon.setExpiresAt(request.expiresAt());
    }

    // ------------------------------------------------------------------ customers

    /** The discount this code gives on a cart worth {@code subtotal}; throws {@link IllegalArgumentException} with a message the customer can act on. */
    @Transactional(readOnly = true, noRollbackFor = IllegalArgumentException.class)
    public Discount quote(String code, String currency, long subtotal) {
        Coupon coupon = usable(code, currency, subtotal);
        return new Discount(coupon.getId(), coupon.getCode(), discountOf(coupon, subtotal));
    }

    /** Like {@link #quote} and counts the redemption: called when the invoice is created. */
    @Transactional
    public Discount redeem(String code, String currency, long subtotal) {
        Coupon coupon = usable(code, currency, subtotal);
        coupon.setRedeemedCount(coupon.getRedeemedCount() + 1);
        couponRepository.save(coupon);
        return new Discount(coupon.getId(), coupon.getCode(), discountOf(coupon, subtotal));
    }

    @Transactional(readOnly = true)
    public CouponDtos.Applied describe(String code, long discount) {
        Coupon coupon = couponRepository.findByCode(normalise(code)).orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        return new CouponDtos.Applied(coupon.getCode(), coupon.getDescription(), coupon.getDiscountType().name(), coupon.getPercentOff(), discount);
    }

    private Coupon usable(String code, String currency, long subtotal) {
        String normalised = normalise(code);
        Coupon coupon = normalised == null ? null : couponRepository.findByCode(normalised).orElse(null);
        if (coupon == null) {
            throw new IllegalArgumentException("This coupon code is not valid.");
        }
        if (!coupon.isEnabled()) {
            throw new IllegalArgumentException("This coupon is not active.");
        }
        if (!coupon.getExpiresAt().isAfter(Instant.now())) {
            throw new IllegalArgumentException("This coupon has expired.");
        }
        if (coupon.getDiscountType() == Coupon.Type.FIXED && !coupon.getCurrency().equalsIgnoreCase(currency)) {
            throw new IllegalArgumentException("This coupon is for " + coupon.getCurrency() + " purchases only.");
        }
        if (discountOf(coupon, subtotal) >= subtotal) {
            throw new IllegalArgumentException("This coupon is worth more than your cart. Add more to your cart to use it.");
        }
        return coupon;
    }

    private static long discountOf(Coupon coupon, long subtotal) {
        if (coupon.getDiscountType() == Coupon.Type.PERCENT) {
            return BigDecimal.valueOf(subtotal).multiply(coupon.getPercentOff()).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).longValueExact();
        }
        return coupon.getAmountOff();
    }

    /** Capital letters, no spaces; null when blank. */
    public static String normalise(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim().toUpperCase(Locale.ROOT);
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CouponDtos.Coupon toDto(Coupon c) {
        String status = !c.isEnabled() ? "DISABLED" : !c.getExpiresAt().isAfter(Instant.now()) ? "EXPIRED" : "ACTIVE";
        return new CouponDtos.Coupon(c.getId(), c.getCode(), c.getDescription(), c.getDiscountType().name(), c.getPercentOff(), c.getAmountOff(), c.getCurrency(),
            c.getExpiresAt(), c.isEnabled(), status, c.getRedeemedCount(), c.getCreatedAt());
    }
}
