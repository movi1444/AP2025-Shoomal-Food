package com.aut.shoomal.dto.request;

import com.aut.shoomal.entity.user.UserStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateApprovalRequest {

    @JsonProperty(value = "status", required = true)
    private UserStatus userStatus;

    public UpdateApprovalRequest() {}

    public UserStatus getStatus() {
        return userStatus;
    }

    public void setStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }
}
