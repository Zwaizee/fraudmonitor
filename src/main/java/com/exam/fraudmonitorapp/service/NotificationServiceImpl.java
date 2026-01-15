package com.exam.fraudmonitorapp.service;

import com.exam.fraudmonitorapp.model.TransactionEventEntity;
import com.exam.fraudmonitorapp.notification.NotificationChannel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.StringJoiner;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationChannel {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@fraud.local}")
    private String mailFrom;

    @Value("${twilio.accountSid:}")
    private String twilioAccountSid;

    @Value("${twilio.authToken:}")
    private String twilioAuthToken;

    @Value("${twilio.whatsapp.from:}")
    private String whatsappFrom; // e.g., whatsapp:+14155238886

    private boolean twilioInitialized = false;

    public NotificationServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void initTwilio() {
        if (twilioAccountSid != null && !twilioAccountSid.isBlank()
                && twilioAuthToken != null && !twilioAuthToken.isBlank()) {
//            Twilio.init(twilioAccountSid, twilioAuthToken);
            twilioInitialized = true;
        }
    }

    @Override
    public void notifyFraud(TransactionEventEntity tx) {
        String subject = "Fraudulent activity detected";
        String body = new StringJoiner("\n")
                .add("We detected potential fraud on your account: " + tx.getAccountId())
                .add("Transaction ID: " + tx.getId())
                .add("Amount: " + tx.getAmount() + " " + tx.getCurrency())
                .add("CategoryEnum: " + tx.getCategory())
                .add("Merchant: " + tx.getMerchant())
                .add("Reasons: " + tx.getFraudReason())
                .add("Time: " + tx.getEventTime())
                .toString();

        // Email
        sendEmail(tx.getUserEmail(), subject, body);

        // WhatsApp
        sendWhatsApp(tx.getUserPhone(), body);
    }

    private void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) return;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception ex) {
            System.err.println("Email send failed: " + ex.getMessage());
        }
    }

    private void sendWhatsApp(String toE164, String body) {
        if (toE164 == null || toE164.isBlank() || whatsappFrom == null || whatsappFrom.isBlank() || !twilioInitialized) return;
        try {
//            Message.creator(new PhoneNumber("whatsapp:" + toE164), new PhoneNumber(whatsappFrom), body).create();
        } catch (Exception ex) {
            System.err.println("WhatsApp send failed: " + ex.getMessage());
        }
    }

}
