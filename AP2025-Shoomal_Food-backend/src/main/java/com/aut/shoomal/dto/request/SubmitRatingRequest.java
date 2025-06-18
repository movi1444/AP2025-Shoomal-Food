package com.aut.shoomal.dto.request;

import java.util.List;

public class SubmitRatingRequest
{
    private Integer orderId, rating;
    private String comment, imageBase64;

    public SubmitRatingRequest() {}
    public SubmitRatingRequest(Integer orderId, Integer rating, String comment, String imageBase64)
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

    public String getImageBase64()
    {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64)
    {
        this.imageBase64 = imageBase64;
    }
}