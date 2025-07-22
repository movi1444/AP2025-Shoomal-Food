package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RemoveItemFromCartRequest {
    @JsonProperty(value = "restaurant_id", required = true)
    private Long restaurantId;
    @JsonProperty(value = "food_item_id", required = true)
    private Long foodItemId;

    public RemoveItemFromCartRequest() {}

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
}