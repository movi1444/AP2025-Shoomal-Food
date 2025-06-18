package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderItemRequest
{
    @JsonProperty(value = "item_id", required = true)
    private Integer itemId;
    @JsonProperty(value = "quantity", required = true)
    private Integer quantity;

    public OrderItemRequest() {}

    public OrderItemRequest(Integer itemId, Integer quantity)
    {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public Integer getItemId()
    {
        return itemId;
    }

    public void setItemId(Integer itemId)
    {
        this.itemId = itemId;
    }

    public Integer getQuantity()
    {
        return quantity;
    }

    public void setQuantity(Integer quantity)
    {
        this.quantity = quantity;
    }
}
