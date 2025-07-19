package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ListItemResponse
{
    private Integer id;
    private String name, imageBase64, description;
    @JsonProperty("vendor_id")
    private Integer vendorId;
    private Integer price, supply;
    private List<String> keywords;

    public ListItemResponse() {}
    public ListItemResponse(Long id, String name, String imageBase64, String description, Long vendorId, Integer price, Integer supply, List<String> keywords)
    {
        this.id = Math.toIntExact(id);
        this.name = name;
        this.imageBase64 = imageBase64;
        this.description = description;
        this.vendorId = Math.toIntExact(vendorId);
        this.price = price;
        this.supply = supply;
        this.keywords = keywords;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public List<String> getKeywords()
    {
        return keywords;
    }

    public void setKeywords(List<String> keywords)
    {
        this.keywords = keywords;
    }

    public Integer getSupply()
    {
        return supply;
    }

    public void setSupply(Integer supply)
    {
        this.supply = supply;
    }

    public Integer getPrice()
    {
        return price;
    }

    public void setPrice(Integer price)
    {
        this.price = price;
    }

    public Integer getVendorId()
    {
        return vendorId;
    }

    public void setVendorId(Integer vendorId)
    {
        this.vendorId = vendorId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getImageBase64()
    {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64)
    {
        this.imageBase64 = imageBase64;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}