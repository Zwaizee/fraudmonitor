package com.exam.fraudmonitorapp.service.constant;

public class TransactionalConstant{

    // ---------- Constants ----------
    public static final java.math.BigDecimal DEFAULT_THRESHOLD = new java.math.BigDecimal("10000"); // fallback category threshold
    public static final java.math.BigDecimal GEO_HIGH_AMOUNT = new java.math.BigDecimal("3000");    // foreign high amount threshold
    public static final java.math.BigDecimal ODD_HOURS_HIGH_AMOUNT = new java.math.BigDecimal("2000");
    public static final java.math.BigDecimal NEW_DEVICE_HIGH_AMOUNT = new java.math.BigDecimal("5000");
    public static final int ODD_HOURS_START = 0;     // inclusive
    public static final int ODD_HOURS_END = 5;       // exclusive
    public static final int VELOCITY_WINDOW_MINUTES = 1;
    public static final int VELOCITY_COUNT_THRESHOLD = 5;
}