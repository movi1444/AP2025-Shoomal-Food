package com.aut.shoomal.dto.request;

import java.math.BigDecimal;

public class WalletRequest
{
    private String method;
    private BigDecimal amount;
    public WalletRequest() {}

    public WalletRequest(String method, BigDecimal amount)
    {
        this.method = method;
        this.amount = amount;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }
}