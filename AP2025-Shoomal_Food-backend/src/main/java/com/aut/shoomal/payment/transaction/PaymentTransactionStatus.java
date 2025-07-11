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

    public String toApiString() {
        return switch (this) {
            case COMPLETED -> "success";
            case FAILED -> "failed";
            case PENDING, REFUNDED -> this.status.toLowerCase();
        };
    }

    public static PaymentTransactionStatus fromStatusName(String statusName) {
        if (statusName != null) {
            if (statusName.equalsIgnoreCase("success")) {
                return COMPLETED;
            }
            if (statusName.equalsIgnoreCase("pending")) {
                return PENDING;
            }
            if (statusName.equalsIgnoreCase("refunded")) {
                return REFUNDED;
            }
            for (PaymentTransactionStatus s : PaymentTransactionStatus.values()) {
                if (s.status.equalsIgnoreCase(statusName)) {
                    return s;
                }
            }
        }
        throw new IllegalArgumentException("Unknown payment transaction status: " + statusName);
    }
}