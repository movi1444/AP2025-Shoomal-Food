package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddItemToCartRequest {
    @JsonProperty(value = "restaurant_id", required = true)
    private Long restaurantId;
    @JsonProperty(value = "food_item_id", required = true)
    private Long foodItemId;
    @JsonProperty(value = "quantity", required = true)
    private Integer quantity;

    public AddItemToCartRequest() {}

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Long getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(Long foodItemId) {
        this.foodItemId = foodItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}