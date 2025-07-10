package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AddFoodItemRequest {
    @JsonProperty(value = "name", required = true)
    private String name;
    @JsonProperty("imageBase64")
    private String imageBase64;
    @JsonProperty(value = "description", required = true)
    private String description;
    @JsonProperty(value = "price", required = true)
    private Integer price;
    @JsonProperty(value = "supply", required = true)
    private Integer supply;
    @JsonProperty(value = "keywords", required = true)
    private List<String> keywords;
    @JsonProperty(value = "vendor_id", required = true)
    private Integer vendor_id;

    public AddFoodItemRequest(){}

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
    @JsonProperty(value = "keywords")
    public List<String> getKeywords() { return keywords; }
    @JsonProperty(value = "keywords")
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public Integer getVendor_id() {
        return vendor_id;
    }
    public void setVendor_id(Integer vendor_id) {
        this.vendor_id = vendor_id;
    }
}