package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserLoginRequest
{
    @JsonProperty(value = "phone", required = true)
    private String phone;
    @JsonProperty(value = "password", required = true)
    private String password;

    public UserLoginRequest() {}

    public UserLoginRequest(String phone, String password)
    {
        this.phone = phone;
        this.password = password;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
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