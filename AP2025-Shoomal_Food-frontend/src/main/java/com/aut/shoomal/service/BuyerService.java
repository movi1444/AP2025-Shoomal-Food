package com.aut.shoomal.service;

import com.aut.shoomal.dto.response.RestaurantResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BuyerService extends AbstractService {

    public CompletableFuture<List<RestaurantResponse>> getAllRestaurants(String token) {
        try {
            HttpRequest request = createAuthenticatedRequestBuilder("vendors", token)
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();
            return sendListRequest(request, new TypeReference<List<RestaurantResponse>>() {});
        } catch (Exception e) {
            System.err.println("Error creating getAllRestaurants request: " + e.getMessage());
            throw new RuntimeException("Client error creating getAllRestaurants request: " + e.getMessage(), e);
        }
    }
    public CompletableFuture<List<RestaurantResponse>> getAllRestaurants(String token, String search, List<String> keywords) {
        try {
            Map<String, String> params = new HashMap<>();
            if (search != null && !search.isEmpty()) {
                params.put("search", search);
            }
            if (keywords != null && !keywords.isEmpty()) {
                params.put("keywords", String.join(",", keywords));
            }

            String query = buildQueryString(params);
            String endpoint = "vendors" + query;

            HttpRequest request = createAuthenticatedRequestBuilder(endpoint, token)
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();
            return sendListRequest(request, new TypeReference<List<RestaurantResponse>>() {});
        } catch (Exception e) {
            System.err.println("Error creating getAllRestaurants with filters request: " + e.getMessage());
            throw new RuntimeException("Client error creating getAllRestaurants with filters request: " + e.getMessage(), e);
        }
    }
}