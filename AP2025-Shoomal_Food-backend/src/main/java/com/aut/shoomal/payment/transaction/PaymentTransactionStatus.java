package com.aut.shoomal.payment.transaction;

public enum PaymentTransactionStatus
{
    PENDING("Pending"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    REFUNDED("Refunded");

    private final String status;
    PaymentTransactionStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public static PaymentTransactionStatus fromStatusName(String statusName) {
        for (PaymentTransactionStatus paymentTransactionStatus : PaymentTransactionStatus.values())
            if (paymentTransactionStatus.getStatus().equalsIgnoreCase(statusName))
                return paymentTransactionStatus;
        throw new IllegalArgumentException("Unknown or unsupported payment transaction API status: " + statusName);
    }
}