package com.aut.shoomal.Erfan;

public enum UserTypes
{
    ADMIN("Admin"),
    BUYER("Buyer"),
    SELLER("Seller"),
    COURIER("Courier");

    private final String name;
    UserTypes(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static UserTypes fromName(String name)
    {
        for (UserTypes type : UserTypes.values())
            if (type.getName().equalsIgnoreCase(name))
                return type;
        throw new IllegalArgumentException("No enum constant with name '" + name + "'");
    }
}