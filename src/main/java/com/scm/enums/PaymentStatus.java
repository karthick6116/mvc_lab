package com.scm.enums;

/**
 * Enumeration for payment status workflow
 * Flow: PENDING -> PROCESSING -> COMPLETED/FAILED
 */
public enum PaymentStatus {
    PENDING,        // Payment initiated
    PROCESSING,     // Being processed by gateway
    COMPLETED,      // Payment successful
    FAILED,         // Payment failed
    REFUNDED        // Payment refunded (for dropped courses)
}
