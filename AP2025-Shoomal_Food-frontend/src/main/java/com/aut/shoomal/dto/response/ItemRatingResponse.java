package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class ItemRatingResponse
{
    @JsonProperty("avg_rating")
    private BigDecimal avgRating;
    private List<RatingResponse> comments;

    public ItemRatingResponse() {}
    public ItemRatingResponse(BigDecimal avgRating, List<RatingResponse> comments)
    {
        this.avgRating = avgRating;
        this.comments = comments;
    }

    public BigDecimal getAvgRating()
    {
        return avgRating;
    }

    public void setAvgRating(BigDecimal avgRating)
    {
        this.avgRating = avgRating;
    }

    public List<RatingResponse> getComments()
    {
        return comments;
    }

    public void setComments(List<RatingResponse> comments)
    {
        this.comments = comments;
    }
}