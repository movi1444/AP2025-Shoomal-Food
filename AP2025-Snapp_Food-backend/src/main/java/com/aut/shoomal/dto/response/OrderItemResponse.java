package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderItemResponse {
    private Integer id;
    @JsonProperty("item_id")
    private Integer itemId;
    @JsonProperty("item_name")
    private String itemName;
    private Integer quantity;
    @JsonProperty("price_at_order")
    private Integer priceAtOrder;

    public OrderItemResponse() {}

    public OrderItemResponse(Integer id, Integer itemId, String itemName, Integer quantity, Integer priceAtOrder) {
        this.id = id;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPriceAtOrder() {
        return priceAtOrder;
    }

    public void setPriceAtOrder(Integer priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }
}