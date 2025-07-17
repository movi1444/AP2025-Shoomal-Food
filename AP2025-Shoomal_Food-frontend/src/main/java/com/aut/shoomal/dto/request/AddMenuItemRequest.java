package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddMenuItemRequest {
    @JsonProperty(value = "item_id", required = true)
    private Integer itemId;

    public AddMenuItemRequest(){}

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }
}
