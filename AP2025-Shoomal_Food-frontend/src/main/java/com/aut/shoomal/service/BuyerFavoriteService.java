package com.aut.shoomal.service;

import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.RestaurantResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BuyerFavoriteService extends AbstractService
{
    public CompletableFuture<List<RestaurantResponse>> getAllRestaurants(String token)
    {
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder("favorites/all", token)
                    .GET()
                    .build();
            return sendListRequest(httpRequest, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error creating get all restaurants request: " + e.getMessage());
            throw new RuntimeException("Error creating get all restaurants request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<RestaurantResponse>> getFavoriteRestaurants(String token)
    {
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder("favorites", token)
                    .GET()
                    .build();
            return sendListRequest(httpRequest, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error creating get favorite restaurants request: " + e.getMessage());
            throw new RuntimeException("Error creating get favorite restaurants request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> addRestaurantToFavorite(String token, Integer restaurantId)
    {
        String endpoint = "favorites/" + restaurantId;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .PUT(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();
            return sendRequest(httpRequest, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating add restaurant favorite request: " + e.getMessage());
            throw new RuntimeException("Error creating add restaurant favorite request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> deleteRestaurantFromFavorite(String token, Integer restaurantId)
    {
        String endpoint = "favorites/" + restaurantId;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .DELETE()
                    .build();
            return sendRequest(httpRequest, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating delete restaurant favorite request: " + e.getMessage());
            throw new RuntimeException("Error creating delete restaurant favorite request: " + e.getMessage(), e);
        }
    }
}