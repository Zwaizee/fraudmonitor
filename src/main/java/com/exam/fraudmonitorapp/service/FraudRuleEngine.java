package com.exam.fraudmonitorapp.service;

import com.exam.fraudmonitorapp.model.TransactionEventEntity;
import com.exam.fraudmonitorapp.model.dto.TransactionEventDto;
import com.exam.fraudmonitorapp.model.enums.CategoryEnum;
import com.exam.fraudmonitorapp.repo.TransactionRepo;
import com.exam.fraudmonitorapp.service.constant.TransactionalConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * Stateless fraud rules engine that evaluates a transaction DTO against configured rules.
 * - Null-safe across all accessed fields.
 * - Magic numbers extracted into constants.
 * - Avoids Map.of(null) lookups by guarding null keys.
 */

@Service
@Slf4j
public class FraudRuleEngine {


    private final TransactionRepo repo;

    private static final Set<String> BLACKLISTED_MERCHANTS = Set.of(
            "SCAM MART", "DODGY DEALS", "PHISH PAY"
    );

    private static final Map<CategoryEnum, BigDecimal> CATEGORY_THRESHOLDS = Map.of(
            CategoryEnum.PURCHASE,      new BigDecimal("10000"),
            CategoryEnum.TRANSFER,      new BigDecimal("50000"),
            CategoryEnum.ATM,           new BigDecimal("3000"),
            CategoryEnum.ONLINE,        new BigDecimal("8000"),
            CategoryEnum.INTERNATIONAL, new BigDecimal("5000"),
            CategoryEnum.BILL_PAYMENT,  new BigDecimal("20000")
    );

    public FraudRuleEngine(TransactionRepo repo) {
        this.repo = Objects.requireNonNull(repo, "TransactionRepo must not be null");
    }

    /**
     * Evaluate the given transaction and return a list of reason strings.
     * The list is empty when no rules are matched.
     */
    public List<String> evaluate(TransactionEventDto tx) {
        if (tx == null) return Collections.emptyList();

        List<String> reasons = new ArrayList<>(6);
        ruleAmountThreshold(tx, reasons);
        ruleVelocity(tx, reasons);
        ruleGeoMismatch(tx, reasons);
        ruleBlacklistMerchant(tx, reasons);
        ruleOddHours(tx, reasons);
        ruleNewDeviceHighAmount(tx, reasons);
        return reasons;
    }

    // ---------- Rules ----------
    private void ruleAmountThreshold(TransactionEventDto tx, List<String> reasons) {
        CategoryEnum category = tx.getCategory();
        BigDecimal threshold = (category == null)
                ? TransactionalConstant.DEFAULT_THRESHOLD
                : CATEGORY_THRESHOLDS.getOrDefault(category, TransactionalConstant.DEFAULT_THRESHOLD);

        BigDecimal amount = tx.getAmount();
        if (amount != null && amount.compareTo(threshold) > 0) {
            reasons.add("Amount exceeds category threshold");
        }
    }

    private void ruleVelocity(TransactionEventDto tx, List<String> reasons) {
        String accountId = tx.getAccountId();
        LocalDateTime end = tx.getEventTime();

        if (accountId == null || accountId.isBlank() || end == null) {
            return;
        }

        LocalDateTime start = end.minus(Duration.ofMinutes(TransactionalConstant.VELOCITY_WINDOW_MINUTES));
        List<TransactionEventEntity> lastMinute = repo.findByAccountIdAndEventTimeBetween(accountId, start, end);

        if (lastMinute != null && lastMinute.size() >= TransactionalConstant.VELOCITY_COUNT_THRESHOLD) {
            reasons.add("High transaction velocity in last minute");
        }
    }

    private void ruleGeoMismatch(TransactionEventDto tx, List<String> reasons) {
        String countryCode = tx.getCountryCode();
        BigDecimal amount = tx.getAmount();

        if (countryCode == null || countryCode.isBlank() || amount == null) return;

        if (!"ZA".equalsIgnoreCase(countryCode) && amount.compareTo(TransactionalConstant.GEO_HIGH_AMOUNT) > 0) {
            reasons.add("Foreign transaction with high amount");
        }
    }

    private void ruleBlacklistMerchant(TransactionEventDto tx, List<String> reasons) {
        String merchant = tx.getMerchant();
        if (merchant == null) return;

        // Use ROOT locale for predictable uppercasing
        String normalized = merchant.toUpperCase(Locale.ROOT);
        if (BLACKLISTED_MERCHANTS.contains(normalized)) {
            reasons.add("Merchant is blacklisted");
        }
    }

    private void ruleOddHours(TransactionEventDto tx, List<String> reasons) {
        LocalDateTime eventTime = tx.getEventTime();
        BigDecimal amount = tx.getAmount();
        if (eventTime == null || amount == null) return;

        int hour = eventTime.getHour(); // Local time; for strict UTC, convert to ZonedDateTime
        if (hour >= TransactionalConstant.ODD_HOURS_START && hour < TransactionalConstant.ODD_HOURS_END
                && amount.compareTo(TransactionalConstant.ODD_HOURS_HIGH_AMOUNT) > 0) {
            reasons.add("Large transaction at unusual hours");
        }
    }

    private void ruleNewDeviceHighAmount(TransactionEventDto tx, List<String> reasons) {
        String deviceId = tx.getDeviceId();
        String accountId = tx.getAccountId();
        BigDecimal amount = tx.getAmount();

        if (deviceId == null || deviceId.isBlank() || accountId == null || accountId.isBlank() || amount == null) {
            return;
        }

        List<String> knownDevices = repo.findDistinctDeviceIdsByAccountId(accountId);
        if (knownDevices == null) knownDevices = Collections.emptyList();

        // Sanitize nulls and check presence
        boolean isNewDevice = knownDevices.stream()
                .filter(Objects::nonNull)
                .noneMatch(d -> d.equals(deviceId));

        if (isNewDevice && amount.compareTo(TransactionalConstant.NEW_DEVICE_HIGH_AMOUNT) > 0) {
            reasons.add("High amount from new device");
        }
    }

}
