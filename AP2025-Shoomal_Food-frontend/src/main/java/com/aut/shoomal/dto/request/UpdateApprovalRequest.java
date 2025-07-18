package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateApprovalRequest {

    @JsonProperty(value = "status", required = true)
    private String userStatus;

    public UpdateApprovalRequest() {}

    public String getStatus() {
        return userStatus;
    }

    public void setStatus(String userStatus) {
        this.userStatus = userStatus;
    }
}