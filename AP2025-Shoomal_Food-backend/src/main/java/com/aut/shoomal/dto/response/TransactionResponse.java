package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class TransactionResponse
{
    private Integer id;
    private String status;
    @JsonProperty("payment_method")
    private String paymentMethod;
    @JsonProperty("order_id")
    private Integer orderId;
    @JsonProperty("user_id")
    private Integer userId;

    public TransactionResponse() {}
    public TransactionResponse(Integer id, String status, String paymentMethod,
                               Integer orderId, Integer userId)
    {
        this.id = id;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;
        this.userId = userId;
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
}