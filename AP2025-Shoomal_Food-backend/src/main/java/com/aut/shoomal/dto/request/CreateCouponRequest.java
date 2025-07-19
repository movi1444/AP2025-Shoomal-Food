package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateCouponRequest
{
    @JsonProperty("coupon_code")
    private String couponCode;
    private String type;
    private BigDecimal value;
    @JsonProperty("min_price")
    private Integer minPrice;
    @JsonProperty("user_count")
    private Integer userCount;
    @JsonProperty("start_date")
    private String startDate;
    @JsonProperty("end_date")
    private String endDate;

    public CreateCouponRequest() {}

    public CreateCouponRequest(String couponCode, String type, BigDecimal value, Integer minPrice, Integer userCount,
                               LocalDate startDate, LocalDate endDate) // Changed from LocalDateTime
    {
        this.couponCode = couponCode;
        this.type = type;
        this.value = value;
        this.minPrice = minPrice;
        this.userCount = userCount;
        this.startDate = (startDate != null) ? startDate.toString() : null;
        this.endDate = (endDate != null) ? endDate.toString() : null;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Integer getMinPrice()
    {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice)
    {
        this.minPrice = minPrice;
    }

    public Integer getUserCount()
    {
        return userCount;
    }

    public void setUserCount(Integer userCount)
    {
        this.userCount = userCount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}