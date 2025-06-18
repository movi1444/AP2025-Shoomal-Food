package com.aut.shoomal.dto.request;

import java.util.List;

public class UpdateRatingRequest
{
    private Integer rating;
    private String comment;
    private List<String> imageBase64;

    public UpdateRatingRequest() {}
    public UpdateRatingRequest(Integer rating, String comment, List<String> imageBase64)
    {
        this.rating = rating;
        this.comment = comment;
        this.imageBase64 = imageBase64;
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