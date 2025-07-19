package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdminUserResponse extends UserResponse {

    @JsonProperty("user_status")
    private String userStatus;

    public AdminUserResponse() {
    }

    public AdminUserResponse(Long id, String fullName, String phone, String email, String role,
                             String address, String profileImageBase64, BankInfoResponse bankInfo, String userStatus) {
        super(id, fullName, phone, email, role, address, profileImageBase64, bankInfo);
        this.userStatus = userStatus;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
}