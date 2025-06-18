package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateRestaurantRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("address")
    private String address;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("logoBase64")
    private String logoBase64;

    @JsonProperty("tax_fee")
    private Integer taxFee;

    @JsonProperty("additional_fee")
    private Integer additionalFee;


    public UpdateRestaurantRequest() {}


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public Integer getTaxFee() {
        return taxFee;
    }

    public void setTaxFee(Integer taxFee) {
        this.taxFee = taxFee;
    }

    public Integer getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(Integer additionalFee) {
        this.additionalFee = additionalFee;
    }
}