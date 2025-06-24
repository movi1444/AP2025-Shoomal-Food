package com.aut.shoomal.dto.request;

public class PaymentRequest
{
    private String orderId;
    private String method;
    public PaymentRequest() {}

    public PaymentRequest(String orderId, String method)
    {
        this.orderId = orderId;
        this.method = method;
    }

    public String getOrderId()
    {
        return orderId;
    }

    public void setOrderId(String orderId)
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