package com.aut.shoomal.dto.request;

import com.aut.shoomal.dto.response.BankInfoResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRegisterRequest
{
    @JsonProperty(value = "full_name", required = true)
    private String fullName;
    @JsonProperty(value = "phone", required = true)
    private String phone;
    private String email;
    @JsonProperty(value = "password", required = true)
    private String password;
    @JsonProperty(value = "role", required = true)
    private String role;
    @JsonProperty(value = "address", required = true)
    private String address;
    private String profileImageBase64;
    @JsonProperty("bank_info")
    private BankInfoResponse bankInfo;

    public UserRegisterRequest() {}

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public BankInfoResponse getBankInfo()
    {
        return bankInfo;
    }

    public void setBankInfo(BankInfoResponse bankInfo)
    {
        this.bankInfo = bankInfo;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }
}
