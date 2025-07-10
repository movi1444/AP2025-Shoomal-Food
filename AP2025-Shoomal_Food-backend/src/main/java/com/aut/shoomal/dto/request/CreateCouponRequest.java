package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

<<<<<<< HEAD
import java.util.List;

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
    @JsonProperty(value = "scope", required = true)
    private String scope;

    public CreateCouponRequest() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
=======
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
                          LocalDateTime startDate, LocalDateTime endDate)
    {
        this.couponCode = couponCode;
        this.type = type;
        this.value = value;
        this.minPrice = minPrice;
        this.userCount = userCount;
        this.startDate = (startDate != null) ? startDate.toString() : null;
        this.endDate = (endDate != null) ? endDate.toString() : null;
    }
>>>>>>> a2f6b05ac90114b00207d6b28ffe919b5874949a

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

<<<<<<< HEAD
    public Integer getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }
=======
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
>>>>>>> a2f6b05ac90114b00207d6b28ffe919b5874949a
}