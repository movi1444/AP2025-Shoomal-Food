package com.aut.shoomal.dto.response;

public class UpdateDeliveryStatusResponse {
    private String message;
    private OrderResponse order;

    public UpdateDeliveryStatusResponse() {}
    public UpdateDeliveryStatusResponse(String message, OrderResponse order) {
        this.message = message;
        this.order = order;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public OrderResponse getOrder()
    {
        return order;
    }

    public void setOrder(OrderResponse order)
    {
        this.order = order;
    }
}
