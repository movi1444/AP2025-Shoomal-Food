package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.aut.shoomal.Mamad.restaurant.RestaurantOrderStatus;

public class UpdateOrderStatusRequest {
    @JsonProperty(value = "status", required = true)
    private RestaurantOrderStatus status;

    public UpdateOrderStatusRequest() {}

    public RestaurantOrderStatus getStatus() {
        return status;
    }

    public void setStatus(RestaurantOrderStatus status) {
        this.status = status;
    }
}