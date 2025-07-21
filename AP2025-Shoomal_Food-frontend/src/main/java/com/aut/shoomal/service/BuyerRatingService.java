package com.aut.shoomal.service;

import com.aut.shoomal.dto.request.SubmitRatingRequest;
import com.aut.shoomal.dto.request.UpdateRatingRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.ItemRatingResponse;
import com.aut.shoomal.dto.response.RatingResponse;
import com.aut.shoomal.dto.response.UpdateRatingResponse;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class BuyerRatingService extends AbstractService
{
    public CompletableFuture<ApiResponse> submitRating(String token, SubmitRatingRequest request)
    {
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder("ratings", token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating submit rating request: " + e.getMessage());
            throw new RuntimeException("Error creating submit rating request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ItemRatingResponse> getItemRating(String token, Integer itemId)
    {
        String endpoint = "ratings/items/" + itemId;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendRequest(httpRequest, ItemRatingResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating get item rating request: " + e.getMessage());
            throw new RuntimeException("Error creating get item rating request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<RatingResponse> getRatingById(String token, Integer ratingId)
    {
        String endpoint = "ratings/" + ratingId;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendRequest(httpRequest, RatingResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating get rating by id request: " + e.getMessage());
            throw new RuntimeException("Error creating get rating by id request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> deleteRating(String token, Integer ratingId)
    {
        String endpoint = "ratings/" + ratingId;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .DELETE()
                    .build();
            return sendRequest(httpRequest, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating delete rating request: " + e.getMessage());
            throw new RuntimeException("Error creating delete rating request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<UpdateRatingResponse> updateRating(String token, Integer ratingId, UpdateRatingRequest request)
    {
        String endpoint = "ratings/" + ratingId;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, UpdateRatingResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating update rating request: " + e.getMessage());
            throw new RuntimeException("Error creating update rating request: " + e.getMessage(), e);
        }
    }
}