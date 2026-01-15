package com.exam.fraudmonitorapp.controller;


import com.exam.fraudmonitorapp.model.dto.TransactionEventDto;
import com.exam.fraudmonitorapp.service.FraudDetectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/transactions")
@Slf4j
public class TransactionMonitorController {

    private final FraudDetectionService fraudService;

    public TransactionMonitorController(FraudDetectionService fraudService) {
        this.fraudService = fraudService;
    }

    @PostMapping
    public ResponseEntity<?> consume(@Valid @RequestBody TransactionEventDto request) {

        log.info("Incoming transaction, {}", request);
        TransactionEventDto saved = fraudService.process(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransaction(@PathVariable Long id) {
        return fraudService.getTransaction(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<?> listByAccount(@RequestParam @NotBlank String accountId) {
        return fraudService.listByAccount(accountId);
    }

}
