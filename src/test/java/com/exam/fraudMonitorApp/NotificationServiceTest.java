package com.exam.fraudmonitorapp;

import com.exam.fraudmonitorapp.model.TransactionEventEntity;
import com.exam.fraudmonitorapp.model.enums.CategoryEnum;
import com.exam.fraudmonitorapp.model.enums.CurrencyEnum;
import com.exam.fraudmonitorapp.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationServiceImpl service;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> mailCaptor;

    private TransactionEventEntity tx;

    @BeforeEach
    void setUp() {
        // Inject default values that would normally come from @Value
        ReflectionTestUtils.setField(service, "mailFrom", "no-reply@fraud.local");
        ReflectionTestUtils.setField(service, "twilioAccountSid", "");
        ReflectionTestUtils.setField(service, "twilioAuthToken", "");
        ReflectionTestUtils.setField(service, "whatsappFrom", ""); // not configured by default

        // Mock the TransactionEventEntity to avoid relying on its implementation
        tx = mock(TransactionEventEntity.class);
        when(tx.getId()).thenReturn(22L);
        when(tx.getAccountId()).thenReturn("ACC-123");
        when(tx.getAmount()).thenReturn(new BigDecimal("123.45"));
        when(tx.getCurrency()).thenReturn(CurrencyEnum.valueOf("ZAR"));
        when(tx.getCategory()).thenReturn(CategoryEnum.valueOf("TRANSFER"));
        when(tx.getMerchant()).thenReturn("Shoprite");
        when(tx.getFraudReason()).thenReturn("Velocity threshold exceeded");
        when(tx.getEventTime()).thenReturn(OffsetDateTime.parse("2025-12-01T10:15:30+02:00").toLocalDateTime());
        when(tx.getUserEmail()).thenReturn("user@example.com");
        when(tx.getUserPhone()).thenReturn("+27821234567");
    }

    @Test
    void notifyFraudSendsEmailWhenEmailPresent() {
        // Act
        service.notifyFraud(tx);

        // Assert: verify an email was sent and capture it
        verify(mailSender, times(1)).send(mailCaptor.capture());
        SimpleMailMessage msg = mailCaptor.getValue();

        assertThat(msg.getFrom()).isEqualTo("no-reply@fraud.local");
        assertThat(msg.getTo()).containsExactly("user@example.com");
        assertThat(msg.getSubject()).isEqualTo("Fraudulent activity detected");

        // Body should contain the important fields we join
        String body = msg.getText();

        assertThat(body).contains("123.45 ZAR");
        assertThat(body).contains("TRANSFER");
        assertThat(body).contains("Shoprite");
        assertThat(body).contains("Velocity threshold exceeded");
    }

    @Test
    void notifyFraudDoesNotSendEmailWhenEmailBlank() {
        // Arrange
        when(tx.getUserEmail()).thenReturn("  ");

        // Act
        service.notifyFraud(tx);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void initTwilioSetsInitializedTrueWhenCredsProvided() {
        // Arrange: set Twilio creds and from-number, then call lifecycle init
        ReflectionTestUtils.setField(service, "twilioAccountSid", "ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        ReflectionTestUtils.setField(service, "twilioAuthToken", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        ReflectionTestUtils.setField(service, "whatsappFrom", "whatsapp:+14155238886");

        // Act
        service.initTwilio();

        // Assert: private field 'twilioInitialized' becomes true
        boolean initialized = (boolean) ReflectionTestUtils.getField(service, "twilioInitialized");
        assertThat(initialized).isTrue();

        // Also ensure calling notifyFraud does not affect mail behavior
        service.notifyFraud(tx);
        verify(mailSender, atLeastOnce()).send(any(SimpleMailMessage.class));
    }

    @Test
    void notifyFraudSafeWhenWhatsAppDisabledOrNotInitialized() {
        // Arrange: ensure WhatsApp config is missing (default in setUp)
        // Act & Assert: should not throw; still sends email
        service.notifyFraud(tx);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmailCatchesExceptionsAndContinues() {
        // Arrange: cause JavaMailSender to throw
        doThrow(new RuntimeException("SMTP failure")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act: should not propagate exception
        service.notifyFraud(tx);

        // Assert: interaction attempted once, exception swallowed
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
