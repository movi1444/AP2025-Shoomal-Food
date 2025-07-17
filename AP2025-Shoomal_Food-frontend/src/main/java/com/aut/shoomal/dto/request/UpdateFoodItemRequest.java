package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UpdateFoodItemRequest {
    @JsonProperty("name")
    private String name;
    @JsonProperty("imageBase64")
    private String imageBase64;
    @JsonProperty("description")
    private String description;
    @JsonProperty("price")
    private Integer price;
    @JsonProperty("supply")
    private Integer supply;
    @JsonProperty("keywords")
    private List<String> keywords;
    @JsonProperty("vendor_id")
    private Integer vendor_id;

    public UpdateFoodItemRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getSupply() { return supply; }
    public void setSupply(Integer supply) { this.supply = supply; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public Integer getVendor_id() {
        return vendor_id;
    }
    public void setVendor_id(Integer vendor_id) {
        this.vendor_id = vendor_id;
    }
}