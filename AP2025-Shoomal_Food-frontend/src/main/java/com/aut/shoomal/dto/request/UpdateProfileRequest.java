package com.aut.shoomal.dto.request;

import com.aut.shoomal.dto.response.BankInfoResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateProfileRequest
{
    @JsonProperty("full_name")
    private String fullName;
    private String phone, email, address, profileImageBase64;
    @JsonProperty("bank_info")
    private BankInfoResponse bankInfo;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String fullName, String phone, String email, String address, String profileImageBase64, BankInfoResponse bankInfo)
    {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.profileImageBase64 = profileImageBase64;
        this.bankInfo = bankInfo;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getProfileImageBase64()
    {
        return profileImageBase64;
    }

    public void setProfileImageBase64(String profileImageBase64)
    {
        this.profileImageBase64 = profileImageBase64;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public BankInfoResponse getBankInfo()
    {
        return bankInfo;
    }

    public void setBankInfo(BankInfoResponse bankInfo)
    {
        this.bankInfo = bankInfo;
    }
}