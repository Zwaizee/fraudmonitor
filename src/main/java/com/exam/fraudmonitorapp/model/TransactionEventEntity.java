package com.exam.fraudmonitorapp.model;

import com.exam.fraudmonitorapp.model.enums.CategoryEnum;
import com.exam.fraudmonitorapp.model.enums.ChannelEnum;
import com.exam.fraudmonitorapp.model.enums.CurrencyEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TransactionEventEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long transactionId;
    private String country;
    private boolean fraudulent = false;
    private String fraudReason;
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(nullable = false)
    private String accountId;
    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private CurrencyEnum currency;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private CategoryEnum category;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ChannelEnum channel;

    @Column(nullable = false)
    private String merchant;

    private String countryCode;           // ISO country code (e.g., ZA)
    private String deviceId;
    private String userEmail;             // for notifications
    private String userPhone;         // e.g., +278123456789

    @Column(nullable = false)
    private LocalDateTime eventTime;

}
