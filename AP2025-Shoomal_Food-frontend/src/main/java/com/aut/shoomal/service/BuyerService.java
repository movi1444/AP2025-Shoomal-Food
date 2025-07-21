package com.aut.shoomal.service;

import com.aut.shoomal.dto.request.ListItemRequest;
import com.aut.shoomal.dto.response.RestaurantResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.aut.shoomal.dto.request.ListVendorsRequest;
import com.aut.shoomal.dto.response.ListItemResponse;

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

    public CompletableFuture<Map<String, Object>> searchPopularRestaurantsAndFoods(String token, String searchKeyword) {
        try {
            ListVendorsRequest requestBody = new ListVendorsRequest();
            requestBody.setSearch(searchKeyword);
            String requestJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = createAuthenticatedRequestBuilder("search", token)
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            return httpClient.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                    .thenApply(httpResponse -> {
                        String responseBody = httpResponse.body();
                        int statusCode = httpResponse.statusCode();
                        System.out.println("HTTP Response Status: " + statusCode);

                        if (statusCode >= 200 && statusCode < 300) {
                            try {
                                TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
                                return objectMapper.readValue(responseBody, typeRef);
                            } catch (IOException e) {
                                System.err.println("Error deserializing search response: " + e.getMessage());
                                throw new RuntimeException("Failed to process search results: " + e.getMessage(), e);
                            }
                        } else {
                            handleHttpErrorResponse(statusCode, responseBody);
                            return null;
                        }
                    })
                    .exceptionally(e -> {
                        handleCompletableFutureException(e);
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Error creating popular search request: " + e.getMessage());
            throw new RuntimeException("Client error creating popular search request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<RestaurantResponse>> searchAllRestaurants(String token, String searchKeyword) {
        try {
            ListVendorsRequest requestBody = new ListVendorsRequest();
            requestBody.setSearch(searchKeyword);
            String requestJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = createAuthenticatedRequestBuilder("vendors", token)
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();
            return sendListRequest(request, new TypeReference<List<RestaurantResponse>>() {});
        } catch (Exception e) {
            System.err.println("Error creating all restaurants search request: " + e.getMessage());
            throw new RuntimeException("Client error creating all restaurants search request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<ListItemResponse>> searchAllFoods(String token, String searchKeyword) {
        try {
            ListItemRequest requestBody = new ListItemRequest();
            requestBody.setSearch(searchKeyword);
            String requestJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = createAuthenticatedRequestBuilder("items", token)
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();
            return sendListRequest(request, new TypeReference<List<ListItemResponse>>() {});
        } catch (Exception e) {
            System.err.println("Error creating all foods search request: " + e.getMessage());
            throw new RuntimeException("Client error creating all foods search request: " + e.getMessage(), e);
        }
    }
}