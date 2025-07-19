package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserResponse
{
    private Long id;
    @JsonProperty("full_name")
    private String name;
    @JsonProperty("phone")
    private String phoneNumber;
    private String email;
    private String role;
    private String address;
    private String profileImageBase64;
    @JsonProperty("bank_info")
    private BankInfoResponse bank;

    public UserResponse() {}

    public UserResponse(Long id, String name, String phoneNumber, String email, String role, String address, String profileImageBase64, BankInfoResponse bank)
    {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.role = role;
        this.address = address;
        this.profileImageBase64 = profileImageBase64;
        this.bank = bank;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @JsonProperty("full_name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("full_name")
    public void setName(String name)
    {
        this.name = name;
    }

    @JsonProperty("phone")
    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    @JsonProperty("phone")
    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getProfileImageBase64()
    {
        return profileImageBase64;
    }

    public void setProfileImageBase64(String profileImageBase64)
    {
        this.profileImageBase64 = profileImageBase64;
    }

    @JsonProperty("bank_info")
    public BankInfoResponse getBank()
    {
        return bank;
    }

    @JsonProperty("bank_info")
    public void setBank(BankInfoResponse bank)
    {
        this.bank = bank;
    }

    public String toString() {
        return this.getName();
    }
}
