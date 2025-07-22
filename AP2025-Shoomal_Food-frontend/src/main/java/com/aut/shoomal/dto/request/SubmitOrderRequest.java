package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SubmitOrderRequest
{
    @JsonProperty(value = "delivery_address", required = true)
    private String deliveryAddress;
    @JsonProperty(value = "vendor_id", required = true)
    private Integer vendorId;
    @JsonProperty("coupon_id")
    private Integer couponId;
    @JsonProperty(value = "items", required = true)
    private List<OrderItemRequest> items;

    public SubmitOrderRequest() {}
    public SubmitOrderRequest(String deliveryAddress, Integer vendorId, Integer couponId,
                              List<OrderItemRequest> items)
    {
        this.deliveryAddress = deliveryAddress;
        this.vendorId = vendorId;
        this.couponId = couponId;
        this.items = items;
    }

    public String getDeliveryAddress()
    {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress)
    {
        this.deliveryAddress = deliveryAddress;
    }

    public List<OrderItemRequest> getItems()
    {
        return items;
    }

    public void setItems(List<OrderItemRequest> items)
    {
        this.items = items;
    }

    public Integer getCouponId()
    {
        return couponId;
    }

    public void setCouponId(Integer couponId)
    {
        this.couponId = couponId;
    }

    public Integer getVendorId()
    {
        return vendorId;
    }

    public void setVendorId(Integer vendorId)
    {
        this.vendorId = vendorId;
    }
}