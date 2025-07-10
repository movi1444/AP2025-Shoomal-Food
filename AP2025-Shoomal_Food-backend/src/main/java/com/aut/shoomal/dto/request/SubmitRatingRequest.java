package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SubmitRatingRequest
{
    @JsonProperty("order_id")
    private Integer orderId;
    private Integer rating;
    private String comment;
    private List<String> imageBase64;

    public SubmitRatingRequest() {}
    public SubmitRatingRequest(Integer orderId, Integer rating, String comment, List<String> imageBase64)
    {
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.imageBase64 = imageBase64;
    }

    public Integer getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Integer orderId)
    {
        this.orderId = orderId;
    }

    public Integer getRating()
    {
        return rating;
    }

    public void setRating(Integer rating)
    {
        this.rating = rating;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public List<String> getImageBase64()
    {
        return imageBase64;
    }

    public void setImageBase64(List<String> imageBase64)
    {
        this.imageBase64 = imageBase64;
    }
}