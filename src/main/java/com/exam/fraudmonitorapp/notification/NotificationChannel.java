package com.exam.fraudmonitorapp.notification;

import com.exam.fraudmonitorapp.model.TransactionEventEntity;

public interface NotificationChannel {
    void notifyFraud(TransactionEventEntity transaction);
}
