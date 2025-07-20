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
            default -> throw new IllegalArgumentException("Unsupported transaction status for API representation: " + this.name());
        };
    }

    public static PaymentTransactionStatus fromStatusName(String statusName) {
        if (statusName != null) {
            if (statusName.equalsIgnoreCase("success")) {
                return COMPLETED;
            }
            if (statusName.equalsIgnoreCase("failed")) {
                return FAILED;
            }
        }
        throw new IllegalArgumentException("Unknown or unsupported payment transaction API status: " + statusName);
    }
}