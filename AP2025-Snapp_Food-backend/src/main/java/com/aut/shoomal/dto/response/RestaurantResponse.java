package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestaurantResponse
{
    private Integer id;
    private String name, address, phone, logoBase64;

    public RestaurantResponse() {}
    public RestaurantResponse(Long id, String name, String address, String phone, String logoBase64)
    {
        this.id = Math.toIntExact(id);
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.logoBase64 = logoBase64;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
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

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getLogoBase64()
    {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64)
    {
        this.logoBase64 = logoBase64;
    }
}