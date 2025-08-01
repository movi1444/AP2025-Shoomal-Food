package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OrderResponse {
    private Integer id;
    @JsonProperty("delivery_address")
    private String deliveryAddress;
    @JsonProperty("customer_name")
    private String customerName;
    @JsonProperty("vendor_name")
    private String vendorName;
    @JsonProperty("courier_name")
    private String courierName;
    @JsonProperty("coupon_id")
    private Integer couponId;
    private List<String> items;
    @JsonProperty("raw_price")
    private Integer rawPrice;
    @JsonProperty("tax_fee")
    private Integer taxFee;
    @JsonProperty("additional_fee")
    private Integer additionalFee;
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

    public OrderResponse(Integer id, String deliveryAddress, String customerName, String vendorName,
                         String courierName, Integer couponId, List<String> items, Integer rawPrice, Integer additionalFee,
                         Integer taxFee, Integer courierFee, Integer payPrice,
                         String status, String createdAt, String updatedAt) {
        this.id = id;
        this.deliveryAddress = deliveryAddress;
        this.customerName = customerName;
        this.vendorName = vendorName;
        this.courierName = courierName;
        this.couponId = couponId;
        this.items = items;
        this.rawPrice = rawPrice;
        this.additionalFee = additionalFee;
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
    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public String getVendorName() {
        return vendorName;
    }
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }
    public String getCourierName() {
        return courierName;
    }
    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }
    public Integer getCouponId() {
        return couponId;
    }
    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }
    public List<String> getItems() {
        return items;
    }
    public void setItems(List<String> items) {
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
    public Integer getAdditionalFee()
    {
        return additionalFee;
    }
    public void setAdditionalFee(Integer additionalFee)
    {
        this.additionalFee = additionalFee;
    }
}