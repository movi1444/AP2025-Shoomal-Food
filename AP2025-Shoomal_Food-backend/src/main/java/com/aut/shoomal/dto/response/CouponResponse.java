package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class CouponResponse {
    private Integer id;
    @JsonProperty("coupon_code")
    private String couponCode;
    private String type;
    private Integer value;
    @JsonProperty("startDate")
    private String startDate;
    @JsonProperty("endDate")
    private String endDate;

    public CouponResponse() {}

    public CouponResponse(Integer id, String couponCode, String type, Integer value, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.couponCode = couponCode;
        this.type = type;
        this.value = value;
        this.startDate = (startDate != null) ? startDate.toString() : null;
        this.endDate = (endDate != null) ? endDate.toString() : null;
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

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
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