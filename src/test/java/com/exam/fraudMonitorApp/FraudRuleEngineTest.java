package com.exam.fraudmonitorapp;

import com.exam.fraudmonitorapp.model.TransactionEventEntity;
import com.exam.fraudmonitorapp.model.dto.TransactionEventDto;
import com.exam.fraudmonitorapp.model.enums.CategoryEnum;
import com.exam.fraudmonitorapp.repo.TransactionRepo;
import com.exam.fraudmonitorapp.service.FraudRuleEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FraudRuleEngineTest {

    private FraudRuleEngine createEngine(TransactionRepo repo) {
        return new FraudRuleEngine(repo);
    }


    private TransactionEventDto dto(
            String accountId,
            BigDecimal amount,
            CategoryEnum category,
            String merchant,
            String countryCode,
            String deviceId,
            LocalDateTime eventTime
    ) {
        TransactionEventDto d = new TransactionEventDto();
        d.setAccountId(accountId);
        d.setAmount(amount);
        d.setCategory(category);
        d.setMerchant(merchant);
        d.setCountryCode(countryCode);
        d.setDeviceId(deviceId);
        d.setEventTime(eventTime);
        return d;
    }

    @Test
    void amountThresholdRuleFlagsHighPurchase() {
        TransactionRepo repo = Mockito.mock(TransactionRepo.class);
        when(repo.findByAccountIdAndEventTimeBetween(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        when(repo.findDistinctDeviceIdsByAccountId(Mockito.anyString()))
                .thenReturn(List.of("device-1"));

        FraudRuleEngine engine = new FraudRuleEngine(repo);

        TransactionEventDto tx = dto(
                "ACC-1",
                new BigDecimal("20000"), // > 10000 threshold for PURCHASE
                CategoryEnum.PURCHASE,
                "OK MART",
                "ZA",
                "device-1",
                LocalDateTime.now()
        );

        List<String> reasons = engine.evaluate(tx);
        assertTrue(reasons.contains("Amount exceeds category threshold"));
    }

    @Test
    void velocityRule_flagsFiveTransactionsInLastMinute() {
        TransactionRepo repo = Mockito.mock(TransactionRepo.class);

        // Build 5 events in the last minute
        LocalDateTime now = LocalDateTime.now();
        List<TransactionEventEntity> recent = List.of(
                entity("ACC-2", now.minusSeconds(10)),
                entity("ACC-2", now.minusSeconds(20)),
                entity("ACC-2", now.minusSeconds(30)),
                entity("ACC-2", now.minusSeconds(40)),
                entity("ACC-2", now.minusSeconds(50))
        );

        when(repo.findByAccountIdAndEventTimeBetween(
                Mockito.eq("ACC-2"),
                Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class))
        ).thenReturn(recent);
        when(repo.findDistinctDeviceIdsByAccountId(Mockito.anyString()))
                .thenReturn(List.of("devA"));

        FraudRuleEngine engine = new FraudRuleEngine(repo);

        TransactionEventDto tx = dto(
                "ACC-2",
                new BigDecimal("10"),
                CategoryEnum.ONLINE,
                "ShopZ",
                "ZA",
                "devA",
                now
        );

        List<String> reasons = engine.evaluate(tx);
        assertTrue(reasons.contains("High transaction velocity in last minute"));
    }

    @Test
    void geoMismatchRule_flagsForeignHighAmount() {
        TransactionRepo repo = Mockito.mock(TransactionRepo.class);
        when(repo.findByAccountIdAndEventTimeBetween(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        when(repo.findDistinctDeviceIdsByAccountId(Mockito.anyString()))
                .thenReturn(List.of("device-1"));

        FraudRuleEngine engine = new FraudRuleEngine(repo);

        TransactionEventDto tx = dto(
                "ACC-3",
                new BigDecimal("3500"), // > 3000 foreign threshold
                CategoryEnum.ONLINE,
                "OK MART",
                "US",                   // Non-ZA
                "device-1",
                LocalDateTime.now()
        );

        List<String> reasons = engine.evaluate(tx);
        assertTrue(reasons.contains("Foreign transaction with high amount"));
    }

    @Test
    void newDeviceHighAmountRule_flagsUnknownDevice() {
        TransactionRepo repo = Mockito.mock(TransactionRepo.class);
        when(repo.findByAccountIdAndEventTimeBetween(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        when(repo.findDistinctDeviceIdsByAccountId(Mockito.eq("ACC-4")))
                .thenReturn(List.of("device-known-1", "device-known-2"));

        FraudRuleEngine engine = new FraudRuleEngine(repo);

        TransactionEventDto tx = dto(
                "ACC-4",
                new BigDecimal("6000"), // > 5000
                CategoryEnum.ONLINE,
                "OK MART",
                "ZA",
                "device-new-xyz",
                LocalDateTime.now()
        );

        List<String> reasons = engine.evaluate(tx);
        assertTrue(reasons.contains("High amount from new device"));
    }

    // Helper to build minimal entities used by velocity rule
    private TransactionEventEntity entity(String accountId, LocalDateTime eventTime) {
        TransactionEventEntity e = new TransactionEventEntity();
        e.setAccountId(accountId);
        e.setEventTime(eventTime);
        return e;
    }

}
