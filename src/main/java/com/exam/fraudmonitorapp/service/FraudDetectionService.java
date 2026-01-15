package com.exam.fraudmonitorapp.service;

import com.exam.fraudmonitorapp.model.FraudAlertEntity;
import com.exam.fraudmonitorapp.model.TransactionEventEntity;
import com.exam.fraudmonitorapp.model.dto.FraudAlertDto;
import com.exam.fraudmonitorapp.model.dto.TransactionEventDto;
import com.exam.fraudmonitorapp.model.mapper.FraudAlertMapper;
import com.exam.fraudmonitorapp.model.mapper.TransactionEventMapper;
import com.exam.fraudmonitorapp.repo.FraudAlertRepo;
import com.exam.fraudmonitorapp.repo.TransactionRepo;
import com.exam.fraudmonitorapp.service.exception.ConflictException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FraudDetectionService {


    private final TransactionRepo transactionRepository;
    private final FraudAlertRepo fraudAlertRepository;
    private final com.exam.fraudmonitorapp.service.FraudRuleEngine ruleEngine;
    private final com.exam.fraudmonitorapp.service.NotificationServiceImpl notificationService;
    @Autowired
    private final TransactionEventMapper eventMapper;
    @Autowired
    private final FraudAlertMapper fraudAlertMapper;

    public FraudDetectionService(TransactionRepo transactionRepository,
                                 FraudAlertRepo fraudAlertRepository,
                                 com.exam.fraudmonitorapp.service.FraudRuleEngine ruleEngine,
                                 com.exam.fraudmonitorapp.service.NotificationServiceImpl notificationService,
                                 TransactionEventMapper eventMapper,
                                 FraudAlertMapper fraudAlertMapper) {
        this.transactionRepository = transactionRepository;
        this.fraudAlertRepository = fraudAlertRepository;
        this.ruleEngine = ruleEngine;
        this.notificationService = notificationService;
        this.eventMapper = eventMapper;
        this.fraudAlertMapper = fraudAlertMapper;
    }

    /**
     * Evaluate and persist a transaction, raising alerts and notifications if fraud is detected.
     *
     * @param incomingTransaction DTO with required fields (accountId, amount, category, etc.).
     * @return persisted DTO annotated with fraud status and reasons (if any).
     */
    @Transactional
    public TransactionEventDto process(TransactionEventDto incomingTransaction) {
        // 1) Map DTO -> Entity
        TransactionEventEntity entity = eventMapper.fromDto(incomingTransaction);

        // 2) Evaluate rules on the entity (ensures uniform domain model for rules)
        List<String> reasons = ruleEngine.evaluate(eventMapper.toDto(entity));
        if (reasons == null) {
            reasons = List.of();
        }

        boolean isFraud = !reasons.isEmpty();
        entity.setFraudulent(isFraud);
        entity.setFraudReason(String.join(", ", reasons)); // empty string if none

        // 3) Persist transaction first (to get an ID for alert)
        log.info("ENTITY::: {} ",entity);
        TransactionEventEntity saved = transactionRepository.save(entity);

        // 4) If fraud, create alert + notify
        if (isFraud) {

            FraudAlertEntity alert = new FraudAlertEntity();
            alert.setTransactionId(saved.getId());             // <-- use the entity's primary key
            alert.setAccountId(saved.getAccountId());
            alert.setReasons(saved.getFraudReason());
            alert.setStatus("OPEN");
            alert.setCreatedAt(LocalDateTime.now());

            fraudAlertRepository.save(alert);

            // Notify (Email + WhatsApp); failures are logged inside NotificationServiceImpl
            notificationService.notifyFraud(saved);
        }

        // 5) Map back to DTO (includes generated id & fraud fields)
        return eventMapper.toDto(saved);
    }

    /**
     * Close an alert idempotently; if already CLOSED, returns current state.
     *
     */
    @Transactional
    public FraudAlertDto closeAlert(Long alertId) {
        FraudAlertEntity alert = fraudAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        if ("CLOSED".equalsIgnoreCase(alert.getStatus())) {
            // Idempotent: return as-is if already closed
            return fraudAlertMapper.toDto(alert);
        }

        alert.setStatus("CLOSED");
        try {
            FraudAlertEntity updated = fraudAlertRepository.save(alert);
            return fraudAlertMapper.toDto(updated);
        } catch (OptimisticLockingFailureException ex) {
            throw new ConflictException("Alert modified by another request. Please refresh and retry.", ex);
        }
    }

    /**
     * List alerts by status, most recent first.
     */
    public List<FraudAlertDto> alertList(String status) {
        return fraudAlertRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(fraudAlertMapper::toDto)
                .collect(Collectors.toList()); // returns empty list when none
    }

    /**
     * Get a transaction by ID.
     * @param id transaction id
     * @return Optional<TransactionEventDto>
     */
    public Optional<TransactionEventDto> getTransaction(Long id) {
        // map Entity -> DTO so controller receives DTO only
        return transactionRepository.findById(id).map(eventMapper::toDto);
    }

    /**
     * List transactions by account, ordered by event time descending.
     * @param accountId account id
     * @return list of TransactionEventDto
     */
    public List<TransactionEventDto> listByAccount(String accountId) {
        //map Entity -> DTO; stream + map + collect
        return transactionRepository.findByAccountIdOrderByEventTimeDesc(accountId)
                .stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

}
