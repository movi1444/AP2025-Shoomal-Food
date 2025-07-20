package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.aut.shoomal.entity.restaurant.RestaurantOrderStatus;

public class UpdateOrderStatusRequest {
    @JsonProperty(value = "status", required = true)
    private String status;

    public UpdateOrderStatusRequest() {}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}