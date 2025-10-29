package com.soondevjomer.libraryverse.constant;

public enum PaymentStatus {
    UNPAID,        // No payment yet (COD before delivery, or pending online payment)
    PAID,          // Payment successful
    REFUNDED,      // Payment refunded
    FAILED,
    PENDING
}
