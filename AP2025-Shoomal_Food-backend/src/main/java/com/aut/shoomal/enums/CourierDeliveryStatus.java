package com.aut.shoomal.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CourierDeliveryStatus {
    ACCEPTED("accepted"),
    RECEIVED("received"),
    DELIVERED("delivered");

    private final String value;

    CourierDeliveryStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CourierDeliveryStatus fromValue(String value) {
        for (CourierDeliveryStatus s : CourierDeliveryStatus.values()) {
            if (s.value.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown courier delivery status: " + value);
    }
}
