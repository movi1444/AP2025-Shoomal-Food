package com.aut.shoomal.payment;

public enum TopupMethod
{
    ONLINE("online"),
    CARD("card");

    private final String name;
    TopupMethod(String method)
    {
        this.name = method;
    }
    public String getName()
    {
        return name;
    }

    public static TopupMethod fromName(String name)
    {
        for (TopupMethod t : TopupMethod.values())
            if (t.getName().equalsIgnoreCase(name))
                return t;
        throw new IllegalArgumentException("No such Top-up Method: " + name);
    }
}