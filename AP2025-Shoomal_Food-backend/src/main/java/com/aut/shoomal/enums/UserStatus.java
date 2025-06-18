package com.aut.shoomal.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserStatus {
    APPROVED("approved"),
    REJECTED("rejected");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserStatus fromValue(String value) {
        for (UserStatus s : UserStatus.values()) {
            if (s.value.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
