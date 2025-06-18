package com.aut.shoomal.payment;

public enum OrderStatus
{
    SUBMITTED("submitted"),
    UNPAID_AND_CANCELLED("unpaid and cancelled"),
    WAITING_VENDOR("waiting vendor"),
    CANCELLED("cancelled"),
    FINDING_COURIER("finding courier"),
    ON_THE_WAY("on the way"),
    COMPLETED("completed");

    private final String name;

    OrderStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static OrderStatus fromName(String name)
    {
        for (OrderStatus status : OrderStatus.values())
            if (status.getName().equalsIgnoreCase(name))
                return status;
        throw new IllegalArgumentException("Unknown order status: " + name);
    }
}
