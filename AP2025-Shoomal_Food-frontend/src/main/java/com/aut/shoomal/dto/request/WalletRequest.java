package com.aut.shoomal.dto.request;

import java.math.BigDecimal;

public class WalletRequest
{
    private BigDecimal amount;
    public WalletRequest() {}

    public WalletRequest(BigDecimal amount)
    {
        this.amount = amount;
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