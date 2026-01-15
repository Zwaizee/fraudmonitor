package com.exam.fraudmonitorapp;


import com.exam.fraudmonitorapp.model.TransactionEventEntity;
import com.exam.fraudmonitorapp.model.dto.TransactionEventDto;
import com.exam.fraudmonitorapp.model.mapper.FraudAlertMapper;
import com.exam.fraudmonitorapp.model.mapper.TransactionEventMapper;
import com.exam.fraudmonitorapp.repo.FraudAlertRepo;
import com.exam.fraudmonitorapp.repo.TransactionRepo;
import com.exam.fraudmonitorapp.service.FraudDetectionService;
import com.exam.fraudmonitorapp.service.FraudRuleEngine;
import com.exam.fraudmonitorapp.service.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FraudDetectionServiceTest {


    @Test
    void getTransactionMapsEntityToDtoWithoutMockingMappers() {
        TransactionRepo txRepo = mock(TransactionRepo.class);
        FraudAlertRepo alertRepo = mock(FraudAlertRepo.class);
        FraudRuleEngine engine = mock(FraudRuleEngine.class);
        NotificationServiceImpl notif = mock(NotificationServiceImpl.class);

        // Real MapStruct mappers
        TransactionEventMapper eventMapper = Mappers.getMapper(TransactionEventMapper.class);
        FraudAlertMapper fraudAlertMapper   = Mappers.getMapper(FraudAlertMapper.class);

        FraudDetectionService svc = new FraudDetectionService(
                txRepo, alertRepo, engine, notif, eventMapper, fraudAlertMapper
        );

        TransactionEventEntity entity = new TransactionEventEntity();
        entity.setId(10L);
        entity.setAccountId("ACC-1");
        entity.setAmount(new BigDecimal("100.00"));
        entity.setEventTime(LocalDateTime.now());

        when(txRepo.findById(10L)).thenReturn(Optional.of(entity));

        Optional<TransactionEventDto> out = svc.getTransaction(10L);

        assertTrue(out.isPresent());
        assertNotEquals(null, out.get().getId());
        verify(txRepo).findById(10L);
    }

    @Test
    void listByAccountMapsEntitiesToDtosWithoutMockingMappers() {
        TransactionRepo txRepo = mock(TransactionRepo.class);
        FraudAlertRepo alertRepo = mock(FraudAlertRepo.class);
        FraudRuleEngine engine = mock(FraudRuleEngine.class);
        NotificationServiceImpl notif = mock(NotificationServiceImpl.class);

        TransactionEventMapper eventMapper = Mappers.getMapper(TransactionEventMapper.class);
        FraudAlertMapper fraudAlertMapper   = Mappers.getMapper(FraudAlertMapper.class);

        FraudDetectionService svc = new FraudDetectionService(
                txRepo, alertRepo, engine, notif, eventMapper, fraudAlertMapper
        );

        TransactionEventEntity e1 = new TransactionEventEntity(); e1.setId(1L);
        TransactionEventEntity e2 = new TransactionEventEntity(); e2.setId(2L);

        when(txRepo.findByAccountIdOrderByEventTimeDesc("ACC-1")).thenReturn(List.of(e1, e2));

        List<TransactionEventDto> out = svc.listByAccount("ACC-1");

        assertEquals(2, out.size());
        assertEquals(1, out.get(0).getId());
        assertNotEquals(0, out.get(0).getId());
        verify(txRepo).findByAccountIdOrderByEventTimeDesc("ACC-1");
    }


}
