package com.aut.shoomal.dto.response;

public class UserResponse
{
    private Long id;
    private String name, phoneNumber, email, role, address, profileImageBase64;
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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

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

    public BankInfoResponse getBank()
    {
        return bank;
    }

    public void setBank(BankInfoResponse bank)
    {
        this.bank = bank;
    }
}