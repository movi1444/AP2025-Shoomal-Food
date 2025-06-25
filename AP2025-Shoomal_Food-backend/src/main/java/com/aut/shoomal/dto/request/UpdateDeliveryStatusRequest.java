package com.aut.shoomal.dto.request;

import com.aut.shoomal.entity.user.CourierDeliveryStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateDeliveryStatusRequest {
    @JsonProperty(value = "status", required = true)
    private CourierDeliveryStatus status;

    public UpdateDeliveryStatusRequest() {}

    public CourierDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(CourierDeliveryStatus status) {
        this.status = status;
    }
}
