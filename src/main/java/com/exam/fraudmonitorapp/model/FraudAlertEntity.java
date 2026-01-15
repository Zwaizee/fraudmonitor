package com.exam.fraudmonitorapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class FraudAlertEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long transactionId;
    @Column(nullable = false)
    private String accountId;
    @Column(nullable = false)
    private String reasons; // comma-separated
    @Column(nullable = false)
    private String status;  // OPEN, CLOSED
    @Column(nullable = false)
    private LocalDateTime createdAt;



}
