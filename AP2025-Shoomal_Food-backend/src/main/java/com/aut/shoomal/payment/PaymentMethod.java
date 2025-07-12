package com.aut.shoomal.payment;

public enum PaymentMethod
{
    WALLET("wallet"),
    ONLINE("online");

    private final String name;
    PaymentMethod(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static PaymentMethod fromName(String name)
    {
        for (PaymentMethod paymentMethod : PaymentMethod.values())
            if (paymentMethod.getName().equalsIgnoreCase(name))
                return paymentMethod;
        throw new IllegalArgumentException("No such payment method: " + name);
    }
}