package com.aut.shoomal.payment.transaction;

public enum TransactionStatus
{
    PENDING("Pending"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    REFUNDED("Refunded");

    private final String status;
    TransactionStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public TransactionStatus fromStatus(String status)
    {
        for (TransactionStatus t : TransactionStatus.values())
            if (t.getStatus().equalsIgnoreCase(status))
                return t;
        throw new IllegalArgumentException("Not a valid transaction status: " + status);
    }
}