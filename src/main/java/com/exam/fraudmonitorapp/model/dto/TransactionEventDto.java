package com.exam.fraudmonitorapp.model.dto;

import com.exam.fraudmonitorapp.model.enums.CategoryEnum;
import com.exam.fraudmonitorapp.model.enums.ChannelEnum;
import com.exam.fraudmonitorapp.model.enums.CurrencyEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class TransactionEventDto implements Serializable {

    private boolean fraudulent = false;
    private Long id;
    private Long transactionId;
    @NotNull
    private BigDecimal amount;
    private String country;
    private LocalDateTime timestamp;
    @NotBlank
    private String accountId;
    @NotNull
    private CurrencyEnum currency;
    @NotNull
    private CategoryEnum category;
    private ChannelEnum channel;
    private String merchant;
    private String countryCode;
    private String deviceId;
    private String userEmail;
    private String userPhone;
    private String fraudReason;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @NotNull
    public LocalDateTime eventTime;

}
