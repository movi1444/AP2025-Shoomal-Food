package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class TransactionResponse
{
    private Integer id;
    private String status;
    @JsonProperty("method")
    private String paymentMethod;
    @JsonProperty("order_id")
    private Integer orderId;
    @JsonProperty("user_id")
    private Integer userId;
    @JsonProperty("transaction_date")
    private String transactionDate;
    private BigDecimal amount;

    public TransactionResponse() {}
    public TransactionResponse(Integer id, String status, String paymentMethod,
                               Integer orderId, Integer userId, String transactionDate, BigDecimal amount)
    {
        this.id = id;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;
        this.userId = userId;
        this.transactionDate = transactionDate;
        this.amount = amount;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getPaymentMethod()
    {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod)
    {
        this.paymentMethod = paymentMethod;
    }

    public Integer getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Integer orderId)
    {
        this.orderId = orderId;
    }

    public Integer getUserId()
    {
        return userId;
    }

    public void setUserId(Integer userId)
    {
        this.userId = userId;
    }

    public String getTransactionDate()
    {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate)
    {
        this.transactionDate = transactionDate;
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