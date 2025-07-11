package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FoodItemResponse {
    private Long id;
    private String name;
    @JsonProperty("imageBase64")
    private String imageBase64;
    private String description;
    private double price;
    private Integer supply;
    @JsonProperty("keywords")
    private List<String> keywords;

    public FoodItemResponse() {}

    public FoodItemResponse(Long id, String name, String imageBase64, String description, double price, Integer supply, List<String> keywords) { // Updated constructor parameter
        this.id = id;
        this.name = name;
        this.imageBase64 = imageBase64;
        this.description = description;
        this.price = price;
        this.supply = supply;
        this.keywords = keywords; // Set renamed field
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getSupply() { return supply; }
    public void setSupply(Integer supply) { this.supply = supply; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
}