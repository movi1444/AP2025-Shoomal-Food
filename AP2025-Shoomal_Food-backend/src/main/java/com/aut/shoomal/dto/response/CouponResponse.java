package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CouponResponse
{
    private Integer id;
    @JsonProperty("coupon_code")
    private String couponCode;
    private String type;
<<<<<<< HEAD
    private Integer value;
=======
    private BigDecimal value;
>>>>>>> a2f6b05ac90114b00207d6b28ffe919b5874949a
    @JsonProperty("min_price")
    private Integer minPrice;
    @JsonProperty("user_count")
    private Integer userCount;
    @JsonProperty("start_date")
    private String startDate;
    @JsonProperty("end_date")
    private String endDate;
    private String scope;

    public CouponResponse() {}

<<<<<<< HEAD
    public CouponResponse(Integer id, String couponCode, String type, Integer value, Integer minPrice, Integer userCount, LocalDate startDate, LocalDate endDate, String scope) {
=======
    public CouponResponse(Integer id, String couponCode, String type, BigDecimal value, Integer minPrice, Integer userCount,
                          LocalDateTime startDate, LocalDateTime endDate)
    {
>>>>>>> a2f6b05ac90114b00207d6b28ffe919b5874949a
        this.id = id;
        this.couponCode = couponCode;
        this.type = type;
        this.value = value;
        this.minPrice = minPrice;
        this.userCount = userCount;
        this.startDate = (startDate != null) ? startDate.toString() : null;
        this.endDate = (endDate != null) ? endDate.toString() : null;
        this.scope = scope;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}