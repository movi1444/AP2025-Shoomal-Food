package com.aut.shoomal.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserResponse
{
    private Integer id;
    @JsonProperty("full_name")
    private String fullName;
    private String phone, email, role, address;
    private String profileImageBase64;
    @JsonProperty("bank_info")
    private BankInfoResponse bankInfo;

    public UserResponse() {}

    public UserResponse(Long id, String fullName, String phone, String email, String role,
                        String address, String profileImageBase64, BankInfoResponse bankInfo)
    {
        this.id = Math.toIntExact(id);
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.address = address;
        this.profileImageBase64 = profileImageBase64;
        this.bankInfo = bankInfo;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
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

    public BankInfoResponse getBankInfo()
    {
        return bankInfo;
    }

    public void setBankInfo(BankInfoResponse bankInfo)
    {
        this.bankInfo = bankInfo;
    }
}