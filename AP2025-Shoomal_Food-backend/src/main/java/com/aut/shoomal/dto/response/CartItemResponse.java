package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CartItemResponse {
    private Long id;
    @JsonProperty("food_item_id")
    private Long foodItemId;
    @JsonProperty("food_item_name")
    private String foodItemName;
    private Integer quantity;
    private Integer unitPrice; // This field is required for the constructor being used
    @JsonProperty("item_total_price")
    private Integer itemTotalPrice;

    public CartItemResponse() {}

    public CartItemResponse(Long id, Long foodItemId, String foodItemName, Integer quantity, Integer unitPrice, Integer itemTotalPrice) {
        this.id = id;
        this.foodItemId = foodItemId;
        this.foodItemName = foodItemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.itemTotalPrice = itemTotalPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(Long foodItemId) {
        this.foodItemId = foodItemId;
    }

    public String getFoodItemName() {
        return foodItemName;
    }

    public void setFoodItemName(String foodItemName) {
        this.foodItemName = foodItemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Integer unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getItemTotalPrice() {
        return itemTotalPrice;
    }

    public void setItemTotalPrice(Integer itemTotalPrice) {
        this.itemTotalPrice = itemTotalPrice;
    }
}
