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

    public PaymentTransactionStatus fromStatus(String status)
    {
        for (PaymentTransactionStatus t : PaymentTransactionStatus.values())
            if (t.getStatus().equalsIgnoreCase(status))
                return t;
        throw new IllegalArgumentException("Not a valid transaction status: " + status);
    }
}