package com.aut.shoomal.payment.coupon;

public enum CouponType
{
    FIXED("fixed"),
    PERCENT("percent");
    private final String name;
    CouponType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static CouponType fromName(String name)
    {
        for (CouponType couponType : CouponType.values())
            if (couponType.getName().equalsIgnoreCase(name))
                return couponType;
        throw new IllegalArgumentException("No enum constant with name '" + name + "'");
    }
}