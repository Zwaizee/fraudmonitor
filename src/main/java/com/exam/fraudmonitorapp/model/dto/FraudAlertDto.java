package com.exam.fraudmonitorapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FraudAlertDto {
    private Long id;
    private Long transactionId;
    private String accountId;
    private String reasons; // comma-separated
    private String status;  // OPEN, CLOSED
    private LocalDateTime createdAt;
}
