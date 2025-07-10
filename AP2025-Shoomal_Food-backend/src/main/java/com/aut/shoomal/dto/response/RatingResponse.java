package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RatingResponse
{
    private Integer id;
    @JsonProperty("item_id")
    private Integer itemId;
    private Integer rating;
    private String comment;
    private List<String> imageBase64;
    @JsonProperty("user_id")
    private Integer userId;
    @JsonProperty("created_at")
    private String createdAt;

    public RatingResponse() {}

    public RatingResponse(Integer id, Integer itemId, Integer rating, String comment, List<String> imageBase64,
                          Integer userId, String createdAt)
    {
        this.id = id;
        this.itemId = itemId;
        this.rating = rating;
        this.comment = comment;
        this.imageBase64 = imageBase64;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getItemId()
    {
        return itemId;
    }

    public void setItemId(Integer itemId)
    {
        this.itemId = itemId;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public Integer getRating()
    {
        return rating;
    }

    public void setRating(Integer rating)
    {
        this.rating = rating;
    }

    public List<String> getImageBase64()
    {
        return imageBase64;
    }

    public void setImageBase64(List<String> imageBase64)
    {
        this.imageBase64 = imageBase64;
    }

    public Integer getUserId()
    {
        return userId;
    }

    public void setUserId(Integer userId)
    {
        this.userId = userId;
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }
}