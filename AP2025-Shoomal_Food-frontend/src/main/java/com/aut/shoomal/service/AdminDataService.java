package com.aut.shoomal.service;

import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.dto.response.TransactionResponse;
import com.aut.shoomal.dto.response.UserResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminDataService extends AbstractService {

    public CompletableFuture<List<UserResponse>> getAllUsers(String token) {
        try {
            HttpRequest request = createAuthenticatedRequestBuilder("admin/users", token)
                    .GET()
                    .build();
            return sendListRequest(request, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error creating getAllUsers request: " + e.getMessage());
            throw new RuntimeException("Client error creating getAllUsers request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<OrderResponse>> getAllOrders(String token) {
        try {
            HttpRequest request = createAuthenticatedRequestBuilder("admin/orders", token)
                    .GET()
                    .build();
            return sendListRequest(request, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error creating getAllOrders request: " + e.getMessage());
            throw new RuntimeException("Client error creating getAllOrders request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<TransactionResponse>> getAllTransactions(String token) {
        try {
            HttpRequest request = createAuthenticatedRequestBuilder("admin/transactions", token)
                    .GET()
                    .build();
            return sendListRequest(request, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error creating getAllTransactions request: " + e.getMessage());
            throw new RuntimeException("Client error creating getAllTransactions request: " + e.getMessage(), e);
        }
    }
}