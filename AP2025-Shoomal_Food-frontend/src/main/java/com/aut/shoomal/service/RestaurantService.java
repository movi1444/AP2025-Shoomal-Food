package com.aut.shoomal.service;

import com.aut.shoomal.dto.request.*;
import com.aut.shoomal.dto.response.*;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RestaurantService extends AbstractService
{
    public CompletableFuture<RestaurantResponse> createRestaurant(CreateRestaurantRequest request, String token)
    {
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder("restaurants", token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, RestaurantResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating restaurant request: " + e.getMessage());
            throw new RuntimeException("Error creating restaurant request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<RestaurantResponse>> getRestaurants(String token)
    {
        try {
            HttpRequest request = createAuthenticatedRequestBuilder("restaurants/mine", token)
                    .GET()
                    .build();
            return sendListRequest(request, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Client error getting restaurants request: " + e.getMessage());
            throw new RuntimeException("Client error creating get restaurants request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<RestaurantResponse> getRestaurantById(String token, Integer restaurantId)
    {
        String endpoint = "buyer/restaurants/" + restaurantId;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendRequest(httpRequest, RestaurantResponse.class);
        } catch (Exception e) {
            System.err.println("Client error getting restaurant request: " + e.getMessage());
            throw new RuntimeException("Client error getting restaurant request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<RestaurantResponse> updateRestaurant(UpdateRestaurantRequest request, String token, Integer restaurantId)
    {
        String endpoint = "restaurants/" + restaurantId;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, RestaurantResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating update restaurant request: " + e.getMessage());
            throw new RuntimeException("Error creating update restaurant request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ListItemResponse> addFoodToRestaurant(AddFoodItemRequest request, String token, Integer restaurantId)
    {
        String endpoint = "restaurants/" + restaurantId + "/item";
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, ListItemResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating add food to restaurant request: " + e.getMessage());
            throw new RuntimeException("Error creating add food to restaurant request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ListItemResponse> editFood(UpdateFoodItemRequest request, String token, Integer foodId)
    {
        String endpoint = "restaurants/" + foodId + "/item";
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, ListItemResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating edit food request: " + e.getMessage());
            throw new RuntimeException("Error creating edit food request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> deleteFoodFromRestaurant(String token, Integer foodId)
    {
        String endpoint = "restaurants/" + foodId + "/item";
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .DELETE()
                    .build();
            return sendRequest(httpRequest, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating delete food from restaurant request: " + e.getMessage());
            throw new RuntimeException("Error creating delete food from restaurant request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ListItemResponse> getFoodById(String token, Integer foodId)
    {
        String endpoint = "restaurants/" + foodId + "/item";
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendRequest(httpRequest, ListItemResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating get food by id request: " + e.getMessage());
            throw new RuntimeException("Error creating get food by id request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<ListItemResponse>> getFoodsByRestaurantId(String token, Integer restaurantId)
    {
        String endpoint = "restaurants/" + restaurantId + "/items";
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendListRequest(httpRequest, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error creating get food by restaurant id request: " + e.getMessage());
            throw new RuntimeException("Error creating get food by restaurant id request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<ListItemResponse>> getFoodsByMenuTitle(String token, Integer restaurantId, String title)
    {
        String endpoint = "restaurants/" + restaurantId + "/items/" + title;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendListRequest(httpRequest, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error creating get food by menu title request: " + e.getMessage());
            throw new RuntimeException("Error creating get food by menu title request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<MenuTitleResponse> addMenu(AddMenuTitleRequest request, String token, Integer restaurantId)
    {
        String endpoint = "restaurants/" + restaurantId + "/menu";
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, MenuTitleResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating add menu request: " + e.getMessage());
            throw new RuntimeException("Error creating add menu request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> deleteMenu(String token, Integer restaurantId, String title)
    {
        String endpoint = "restaurants/" + restaurantId + "/menu/" + title;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .DELETE()
                    .build();
            return sendRequest(httpRequest, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating delete menu request: " + e.getMessage());
            throw new RuntimeException("Error creating delete menu request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<MenuTitleResponse> editMenu(AddMenuTitleRequest request, String token, Integer restaurantId, String title)
    {
        String endpoint = "restaurants/" + restaurantId + "/menu/edit" + title;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, MenuTitleResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating update menu request: " + e.getMessage());
            throw new RuntimeException("Error creating update menu request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<MenuTitleResponse> getMenuByTitle(String token, Integer restaurantId, String title)
    {
        String endpoint = "restaurants/" + restaurantId + "/menu/" + title;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendRequest(httpRequest, MenuTitleResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating get menu by title request: " + e.getMessage());
            throw new RuntimeException("Error creating get menu by title request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<MenuTitleResponse>> getMenusByRestaurantId(String token, Integer restaurantId)
    {
        String endpoint = "restaurants/" + restaurantId + "/menus";
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendListRequest(httpRequest, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error creating get menus by restaurant id request: " + e.getMessage());
            throw new RuntimeException("Error creating get menus by restaurant id request: " + e.getMessage(), e);
        }
    }

    public  CompletableFuture<ApiResponse> addItemToMenu(AddMenuItemRequest request, String token, Integer restaurantId, String title)
    {
        String endpoint = "restaurants/" + restaurantId + "/menu/" + title;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating add item to menu request: " + e.getMessage());
            throw new RuntimeException("Error creating add item to menu request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> deleteItemFromMenu(String token, Integer restaurantId, String title, Integer foodId)
    {
        String endpoint = "restaurants/" + restaurantId + "/menu/" + title + "/" + foodId;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .DELETE()
                    .build();
            return sendRequest(httpRequest, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating delete item from menu request: " + e.getMessage());
            throw new RuntimeException("Error creating delete item from menu request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<OrderResponse>> getOrderList(
           String token,
           Integer restaurantId,
           String status,
           String search,
           String user,
           String courier)
    {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("status", status);
            params.put("search", search);
            params.put("user", user);
            params.put("courier", courier);
            String query = buildQueryString(params);

            String baseEndpoint = "restaurants/" + restaurantId + "/orders/";
            String endpoint = baseEndpoint + query;
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendListRequest(httpRequest, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error getting restaurant order list: " + e.getMessage());
            throw new RuntimeException("Error getting restaurant order list: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> changeOrderStatus(String token, Integer orderId, UpdateOrderStatusRequest request)
    {
        String endpoint = "restaurants/orders/" + orderId;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating change order status request: " + e.getMessage());
            throw new RuntimeException("Error creating change order status request: " + e.getMessage(), e);
        }
    }
}