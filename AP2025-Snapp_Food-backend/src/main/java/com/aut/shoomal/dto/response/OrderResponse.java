package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OrderResponse {
    private Integer id;
    @JsonProperty("delivery_address")
    private String deliveryAddress;
    @JsonProperty("customer_id")
    private Integer customerId;
    @JsonProperty("vendor_id")
    private Integer vendorId;
    @JsonProperty("courier_id")
    private Integer courierId;
    @JsonProperty("coupon_id")
    private Integer couponId;
    @JsonProperty("item_ids")
    private List<Integer> items;
    @JsonProperty("raw_price")
    private Integer rawPrice;
    @JsonProperty("tax_fee")
    private Integer taxFee;
    @JsonProperty("courier_fee")
    private Integer courierFee;
    @JsonProperty("pay_price")
    private Integer payPrice;
    private String status;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;

    public OrderResponse() {}

    public OrderResponse(Integer id, String deliveryAddress, Integer customerId, Integer vendorId,
                         Integer courierId, Integer couponId, List<Integer> items, Integer rawPrice,
                         Integer taxFee, Integer courierFee, Integer payPrice,
                         String status, String createdAt, String updatedAt) {
        this.id = id;
        this.deliveryAddress = deliveryAddress;
        this.customerId = customerId;
        this.vendorId = vendorId;
        this.courierId = courierId;
        this.couponId = couponId;
        this.items = items;
        this.rawPrice = rawPrice;
        this.taxFee = taxFee;
        this.courierFee = courierFee;
        this.payPrice = payPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public Integer getCustomerId() {
        return customerId;
    }
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
    public Integer getVendorId() {
        return vendorId;
    }
    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }
    public Integer getCourierId() {
        return courierId;
    }
    public void setCourierId(Integer courierId) {
        this.courierId = courierId;
    }
    public Integer getCouponId() {
        return couponId;
    }
    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }
    public List<Integer> getItems() {
        return items;
    }
    public void setItems(List<Integer> items) {
        this.items = items;
    }
    public Integer getRawPrice() { return rawPrice; }
    public void setRawPrice(Integer rawPrice) { this.rawPrice = rawPrice; }
    public Integer getTaxFee() { return taxFee; }
    public void setTaxFee(Integer taxFee) { this.taxFee = taxFee; }
    public Integer getCourierFee() { return courierFee; }
    public void setCourierFee(Integer courierFee) { this.courierFee = courierFee; }
    public Integer getPayPrice() { return payPrice; }
    public void setPayPrice(Integer payPrice) { this.payPrice = payPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}