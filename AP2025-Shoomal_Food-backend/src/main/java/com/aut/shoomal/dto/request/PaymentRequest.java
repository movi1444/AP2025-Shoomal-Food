package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentRequest
{
    @JsonProperty("order_id")
    private Integer orderId;
    private String method;
    public PaymentRequest() {}

    public PaymentRequest(Integer orderId, String method)
    {
        this.orderId = orderId;
        this.method = method;
    }

    public Integer getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Integer orderId)
    {
        this.orderId = orderId;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }
}