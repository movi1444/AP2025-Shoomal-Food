package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CartResponse {
    private Long id;
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("restaurant_id")
    private Long restaurantId;
    private List<CartItemResponse> items;
    @JsonProperty("total_price")
    private Integer totalPrice;

    public CartResponse() {}

    public CartResponse(Long id, Long userId, Long restaurantId, List<CartItemResponse> items, Integer totalPrice) {
        this.id = id;
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }
}