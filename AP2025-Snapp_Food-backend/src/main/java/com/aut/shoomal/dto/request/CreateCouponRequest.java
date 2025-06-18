package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateCouponRequest {
    @JsonProperty(value = "coupon_code", required = true)
    private String couponCode;
    @JsonProperty(value = "type", required = true)
    private String type;
    @JsonProperty(value = "value", required = true)
    private Integer value;
    @JsonProperty(value = "min_price", required = true)
    private Integer minPrice;
    @JsonProperty(value = "user_count", required = true)
    private Integer userCount;
    @JsonProperty(value = "start_date", required = true)
    private String startDate;
    @JsonProperty(value = "end_date", required = true)
    private String endDate;

    public CreateCouponRequest() {}

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
    public Integer getMinPrice() { return minPrice; }
    public void setMinPrice(Integer minPrice) { this.minPrice = minPrice; }
    public Integer getUserCount() { return userCount; }
    public void setUserCount(Integer userCount) { this.userCount = userCount; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}