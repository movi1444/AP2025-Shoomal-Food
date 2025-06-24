package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class TransactionResponse
{
    private Long id;
    private BigDecimal amount;
    private String status;
    @JsonProperty("transaction_time")
    private String transactionTime;
    @JsonProperty("payment_method")
    private String paymentMethod;
    @JsonProperty("order_id")
    private Integer orderId;
    @JsonProperty("user_id")
    private Long userId;

    public TransactionResponse() {}
    public TransactionResponse(Long id, BigDecimal amount, String status, String transactionTime, String paymentMethod,
                               Integer orderId, Long userId)
    {
        this.id = id;
        this.amount = amount;
        this.status = status;
        this.transactionTime = transactionTime;
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;
        this.userId = userId;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getTransactionTime()
    {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime)
    {
        this.transactionTime = transactionTime;
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

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }
}