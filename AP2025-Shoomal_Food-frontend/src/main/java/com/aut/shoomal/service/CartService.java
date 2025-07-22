package com.aut.shoomal.service;

import com.aut.shoomal.dto.request.AddItemToCartRequest;
import com.aut.shoomal.dto.request.RemoveItemFromCartRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.CartResponse;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class CartService extends AbstractService {

    public CompletableFuture<CartResponse> addItemToCart(AddItemToCartRequest request, String token) {
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder("cart/add", token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, CartResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating add item to cart request: " + e.getMessage());
            throw new RuntimeException("Client error adding item to cart: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> removeItemFromCart(RemoveItemFromCartRequest request, String token) {
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder("cart/remove", token)
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating remove item from cart request: " + e.getMessage());
            throw new RuntimeException("Client error removing item from cart: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<CartResponse> getCart(Long userId, Long restaurantId, String token) {
        try {
            String endpoint = String.format("cart/%d/%d", userId, restaurantId);
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendRequest(httpRequest, CartResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating get cart request: " + e.getMessage());
            throw new RuntimeException("Client error getting cart: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> clearCart(Long userId, Long restaurantId, String token) {
        try {
            String endpoint = String.format("cart/clear/%d/%d", userId, restaurantId);
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .DELETE()
                    .build();
            return sendRequest(httpRequest, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating clear cart request: " + e.getMessage());
            throw new RuntimeException("Client error clearing cart: " + e.getMessage(), e);
        }
    }
}