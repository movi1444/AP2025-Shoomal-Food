package com.aut.shoomal.entity.restaurant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RestaurantOrderStatus {
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    SERVED("served");

    private final String value;

    RestaurantOrderStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RestaurantOrderStatus fromValue(String value) {
        for (RestaurantOrderStatus s : RestaurantOrderStatus.values()) {
            if (s.value.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown restaurant action status: " + value);
    }
}