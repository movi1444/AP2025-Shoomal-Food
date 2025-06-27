package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateDiscountRequest {
    @JsonProperty(value = "type", required = true)
    private String type;
    @JsonProperty(value = "value", required = true)
    private Integer value;
    @JsonProperty(value = "startDate", required = true)
    private String startDate;
    @JsonProperty(value = "endDate", required = true)
    private String endDate;
    @JsonProperty(value = "scope", required = true)
    private String scope;

    public CreateDiscountRequest() {}

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
}