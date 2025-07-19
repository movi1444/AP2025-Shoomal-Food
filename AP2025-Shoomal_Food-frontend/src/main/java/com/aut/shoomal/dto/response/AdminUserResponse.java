package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdminUserResponse extends UserResponse {

    @JsonProperty("user_status")
    private String userStatus;

    public AdminUserResponse() {
        super();
    }

    public AdminUserResponse(Long id, String name, String phoneNumber, String email, String role,
                             String address, String profileImageBase64, BankInfoResponse bank, String userStatus) {
        super(id, name, phoneNumber, email, role, address, profileImageBase64, bank);
        this.userStatus = userStatus;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
}