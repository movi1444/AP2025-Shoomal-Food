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

    @JsonProperty("categories")
    private List<String> categories;


    public UpdateFoodItemRequest() {}

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getSupply() {
        return supply;
    }

    public void setSupply(Integer supply) {
        this.supply = supply;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}